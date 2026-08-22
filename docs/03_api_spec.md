# 中心營運分析系統 - REST API Specification

## 1. API Design Principles

本專案 API 遵循：

- RESTful API Design
- DTO Pattern
- ResponseEntity 統一回傳
- HTTP Status Code 正確使用


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
```


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

# 4. Course API


## 4.1 查詢課程列表


Request:

```http
GET /api/courses?page=0
```


Response:

```json
{
  "content": [
    {
      "id":1,
      "code":"JAVA01",
      "name":"Java Basic"
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
"name":"Java Basic"
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


---

# 5. Enrollment API


## 5.1 查詢全部修課紀錄


Request:

```http
GET /api/enrollments?page=0
```

Response 使用共同分頁格式，`content` 為 `EnrollmentResponse`。


---


## 5.2 查詢學員學習紀錄


Request:

```http
GET /api/people/{personId}/enrollments?page=0
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
[
"Java Basic",
"OOP",
"Data Structure",
"Algorithm"
]
```


用途：

展示：

- Topological Sort
- BFS / DFS


---

# 8. Alert API


## 8.1 查詢警示


Request:

```http
GET /api/alerts?page=0
```


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

目前 Priority 是 Alert 建立時保存的分類值，Backend 依 `priority DESC, created_at ASC` 查詢：

- `3 HIGH`：需立即處理
- `2 MEDIUM`：需要追蹤
- `1 LOW`：一般提醒

目前 MVP 尚未根據期限自動計算 Priority；若後續加入 Alert Generator，應由業務規則產生 priority，再交由自訂 MaxHeap 展示優先排程。


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

## CourseCreateRequest


```java
class CourseCreateRequest {

String code;

String name;

String description;

}
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
