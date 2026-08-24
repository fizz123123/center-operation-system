# 中心營運分析系統 - Test Strategy

## 1. Testing Goal

測試目的：

- 確認系統功能正確
- 確認資料結構正確
- 確認 API 整合正常
- 驗證演算法效能


測試分為：

1. Unit Test
2. Integration Test
3. Performance Test

---

# 2. Testing Structure


```text
src/test/java/

com.centerops

├── service

├── controller

├── datastructure

├── algorithm

└── integration
```

測試與功能程式碼在相同功能分支完成：Backend Service 與 Controller 測試屬於 `feature/backend-core`；資料結構與演算法測試屬於 `feature/algorithm-module`；跨資料庫、API 與前端的完整流程屬於 `feature/integration-testing`。


---

# 3. Unit Test


## 3.1 Service Test


使用：

- JUnit 5
- Mockito


測試：

## PersonService


案例：

### 新增成功

Input:

```text
Valid Person
```


Expected:

```text
Create Success
```


---

### Email 重複


Input:

```text
Existing Email
```


Expected:

```text
Duplicate Exception
```

其他案例：

- 查詢不存在的 Person 時拋出 ResourceNotFoundException
- 姓名搜尋與 Email 搜尋均在分頁前套用
- `ACTIVE`／`INACTIVE` 篩選結果與分頁 metadata 正確
- 人員統計滿足 `total = active + inactive`
- 搜尋或篩選不影響全域統計結果


---

## CourseService


測試：

- 建立課程
- 查詢課程
- 重複課程代碼
- 禁止課程將自己設為先修課程
- 禁止先修關係形成 Cycle
- 課程代碼或名稱搜尋在分頁前套用
- `CourseResponse` 正確回傳 `prerequisiteIds`
- 新增先修關係後重新查詢可取得該關係
- 刪除先修關係後 Database 與重新查詢結果一致
- 可用先修課程排除自己、既有關係與會形成 Cycle 的課程
- 查詢候選後新增關係時仍再次執行 Cycle 驗證
- Learning Path 必須先列出先修課程


---

## EnrollmentService


測試：

- 正常註冊
- 重複註冊
- 可註冊課程排除已註冊項目，以及直接或間接先修尚未完成的課程
- 即使繞過 Frontend 直接呼叫註冊 API，先修尚未完成仍回傳 `409 CONFLICT`
- 狀態更新
- 完成課程時寫入開始與完成日期
- 禁止學習狀態倒退
- `COMPLETED` 紀錄不可再次修改
- 依課程名稱 `asc`／`desc` 排序後再分頁
- `sort=courseName` 對完整查詢結果執行 Merge Sort，而非只排序目前頁面
- 個人學習紀錄先依 person 篩選，再執行 Merge Sort 與分頁
- 未指定 sort 時維持 enrollment id 升序的 Repository 分頁
- 非法 sort 欄位或 direction 回傳 `400 BAD REQUEST`

Demo SQL 額外以遞迴先修閉包驗證 1,000 筆 Enrollment；任何已註冊課程的直接與間接先修都必須存在且為
`COMPLETED`，先修完成日期也必須早於後續課程開始日期。


---

## DashboardService


測試：

- 正確計算完成率並四捨五入至小數一位
- Enrollment 為零時完成率回傳 0


## AlertService

- 未提供 priority 時依 `priority DESC, createdAt ASC` 查詢
- priority `1`、`2`、`3` 的篩選結果與分頁 metadata 正確
- priority 不在 1–3 時回傳 `400 BAD REQUEST`
- Alert Generator 依 Enrollment status 與 start date 產生正確 priority
- `COMPLETED` 不產生警示
- SQL 基準警示與 Runtime Generator 使用相同 `[AUTO]` 識別規則，更新時不產生重複資料


---

## Pagination


測試：

- Person、Course、Enrollment、Alert 每頁固定 10 筆
- 回傳 page、totalElements、totalPages、first、last metadata
- Controller 正確傳遞 page query parameter
- 搜尋、篩選與排序後的 `totalElements`／`totalPages` 以完整條件結果計算
- 切換查詢條件後從 page 0 查詢


---

# 4. Data Structure Test


## 4.1 Custom Hash Table


測試：

## Insert


```text
put(key,value)
```


Expected:

資料存在。


---

## Collision


測試：

不同 key 相同 hash。


Expected:

兩筆資料皆可查詢。


---

## Remove


Expected：

資料不存在。


---

# 4.2 Course Graph


測試：

## Add Edge


Input:

```text
A -> B
```


Expected:

B 出現在 A adjacency list。

邊的方向以 `prerequisite → dependent course` 為準，測試名稱與 fixture 不得混用相反方向。


---

## BFS


Input:

```text
A-B-C
```


Expected:

```text
A B C
```


---

## Cycle Detection


Input:

```text
A→B→C→A
```


Expected:

Detect Cycle。

其他案例：

- 分支 Graph 可產生合法拓樸順序
- 不相連節點仍包含在 nodes、topologicalOrder 與 stages
- `nodes`、`edges`、`topologicalOrder` 與 `stages` 的 ID 均能互相對應
- stages 同一組可平行學習，所有先修節點都位於依賴節點的較早階段
- 拓樸排序中相鄰節點不必存在直接 edge


---

# 4.3 Heap


測試：

## Insert


新增：

```text
priority 1
priority 3
priority 2
```


Expected:

```text
3
```


---

## Remove


Expected：

維持 Heap Property。

MaxHeap 測試只驗證已具有 priority 的 Alert 排序，不在 Heap 單元測試中驗證 Alert Generator 業務規則。


---

# 5. Algorithm Test


## Merge Sort


測試：

Input:

```text
5,3,8,1
```


Expected:

```text
1,3,5,8
```


測試：

- 空集合
- 單筆資料
- 已排序資料
- 泛型物件清單可依 Comparator 排序
- 相同比較值維持原始相對順序（Stable Sort）
- 不修改呼叫端傳入的陣列或清單


---

## Topological Sort


正常：

```text
A→B→C
```


Expected:

```text
A,B,C
```


異常：

```text
A→B→C→A
```


Expected:

Cycle Exception


---

# 6. Integration Test


測試完整流程：

```text
Database

↓

Repository

↓

Service

↓

Controller

↓

API Response
```


---

# Scenario 1：建立學員


流程：

```text
POST /api/people

↓

Database

↓

GET /api/people
```


確認：

資料存在。


---

# Scenario 2：課程註冊


流程：

```text
Create Person

↓

Create Course

↓

Enroll Course

↓

Query Enrollment
```


確認：

Relationship 正確。


---

# Scenario 3：Graph Analysis


流程：

```text
Course Data

↓

CourseGraph

↓

Topological Sort

↓

Learning Path API
```


確認：

學習路徑正確。

另外確認：

- Graph API 回傳 `nodes`、`edges`、`topologicalOrder`、`stages`
- 每條 edge 的先修節點出現在依賴節點之前
- 分支關係不會被誤判為單一鏈狀路徑
- 新增會形成 Cycle 的關係回傳 `409 CONFLICT`


---

# Scenario 4：人員查詢與統計

流程：

```text
建立 ACTIVE／INACTIVE 人員
↓
依姓名或 Email 搜尋並依狀態篩選
↓
檢查 PageResponse
↓
GET /api/people/statistics
```

確認列表條件只影響列表結果，統計仍反映全域資料。


---

# Scenario 5：警示產生與篩選

流程：

```text
建立不同狀態與開始日期的 Enrollment
↓
Alert Generator
↓
MaxHeap 排序
↓
GET /api/alerts?priority=3
```

確認 Alert Generator 與排序責任分離，且篩選後分頁 metadata 正確。


---

# Scenario 6：Dashboard


流程：

```text
Enrollment Data

↓

Analytics

↓

Dashboard API
```


確認：

統計正確。


---

# 7. Performance Test


目的：

比較不同搜尋方法。


測試資料量：

```text
100

1000

10000
```


---

# Search Comparison


比較：

| Method | Complexity |
|-|-|
| Linear Search | O(n) |
| Binary Search | O(log n) |
| Hash Table | Average O(1) |


---

# Test Result Template


|Data Size|Linear|Binary|Hash|
|-|-|-|-|
|100||||
|1000||||
|10000||||


---

# 8. API Test


工具：

可使用：

- Postman
- Swagger


測試：

## Success Case


例如：

```http
POST /api/courses
```


Expected:

```text
201 CREATED
```


---

## Validation Error


例如：

空名稱：

Expected:

```text
400 BAD REQUEST
```


---

## Not Found


例如：

不存在 ID：

Expected:

```text
404 NOT FOUND
```


---

# 9. Test Documentation


提交內容：

包含：

- Test Case
- Test Result
- Screenshot
- Performance Table


---

# 10. Definition of Done


功能完成條件：

- [x] Code 完成
- [x] Unit Test 通過（85 項）
- [x] API 可正常呼叫
- [x] 文件更新
- [x] Demo Flow 可執行


---

# Testing Goal Summary


最終確保：

```text
Correctness

+

Maintainability

+

Performance Awareness

+

Demo Stability
```
