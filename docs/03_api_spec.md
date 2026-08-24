# 中心營運分析系統 - REST API Specification

## 1. API Design Principles

本專案 API 遵循：

- RESTful API Design
- DTO Pattern
- ResponseEntity 統一回傳
- HTTP Status Code 正確使用

本文件描述整合測試會議確認後的目標 API 契約。若目前程式尚未支援本文件新增的查詢參數、DTO 或端點，應視為待實作項目，通過 `docs/06_test_strategy.md` 對應測試後才算完成。


API Flow:

```text
Frontend

↓

Controller

↓

Service Interface

↓

Service Implementation

↓

Repository

↓

Database
```

---

# 2. Common Rules


## Base URL

```text
/api
```


---

## Response Format

所有 Controller 必須：

```java
ResponseEntity<T>
```


禁止：

```java
return Entity;
```


## Pagination Rules

人員、課程、修課紀錄與警示清單採用分頁查詢：

```http
GET /api/{resource}?page=0
```

- `page` 從 0 開始，未提供時預設為 0
- 每頁固定 10 筆，不開放前端修改 page size
- 負數 page 回傳 `400 BAD REQUEST`
- 搜尋、篩選與排序必須先在 Backend 套用，再對完整結果分頁；Frontend 不得只處理目前載入的 10 筆

共同回應格式：

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 200,
  "totalPages": 20,
  "first": true,
  "last": false
}
```


---

# 3. Person API


## 3.1 查詢所有人員


### Request

```http
GET /api/people?page=0
GET /api/people?page=0&status=ACTIVE
GET /api/people?page=0&search=王
GET /api/people?page=0&status=ACTIVE&search=wang@example.com
```

Query Parameters:

| Parameter | Required | Description |
|-|-|-|
| `page` | No | 從 0 開始，預設 0 |
| `status` | No | `ACTIVE` 或 `INACTIVE`；全部資料時不傳 |
| `search` | No | 姓名或 Email 的部分比對，英文忽略大小寫 |

切換狀態或搜尋文字時，Frontend 必須將頁碼重設為 0；搜尋欄建議使用 300–500ms debounce。


### Response

```json
{
  "content": [
    {
    "id":1,
    "name":"Brian",
    "email":"test@example.com",
    "phone":"0912345678",
    "status":"ACTIVE"
    }
  ],
  "page":0,
  "size":10,
  "totalElements":200,
  "totalPages":20,
  "first":true,
  "last":false
}
```


HTTP Status:

```
200 OK
```


---

## 3.2 查詢單一人員


### Request

```http
GET /api/people/{id}
```


Example:

```http
GET /api/people/1
```


Response:

```json
{
"id":1,
"name":"Brian",
"email":"test@example.com"
}
```


Status:

```
200 OK
```


不存在：

```
404 NOT FOUND
```


---

## 3.3 新增人員


### Request

```http
POST /api/people
```


Request Body:

```json
{
"name":"Brian",
"email":"test@example.com",
"phone":"0912345678"
}
```


DTO:

```java
PersonCreateRequest
```


Response:

```json
{
"id":1,
"name":"Brian",
"email":"test@example.com",
"status":"ACTIVE"
}
```


Status:

```
201 CREATED
```


---

## 3.4 修改人員


### Request

```http
PUT /api/people/{id}
```


Request:

```json
{
"name":"Brian Lin",
"phone":"0987654321"
}
```


Status:

```
200 OK
```


---

## 3.5 查詢人員統計

統計值永遠以所有人員為母體，不受列表目前的搜尋、篩選或頁碼影響。

Request:

```http
GET /api/people/statistics
```

Response:

```json
{
  "total": 200,
  "active": 180,
  "inactive": 20
}
```

必須滿足 `total = active + inactive`。


---

# 4. Course API


## 4.1 查詢課程列表


Request:

```http
GET /api/courses?page=0
GET /api/courses?page=0&search=java
```

`search` 對課程代碼或名稱做部分比對，英文忽略大小寫。搜尋必須由 Backend 在分頁前完成。


Response:

```json
{
  "content": [
    {
      "id":1,
      "code":"JAVA01",
      "name":"Java Basic",
      "description":"Java introduction",
      "prerequisiteIds":[]
    }
  ],
  "page":0,
  "size":10,
  "totalElements":20,
  "totalPages":2,
  "first":true,
  "last":false
}
```


---

## 4.2 新增課程


Request:

```http
POST /api/courses
```


Request DTO:

```json
{
"code":"JAVA01",
"name":"Java Basic",
"description":"Java introduction"
}
```


Response:

```json
{
  "id":1,
  "code":"JAVA01",
  "name":"Java Basic",
  "description":"Java introduction",
  "prerequisiteIds":[]
}
```


Status:

```
201 CREATED
```


---

## 4.3 修改課程


Request:

```http
PUT /api/courses/{id}
```


---

## 4.4 建立課程先修關係


用途：

建立 Course Graph。


Request:

```http
POST /api/courses/{courseId}/prerequisites
```


Example:

```http
POST /api/courses/3/prerequisites
```


Body:

```json
{
"prerequisiteId":2
}
```


代表：

```text
Course 3

需要先完成

Course 2
```

Backend 必須再次驗證：

- 不得將自己設為先修課程
- 不得建立重複關係
- 新增後不得形成 Cycle

成功回傳 `201 CREATED`；形成 Cycle 時回傳 `409 CONFLICT`。


---

## 4.5 移除課程先修關係

```http
DELETE /api/courses/{courseId}/prerequisites/{prerequisiteId}
```

成功回傳 `204 NO CONTENT`。Frontend 不得只在本地移除關係；重新整理後顯示內容必須與 Database 一致。


---

## 4.6 查詢可用先修課程

```http
GET /api/courses/{courseId}/available-prerequisites
```

Response:

```json
[
  {
    "id": 2,
    "code": "CS102",
    "name": "Object Oriented Programming"
  }
]
```

Backend 應排除課程自己、已存在的先修課程，以及新增後會形成 Cycle 的課程。此端點用於改善操作體驗；建立關係的 POST 仍須重新驗證 Cycle，以避免查詢候選後資料發生變動。


---

## 4.7 查詢課程選項

提供不分頁的輕量資料，供學習路徑頁下拉選單及直接關係顯示使用；不以任意大的 `size` 規避分頁限制。

```http
GET /api/courses/options
```

Response:

```json
[
  {
    "id": 1,
    "code": "JAVA01",
    "name": "Java Basic",
    "prerequisiteIds": []
  }
]
```


---

# 5. Enrollment API


## 5.1 查詢全部修課紀錄


Request:

```http
GET /api/enrollments?page=0
GET /api/enrollments?page=0&sort=courseName&direction=asc
```

Response 使用共同分頁格式，`content` 為 `EnrollmentResponse`。

允許的排序參數：

| Parameter | Value |
|-|-|
| `sort` | 目前只允許 `courseName` |
| `direction` | `asc` 或 `desc` |

排序必須先套用於完整查詢結果，再切出指定頁面；不允許 Frontend 只排序目前頁面的 10 筆資料。切換排序方向時，Frontend 必須回到第 0 頁。


---


## 5.2 查詢學員學習紀錄


Request:

```http
GET /api/people/{personId}/enrollments?page=0
GET /api/people/{personId}/enrollments?page=0&sort=courseName&direction=desc
```


Response:

```json
{
  "content": [
    {
      "courseName":"Java Basic",
      "status":"COMPLETED"
    }
  ],
  "page":0,
  "size":10,
  "totalElements":3,
  "totalPages":1,
  "first":true,
  "last":true
}
```


---

## 5.3 註冊課程


Request:

```http
POST /api/enrollments
```


Request:

```json
{
"personId":1,
"courseId":2
}
```


Response:

```json
{
"id":1,
"status":"NOT_STARTED"
}
```


Status:

```
201 CREATED
```


---

## 5.4 更新學習狀態


Request:

```http
PUT /api/enrollments/{id}
```


Body:

```json
{
"status":"COMPLETED"
}
```

允許的狀態轉換：

```text
NOT_STARTED → IN_PROGRESS
NOT_STARTED → COMPLETED
IN_PROGRESS → COMPLETED
COMPLETED → 無後續狀態
```

- 不允許狀態倒退
- `COMPLETED` 是終止狀態，不可再次修改
- Frontend 對已完成紀錄應停用狀態選單並顯示唯讀提示
- Backend 必須保留驗證，不能只依賴 Frontend
- 非法轉換回傳 `409 CONFLICT`


---

# 6. Dashboard API


## 6.1 Dashboard Summary


Request:

```http
GET /api/dashboard
```


Response:

```json
{
"totalPeople":200,
"totalCourses":20,
"totalEnrollments":1000,
"completionRate":45.0
}
```


---

# 7. Course Graph API


## 7.1 取得學習路徑


Request:

```http
GET /api/courses/learning-path
```


Response:

```json
{
  "nodes": [
    {
      "id": 1,
      "code": "JAVA01",
      "name": "Java Basic"
    },
    {
      "id": 2,
      "code": "OOP01",
      "name": "Object Oriented Programming"
    }
  ],
  "edges": [
    {
      "fromCourseId": 1,
      "toCourseId": 2
    }
  ],
  "topologicalOrder": [1, 2]
}
```

邊的方向固定為「先修課程 → 依賴它的課程」。`topologicalOrder` 是一組合法順序，不代表唯一答案，也不表示陣列中每兩個相鄰節點存在直接先修關係。


用途：

- `nodes` 與 `edges`：顯示真正的 Course Graph 關係
- `topologicalOrder`：顯示一組可行的建議修課順序

若 Frontend 以線性清單顯示拓樸排序，標題應使用「一組可行的建議修課順序」，不得用連續箭頭暗示相鄰課程具有直接依賴。

BFS 與 DFS 由演算法模組獨立提供及測試；本端點不宣稱回傳 BFS／DFS 結果。


---

# 8. Alert API


## 8.1 查詢警示


Request:

```http
GET /api/alerts?page=0
GET /api/alerts?page=0&priority=3
```

Query Parameters:

| Parameter | Required | Description |
|-|-|-|
| `page` | No | 從 0 開始，預設 0 |
| `priority` | No | `3`、`2` 或 `1`；全部警示時不傳 |

篩選必須在分頁前由 Backend 完成，`totalElements` 與 `totalPages` 應反映篩選後結果。非法 priority 回傳 `400 BAD REQUEST`。


Response:

```json
{
  "content": [
    {
      "id":1,
      "priority":3,
      "message":"Course unfinished",
      "resolved":false
    }
  ],
  "page":0,
  "size":10,
  "totalElements":30,
  "totalPages":3,
  "first":true,
  "last":false
}
```


Priority:

```text
3 HIGH

2 MEDIUM

1 LOW
```

Priority 是 Alert 建立時保存的分類值。Backend 先依 priority 篩選完整結果，再交由自訂 `MaxHeap` 依
`priority DESC, created_at ASC, id ASC` 排序，最後才切出每頁 10 筆資料：

- `3 HIGH`：需立即處理
- `2 MEDIUM`：需要追蹤
- `1 LOW`：一般提醒

`sample_data.sql` 的 30 筆 Alert 是獨立的 Demo seed data，message 與 priority 均預先指定。
透過 API 新增或更新 Enrollment 時，Backend 會同步執行 Alert Generator；自動警示使用 `[AUTO] `
訊息前綴識別，不會覆寫 Demo seed data。

MVP 的 Alert Generator 規則：

| Enrollment condition | Priority |
|-|-:|
| `IN_PROGRESS` 且開始已滿 90 天 | 3 |
| `IN_PROGRESS` 且開始已滿 30 天、未滿 90 天 | 2 |
| `IN_PROGRESS` 且開始未滿 30 天 | 不產生警示 |
| `NOT_STARTED` | 1 |
| `COMPLETED` | 不產生警示 |

同一組 person 與 course 最多由 Generator 維護一筆 `[AUTO]` 警示。狀態或經過天數改變時會更新該警示；
條件變成「不產生警示」時會移除該筆自動警示。Alert Generator 負責決定是否建立警示與 priority；
自訂 MaxHeap 只負責排列已判定的 Alert，不負責業務判斷。


---

# 9. DTO Definition


## PersonCreateRequest


```java
class PersonCreateRequest {

String name;

String email;

String phone;

}
```


---

## PersonResponse


```java
class PersonResponse {

Long id;

String name;

String email;

String phone;

String status;

}
```


---

## PersonStatisticsResponse

```java
record PersonStatisticsResponse(
    long total,
    long active,
    long inactive
) {}
```


---

## CourseCreateRequest


```java
class CourseCreateRequest {

String code;

String name;

String description;

}
```


---

## CourseResponse

```java
record CourseResponse(
    Long id,
    String code,
    String name,
    String description,
    List<Long> prerequisiteIds
) {}
```


---

## CourseGraphResponse

```java
record CourseGraphResponse(
    List<CourseNodeResponse> nodes,
    List<CourseEdgeResponse> edges,
    List<Long> topologicalOrder
) {}
```


---

## EnrollmentCreateRequest


```java
class EnrollmentCreateRequest {

Long personId;

Long courseId;

}
```


---

# 10. Validation Rules


使用：

```java
jakarta.validation
```


Example:

```java
@NotBlank
private String name;


@Email
private String email;
```


---

# 11. Error Response


統一格式：

```json
{
"timestamp":"2026-08-20T12:00:00",
"status":400,
"message":"Email already exists"
}
```


由：

```java
@RestControllerAdvice
```

統一處理。


---

# 12. API Checklist


Backend 完成後確認：

- [ ] Controller 使用 ResponseEntity
- [ ] Controller 不回傳 Entity
- [ ] 使用 Request / Response DTO
- [ ] Service 使用 Interface
- [ ] Validation 完成
- [ ] Exception 統一處理
- [ ] 搜尋、篩選與排序在分頁前由 Backend 完成
- [ ] Course Response 與 Graph Response 能表達先修關係
- [ ] 已完成 Enrollment 不可再修改
- [ ] Alert Generator 與 MaxHeap 責任分離
