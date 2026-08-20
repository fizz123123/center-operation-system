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


---

# 3. Person API


## 3.1 查詢所有人員


### Request

```http
GET /api/people
```


### Response

```json
[
  {
    "id":1,
    "name":"Brian",
    "email":"test@example.com",
    "phone":"0912345678",
    "status":"ACTIVE"
  }
]
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
GET /api/courses
```


Response:

```json
[
 {
  "id":1,
  "code":"JAVA01",
  "name":"Java Basic"
 }
]
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


## 5.1 查詢學員學習紀錄


Request:

```http
GET /api/people/{personId}/enrollments
```


Response:

```json
[
 {
  "courseName":"Java Basic",
  "status":"COMPLETED"
 }
]
```


---

## 5.2 註冊課程


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

## 5.3 更新學習狀態


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
"totalPeople":50,
"totalCourses":15,
"totalEnrollments":200,
"completionRate":75.5
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
GET /api/alerts
```


Response:

```json
[
{
"id":1,
"priority":3,
"message":"Course unfinished"
}
]
```


Priority:

```text
3 HIGH

2 MEDIUM

1 LOW
```


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