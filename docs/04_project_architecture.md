# 中心營運分析系統 - Project Architecture

## 1. Architecture Overview

本專案採用：

> Layered Architecture + DTO Pattern


主要目標：

- 降低模組耦合
- 提高可測試性
- 遵循 Spring Boot 現代開發習慣


---

# 2. Package Structure


```text
com.example.center

├── controller
│
├── service
│   └── impl
│
├── repository
│
├── entity
│
├── dto
│   ├── request
│   └── response
│
├── mapper
│
├── datastructure
│
├── algorithm
│
├── analytics
│
├── exception
│
└── config
```


---

# 3. Dependency Rule


系統依賴方向：


```text
Controller

↓

Service Interface

↓

ServiceImpl

↓

Repository

↓

Database
```


禁止：

```text
Controller

↓

ServiceImpl
```


原因：

Controller 不應知道 Service 實作。


---

# 4. Controller Layer


位置：

```
controller/
```


責任：

- 接收 Request
- 呼叫 Service
- 回傳 ResponseEntity DTO


Example:


```java
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {


private final CourseService courseService;


}
```


注意：

依賴：

```java
CourseService
```


不是：

```java
CourseServiceImpl
```


---

# 5. Service Layer


## Interface


位置：

```
service/
```


Example:

```java
public interface CourseService {


PageResponse<CourseResponse> getCourses(int page);


CourseResponse createCourse(
CourseCreateRequest request
);


}
```


---

## Implementation


位置：

```
service/impl
```


Example:


```java
@Service
@RequiredArgsConstructor
public class CourseServiceImpl
implements CourseService {


private final CourseRepository repository;


}
```


---

# 6. Repository Layer


位置：

```
repository/
```


責任：

- Database Access
- Query


Example:

```java
public interface CourseRepository
extends JpaRepository<Course, Long>{


}
```


禁止：

- Business Logic
- DTO Mapping


---

# 7. Entity Layer


位置：

```
entity/
```


用途：

Database Mapping。


Example:

```java
@Entity
public class Course {


@Id
@GeneratedValue
private Long id;


}
```


禁止：

- 直接回傳 API
- 包含 Controller 邏輯
- 包含 Analytics


---

# 8. DTO Pattern


資料流：

```text
Request DTO

↓

Controller

↓

Service

↓

Entity

↓

Repository


Database


Repository

↓

Entity

↓

Mapper

↓

Response DTO

↓

Controller

↓

Frontend
```


---

# 9. Mapper Layer


位置：

```
mapper/
```


責任：

Entity 與 DTO 轉換。


Example:

```java
@Component
public class CourseMapper {


public CourseResponse
toResponse(Course entity){

}


}
```


避免：

Controller 出現：

```java
dto.setName(entity.getName());
```


---

# 10. Data Structure Module


位置：

```
datastructure/
```


包含：


```
CustomHashTable

CourseGraph

MaxHeap
```


規則：

- 純 Java Class
- 不依賴 Spring
- 不直接存取 Database


---

# 11. Algorithm Module


位置：

```
algorithm/
```


包含：

```
BFS

DFS

TopologicalSort

MergeSort
```


規則：

- Stateless
- 可獨立測試
- 不依賴 Controller / Repository


---

# 12. Analytics Module


位置：

```
analytics/
```


負責：

- Dashboard 計算
- Completion Rate
- Alert Generator


流程：

```text
Repository

↓

Analytics

↓

DTO

↓

Frontend
```


---

# 13. Exception Handling


位置：

```
exception/
```


包含：

```
GlobalExceptionHandler

ResourceNotFoundException

DuplicateResourceException
```


Controller 不自行處理：

```java
try {

}
catch(Exception e){

}
```


統一：

```java
@RestControllerAdvice
```


---

# 14. Spring Boot Best Practice


## Constructor Injection


使用：

```java
@RequiredArgsConstructor
```


禁止：

```java
@Autowired
private CourseService service;
```


---

## Configuration


敏感資訊：

不可：

```java
String password="123456";
```


使用：

```yaml
application.yml
```


---

## Transaction


跨資料更新：

使用：

```java
@Transactional
```


例如：

Enrollment 建立：

```text
Create Enrollment

↓

Update Statistics

↓

Create Alert
```


失敗：

Rollback


---

# 15. Frontend Architecture


前端放：

```
src/main/resources/static
```


原因：

- Spring Boot 自動提供
- 單體部署
- 不需處理 CORS


結構：

```text
static/

├── index.html

├── pages/

├── js/

├── css/

└── assets/
```


---

# 16. Final Development Rule


所有開發必須遵守：

## Controller

只負責：

HTTP


## Service

只負責：

Business Logic


## Repository

只負責：

Database


## DTO

只負責：

API Data Transfer


## Entity

只負責：

Database Mapping


## Data Structure

只負責：

Algorithm Data Processing


---

# Architecture Goal

最終希望達成：

```text
Clean Code

+

Low Coupling

+

Testable

+

Maintainable

+

符合現代 Spring Boot 開發習慣
```
