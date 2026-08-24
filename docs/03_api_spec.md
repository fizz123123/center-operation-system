# 中心營運分析系統 - REST API Specification

## 1. API Design Principles

本專案 API 遵循：

- RESTful API Design
- DTO Pattern
- ResponseEntity 統一回傳
- HTTP Status Code 正確使用

本文件描述目前已實作並通過測試的 API 契約；Frontend、測試與後續修改皆應以此文件為準。


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
    "name":"王小明",
    "email":"ming@example.com",
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
"name":"王小明",
"email":"ming@example.com"
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
"name":"王小明",
"email":"ming@example.com",
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
"name":"王小明",
"email":"ming@example.com",
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
"name":"王大明",
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
      "code":"JAVA-001",
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

## 4.2 查詢單一課程

```http
GET /api/courses/{id}
```

成功回傳完整 `CourseResponse`；不存在時回傳 `404 NOT FOUND`。


---

## 4.3 新增課程


Request:

```http
POST /api/courses
```


Request DTO:

```json
{
"code":"JAVA-001",
"name":"Java Basic",
"description":"Java introduction"
}
```


Response:

```json
{
  "id":1,
  "code":"JAVA-001",
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

## 4.4 修改課程


Request:

```http
PUT /api/courses/{id}
```


---

## 4.5 建立課程先修關係


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

## 4.6 移除課程先修關係

```http
DELETE /api/courses/{courseId}/prerequisites/{prerequisiteId}
```

成功回傳 `204 NO CONTENT`。Frontend 不得只在本地移除關係；重新整理後顯示內容必須與 Database 一致。


---

## 4.7 查詢可用先修課程

```http
GET /api/courses/{courseId}/available-prerequisites
```

Response:

```json
[
  {
    "id": 2,
    "code": "JAVA-002",
    "name": "Object Oriented Programming"
  }
]
```

Backend 應排除課程自己、已存在的先修課程，以及新增後會形成 Cycle 的課程。此端點用於改善操作體驗；建立關係的 POST 仍須重新驗證 Cycle，以避免查詢候選後資料發生變動。


---

## 4.8 查詢課程選項

提供不分頁的輕量資料，供學習路徑頁下拉選單及直接關係顯示使用；不以任意大的 `size` 規避分頁限制。

```http
GET /api/courses/options
```

Response:

```json
[
  {
    "id": 1,
    "code": "JAVA-001",
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

- 未傳 `sort`：維持 Repository 依 enrollment id 升序進行資料庫分頁。
- 傳入 `sort=courseName`：Backend 取得完整條件結果，使用自訂 `MergeSort` 依課程名稱排序，
  相同課程名稱再依 enrollment id 升序排列，最後切出每頁 10 筆。
- 課程名稱比較不分英文字母大小寫；`direction` 只改變課程名稱方向，id 的 tie-breaker 固定升序。


---


## 5.2 查詢學員學習紀錄


Request:

```http
GET /api/people/{personId}/enrollments?page=0
GET /api/people/{personId}/enrollments?page=0&sort=courseName&direction=desc
```

排序與分頁規則和 5.1 相同，但完整結果只包含指定 person 的 Enrollment。


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

## 5.3 查詢學員可註冊課程


Request:

```http
GET /api/people/{personId}/available-courses
```

只回傳學員尚未註冊，且所有直接與間接先修課程皆為 `COMPLETED` 的課程。
後端以課程先修 Graph 搭配 DFS 判斷可達的所有先修課程；回傳順序依 course id 升序。

Response:

```json
[
  {
    "id": 2,
    "code": "JAVA-002",
    "name": "Object Oriented Programming",
    "prerequisiteIds": [1]
  }
]
```


---

## 5.4 註冊課程


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

註冊時後端會再次驗證直接與間接先修課程，避免繞過前端直接呼叫 API。
若仍有先修課程尚未完成，回傳 `409 CONFLICT`，且不建立修課紀錄。


---

## 5.5 更新學習狀態


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
"completionRate":85.0
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
      "code": "JAVA-001",
      "name": "Java Basic"
    },
    {
      "id": 2,
      "code": "JAVA-002",
      "name": "Object Oriented Programming"
    }
  ],
  "edges": [
    {
      "fromCourseId": 1,
      "toCourseId": 2
    }
  ],
  "topologicalOrder": [1, 2],
  "stages": [
    [1],
    [2]
  ]
}
```

邊的方向固定為「先修課程 → 依賴它的課程」。`topologicalOrder` 是一組合法順序，不代表唯一答案，也不表示陣列中每兩個相鄰節點存在直接先修關係。`stages` 由 Kahn's Algorithm 每一輪當下所有 indegree 為 0 的課程組成；同一內層陣列可平行學習，後續階段則需等待必要先修階段完成。


用途：

- `nodes` 與 `edges`：顯示真正的 Course Graph 關係
- `topologicalOrder`：保留一組向後相容的合法線性順序
- `stages`：以前端容易理解的階段呈現可平行學習的課程

Frontend 應優先使用 `stages` 分組顯示，不得用連續箭頭或全域流水號暗示不同分支之間存在直接依賴。

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
      "personId":1,
      "personName":"王小明",
      "courseId":3,
      "courseName":"Data Structure",
      "priority":3,
      "message":"學習進度嚴重落後，可能需要立即協助",
      "resolved":false,
      "createdAt":"2026-08-24T10:30:00"
    }
  ],
  "page":0,
  "size":10,
  "totalElements":150,
  "totalPages":15,
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

`sample_data.sql` 會按照 Enrollment 的 status 與 start date，以相同門檻為每筆符合條件的 Enrollment
建立 `[AUTO]` Alert；目前共 150 筆，高、中、低優先權各 50 筆，方便展示 MaxHeap 全域排序與跨頁分頁。
此腳本每次執行都會重置全部業務資料，僅供本機、Demo 與測試環境使用。
透過 API 新增或更新 Enrollment 時，Backend 會同步執行 Alert Generator；自動警示使用 `[AUTO] `
訊息前綴識別。SQL 建立的 150 筆基準警示也使用相同前綴，因此後續 Enrollment 更新時，Runtime Generator
能更新或移除同一組 person/course 的既有警示，不會產生重複資料。

Alert 的人員與課程應由 `personName`、`courseName` 等結構化欄位呈現；`message` 只描述需要關注的狀況，
避免把可能變動的姓名或課程名稱寫死在訊息文字中。

`[AUTO] ` 是 Backend 在未擴充 `source` 欄位前辨識系統警示的內部前綴。API 保留原始 message，Frontend
顯示時會隱藏此前綴，不向使用者呈現技術標記。

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
record PersonCreateRequest(
    String name,
    String email,
    String phone
) {}
```


---

## PersonResponse


```java
record PersonResponse(
    Long id,
    String name,
    String email,
    String phone,
    PersonStatus status
) {}
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
record CourseCreateRequest(
    String code,
    String name,
    String description
) {}
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
    List<Long> topologicalOrder,
    List<List<Long>> stages
) {}
```


---

## EnrollmentCreateRequest


```java
record EnrollmentCreateRequest(
    Long personId,
    Long courseId
) {}
```

其他更新與先修 Request DTO：

```java
record PersonUpdateRequest(String name, String email, String phone, PersonStatus status) {}
record CourseUpdateRequest(String code, String name, String description) {}
record EnrollmentUpdateRequest(EnrollmentStatus status) {}
record PrerequisiteCreateRequest(Long prerequisiteId) {}
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
  "timestamp": "2026-08-25T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/people",
  "fieldErrors": {
    "name": "must not be blank"
  }
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

- [x] Controller 使用 ResponseEntity
- [x] Controller 不回傳 Entity
- [x] 使用 Request / Response DTO
- [x] Service 使用 Interface
- [x] Validation 完成
- [x] Exception 統一處理
- [x] 搜尋、篩選與排序在分頁前由 Backend 完成
- [x] Course Response 與 Graph Response 能表達先修關係
- [x] 已完成 Enrollment 不可再修改
- [x] Alert Generator 與 MaxHeap 責任分離
