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


PageResponse<CourseResponse> getCourses(
int page,
String search
);


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


## 分頁查詢責任

人員、課程、修課紀錄與警示的搜尋、篩選及排序由 Service／Repository 在分頁前完成。Frontend 只傳入查詢條件並顯示 `PageResponse`，不得只處理目前載入的 10 筆資料。

```text
Query Parameters
↓
Controller
↓
Service 驗證允許的條件
↓
Repository／Algorithm 對完整結果查詢、篩選與排序
↓
PageResponse
```

人員統計使用專屬 `PersonStatisticsResponse`，不把人員特有欄位加入共用的 `PageResponse<T>`。

Enrollment 未指定 `sort` 時維持 Repository 分頁；指定 `sort=courseName` 時，Service 先取得全部符合條件的
Enrollment，交由自訂 `MergeSort` 排序後再建立 `PageResponse`。此路徑用於展示演算法實際整合，API 契約不變。

課程先修關係的可用候選由 Backend Course Graph 判斷。Frontend 可先隱藏會形成 Cycle 的選項，但建立關係時 Service 仍須再次驗證。


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

`CourseGraph` 的邊統一定義為「先修課程 → 依賴它的課程」，並提供節點、邊與拓樸排序結果。拓樸排序只保證先修課程出現在依賴課程之前，不代表結果中相鄰節點有直接關係。

`MaxHeap` 只負責排列已具有 priority 的 Alert，不得包含「什麼情況應產生警示」的業務規則。


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

`MergeSort` 提供容易展示的 `int[]` 版本，以及供應用層排序物件的泛型 `List<T> + Comparator` 版本。
`EnrollmentService` 只在使用者明確指定 `sort=courseName` 時呼叫泛型版本；未指定排序時仍使用資料庫分頁。


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

Alert Generator 的 MVP 規則以 Enrollment status 與 start date 判斷是否產生警示及 priority；完成判定後再交給 MaxHeap 排序。兩者責任分離：

```text
Enrollment API 寫入流程
├─ Enrollment Repository（保存修課狀態）
└─ Alert Generator（同步建立／更新／移除 [AUTO] Alert）
   └─ Alert Repository

Alert API 查詢流程
Alert Repository
└─ MaxHeap（priority DESC、createdAt ASC、id ASC）
   └─ 分頁（每頁 10 筆）
      └─ Alert Response DTO
```

Generator 在 Enrollment 建立或狀態更新成功後執行，不在 `GET /api/alerts` 時掃描或修改資料，
因此查詢端點保持唯讀。`sample_data.sql` 的 Demo Alert 沒有 `[AUTO]` 前綴，Generator 不會修改它們。


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


前端原始碼放：

```
frontend/
```

Vite 建置輸出放：

```
src/main/resources/static/
```

此配置的原因：

- 開發時由 Vite dev server 將 `/api` proxy 到 Spring Boot
- 部署時由 Spring Boot 提供編譯後靜態檔案
- 單體部署
- 開發 proxy 與正式環境同源部署均不需額外 CORS 設定


Frontend 修改後必須重新執行 Vite build，才能更新 `static/` 內的部署成品。原始碼與建置成品不可混為同一層。

建置成品結構：

```text
static/

├── index.html
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
