# Alert Generator 完整說明

> 對應程式：`src/main/java/com/centerops/analytics/AlertGenerator.java`

## 1. 這個模組解決什麼問題？

修課紀錄只保存「尚未開始、進行中、已完成」等事實；使用者真正需要看到的是哪些紀錄值得注意。
`AlertGenerator` 把修課事實轉換成警示：

```text
Enrollment
    │
    │ status + startDate
    ▼
Alert Generator
    │
    ├─ 不需要提醒 ──► 移除舊的 [AUTO] Alert
    │
    └─ 需要提醒 ───► 建立或更新 [AUTO] Alert
                         │
                         ▼
                    Alert Repository
```

Generator 負責「要不要警示、priority 是多少、message 是什麼」。`MaxHeap` 只負責將已經具有 priority
的 Alert 排序，兩者不能混在一起。

---

## 2. MVP 規則

| Enrollment 狀態 | 日期條件 | 結果 |
|-|-|-|
| `NOT_STARTED` | 不看日期 | priority 1 |
| `IN_PROGRESS` | 未滿 30 天 | 不產生警示 |
| `IN_PROGRESS` | 30–89 天 | priority 2 |
| `IN_PROGRESS` | 90 天以上 | priority 3 |
| `COMPLETED` | 不看日期 | 不產生警示 |

邊界值採「滿」30 天與「滿」90 天，因此第 30 天已是 priority 2，第 90 天已是 priority 3。

```text
經過天數     0                30                         90
             │                 │                          │
IN_PROGRESS  ├─ 無警示 ────────┼─ priority 2 ─────────────┼─ priority 3 ──►
                               ▲                          ▲
                            含第 30 天                  含第 90 天
```

---

## 3. 為什麼使用 `[AUTO]` 前綴？

目前 MVP 的 `alerts` 資料表沒有 `source` 或 `enrollment_id` 欄位，但 Generator 必須區分：

- `sample_data.sql` 建立的 Demo Alert
- 系統依 Enrollment 自動建立的 Alert

因此自動訊息統一以 `[AUTO] ` 開頭，並使用下列條件尋找既有警示：

```text
person_id + course_id + message starts with "[AUTO] "
```

如此可以更新或移除自己的警示，又不會誤動 Demo 資料。這是 MVP 的識別策略；若未來需要完整稽核，
可再新增 `source`、`enrollment_id` 與唯一約束。

---

## 4. 公開方法

### `synchronize(Enrollment enrollment)`

```java
public void synchronize(Enrollment enrollment) {
    synchronize(enrollment, LocalDate.now());
}
```

用途：以系統今天日期，同步一筆 Enrollment 對應的自動警示。

可能結果：

- 沒有舊警示且符合規則：建立 Alert。
- 已有舊警示且符合規則：更新 priority、message，並設為未解決。
- 已有舊警示但不再符合規則：只刪除該筆 `[AUTO]` Alert。
- 沒有舊警示且不符合規則：不做寫入。

這是唯一公開方法。接收 `today` 的同名方法維持 package-private，只供單元測試固定日期使用，避免測試結果
隨執行日期改變。

---

## 5. 核心程式碼解說

### 先找出既有自動警示

```java
Optional<Alert> existingAlert = findExistingAlert(enrollment);
AlertRuleResult result = evaluate(enrollment, today);
```

先查舊資料，才能判斷要建立、更新或移除；`evaluate` 只計算規則結果，不直接操作 Repository。

### 不需警示時移除舊資料

```java
if (result == null) {
    existingAlert.ifPresent(alertRepository::delete);
    return;
}
```

`null` 在此代表「目前不需要警示」。刪除範圍只限已由 `[AUTO]` 前綴找到的資料。

### 建立或重用同一筆 Alert

```java
Alert alert = existingAlert.orElseGet(() -> Alert.builder()
        .person(enrollment.getPerson())
        .course(enrollment.getCourse())
        .resolved(false)
        .build());

alert.setPriority(result.priority());
alert.setMessage(AUTO_MESSAGE_PREFIX + result.message());
alert.setResolved(false);
alertRepository.save(alert);
```

有舊資料時直接更新同一物件，沒有才建立新物件，避免每次狀態更新都累積重複警示。

### 計算進行天數

```java
long elapsedDays = ChronoUnit.DAYS.between(enrollment.getStartDate(), today);
```

使用日期差而不是自己計算月份，避免不同月份天數造成錯誤。規則先判斷 90 天，再判斷 30 天，因為
90 天同時也符合「至少 30 天」；若順序相反，高優先警示會錯誤地停在 priority 2。

---

## 6. 與 EnrollmentService 的整合時機

```text
POST /api/enrollments 或 PUT /api/enrollments/{id}
                    │
                    ▼
          儲存 Enrollment
                    │
                    ▼
      AlertGenerator.synchronize(saved)
                    │
                    ▼
          回傳 EnrollmentResponse
```

Generator 與 Enrollment 寫入位於同一個 transaction。若警示同步失敗，整次操作會一起回滾，不會出現
Enrollment 已更新但 Alert 還停留在舊狀態的情況。

Generator 不放在 `GET /api/alerts` 中執行，因為 GET 應保持唯讀；重新整理警示頁面不應偷偷修改資料庫。

---

## 7. 與 MaxHeap 的合作方式

```text
Alert Generator ──► 產生 priority
                          │
                          ▼
Alert Repository ──► 取出符合篩選的完整集合
                          │
                          ▼
                     MaxHeap 排序
                          │
                          ▼
                       分頁回傳
```

必須先完成全域排序再分頁。若先取資料庫第 1 頁再用 Heap 排序，只能保證頁內順序，priority 3 仍可能落在
後續頁面。

---

## 8. 複雜度

單筆 Generator 同步只查找最多一筆自動警示，再執行一次 save 或 delete；業務規則判斷本身是 `O(1)`。

Alert 查詢若共有 `n` 筆符合資料：

- 全部插入 MaxHeap：`O(n log n)`
- 依序移除全部元素：`O(n log n)`
- 額外空間：`O(n)`

對本專案的 MVP 資料量足夠，也能清楚展示自訂 MaxHeap 的實際用途。正式大型系統通常會讓資料庫索引完成
排序與分頁，不會為展示資料結構而將全部資料載入記憶體。

---

## 9. 測試重點

- `NOT_STARTED` 建立 priority 1。
- 第 30 天建立 priority 2。
- 第 90 天建立或更新為 priority 3。
- 未滿 30 天移除舊的自動警示。
- `COMPLETED` 移除舊的自動警示。
- `IN_PROGRESS` 缺少 startDate 時不建立錯誤警示。
- Enrollment 建立與狀態更新後確實呼叫 Generator。
- Alert 先以 MaxHeap 全域排序，再進行每頁 10 筆的分頁。

---

## 10. 常見答辯問題

### Q1：為什麼 Generator 不直接寫在 MaxHeap 裡？

Generator 是會改變的業務規則；MaxHeap 是可重用的資料結構。分開後可以各自測試，也能讓 Heap 排列其他型別。

### Q2：為什麼完成後刪除，而不是設為 resolved？

目前 UI 沒有 resolved 篩選或歷史頁面，規格也定義 `COMPLETED` 不產生警示。MVP 因此只移除系統自己的
`[AUTO]` 警示，使使用者不再看到它；Demo 警示不受影響。

### Q3：為什麼不是每次查詢時掃描全部 Enrollment？

GET 不應產生資料庫副作用，而且每次開啟頁面都掃描全部修課紀錄會重複浪費資源。現在採寫入時同步，
狀態改變後立即維護對應警示。

### Q4：目前設計最大的限制是什麼？

時間經過 30 或 90 天但 Enrollment 沒有任何寫入時，priority 不會自行升級。MVP 可在展示時透過固定測試資料
驗證規則；未來可加入每日排程呼叫批次同步，同時保留目前單筆規則。

### Q5：為什麼自動警示要加前綴？

因為目前 schema 沒有警示來源欄位。前綴是最低成本且不改 schema 的 MVP 做法，能避免 Generator 誤動
sample data；正式擴充時應改為明確欄位與資料庫唯一約束。
