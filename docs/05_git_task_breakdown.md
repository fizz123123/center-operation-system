# 中心營運分析系統 - Git Task Breakdown

## 1. 開發管理原則

本專案採用 Git Flow 簡化版：

```text
main

↓

develop

↓

feature branch
```


Branch 用途：

| Branch | 用途 |
|-|-|
| main | 最終穩定版本 |
| develop | 整合開發版本 |
| feature/* | 個人功能開發 |


---

# 2. Repository Structure


```text
center-operation-system/

├── frontend/                         # Vue/Vite 原始碼
├── src/main/java/                    # Spring Boot Backend
├── src/main/resources/static/        # Vite 編譯後成品
├── database/
├── docs/
└── pom.xml
```


實際採用：

Vue/Vite 原始碼與 Spring Boot Backend 位於同一 Repository。開發時使用 Vite proxy 呼叫 `/api`；正式建置輸出至：

```text
src/main/resources/static/
```

由 Spring Boot 以同源方式提供前端成品。


---

# 3. Team Responsibility


## Member A：Backend Developer

主要負責：

- Spring Boot Backend
- Database Design
- Entity
- Repository
- Service
- Controller
- API


負責模組：

```text
backend/

entity

repository

service

controller
```


---

## Member B：Frontend Developer


主要負責：

- Web Interface
- HTML
- CSS
- JavaScript
- API 呼叫
- UI 整合


負責：

```text
resources/static

pages/

js/

css/
```


---

## Member C：Integration & Testing


主要負責：

- 前後端整合
- API 測試
- 測試資料
- Demo Flow
- 文件整理


另外協助：

- Algorithm Module
- Data Structure Module


---

# 4. Shared Responsibility


以下功能不屬於單一成員：

## Data Structure

共同完成：

```text
datastructure/

CustomHashTable

CourseGraph

MaxHeap
```


---

## Algorithm

共同完成：

```text
algorithm/

BFS

DFS

TopologicalSort

MergeSort
```


原因：

口頭報告時每位組員都需要理解核心程式。


---

# 5. Git Branch Naming


格式：

```text
feature/{name}-{function}
```


範例：

Backend:

```text
feature/backend-person-api
```


Frontend:

```text
feature/frontend-dashboard
```


Integration:

```text
feature/api-integration-test
```


---

# 6. Commit Convention


採用 Conventional Commit。


格式：

```text
type: description
```


---

## Feature


新增功能：

```text
feat: add person entity
```


---

## Fix


修正：

```text
fix: fix enrollment validation
```


---

## Refactor


重構：

```text
refactor: improve service layer structure
```


---

## Docs


文件：

```text
docs: update api specification
```


---

## Test


測試：

```text
test: add graph unit test
```


---

# 7. Pull Request Rule


合併前確認：

- 程式可以正常 Build
- API 文件同步更新
- 沒有直接操作 Repository 的 Controller
- DTO 使用正確
- Unit Test 通過


---

# 8. Development Milestone


# Day 1：Project Setup


完成：

Backend：

- Spring Boot 建立
- MySQL 連線
- Package 建立


Database：

- ERD 確認
- Schema 建立


Frontend：

- Static Page 架構


完成條件：

```text
Application 啟動成功
Database Connection 成功
```


---

# Day 2：Core CRUD


Backend：

完成：

- Person CRUD
- Course CRUD
- Enrollment CRUD


Frontend：

完成：

- Person Page
- Course Page


Integration：

完成：

- API 串接


---

# Day 3：Algorithm Integration


完成：

## Course Graph

- Graph 建立
- BFS
- DFS
- Topological Sort


## Hash Table

- Insert
- Search
- Remove


Frontend：

完成：

- Learning Path Page


---

# Day 4：Analytics & Enhancement


完成：

Dashboard：

- Total People
- Total Course
- Total Enrollment
- Completion Rate


完成：

- Heap
- Alert


---

# Day 5：Testing & Presentation


完成：

- Bug Fix
- Test
- Demo Script
- Presentation


---

# 9. Git Issue List


## Backend


### Issue #1

建立 Entity Model


內容：

- Person
- Course
- Enrollment
- Alert


---

### Issue #2

建立 Repository


---

### Issue #3

建立 Service Interface


要求：

Controller 不直接依賴 ServiceImpl。


---

### Issue #4

建立 REST API


要求：

所有 API：

```java
ResponseEntity<DTO>
```


---

# Frontend


### Issue #5

建立 Layout


包含：

- Navbar
- Sidebar


---

### Issue #6

完成 Person Page


功能：

- List
- Create
- Update


---

### Issue #7

完成 Course Page


功能：

- List
- Create
- Prerequisite


---

### Issue #8

完成 Dashboard


---

# Integration


### Issue #9

API Testing


確認：

- Request
- Response
- Error Case


---

### Issue #10

Demo Flow


完成：

```text
Create Person

↓

Create Course

↓

Create Enrollment

↓

Graph Analysis

↓

Dashboard
```


---

# 10. Final Checklist


## Backend

- [ ] Controller 使用 Service Interface
- [ ] DTO Pattern 完成
- [ ] Entity 不直接暴露
- [ ] Exception Handler 完成
- [ ] Transaction 完成


## Frontend

- [ ] 所有頁面可操作
- [ ] API 正確串接
- [ ] Error Message 顯示


## Algorithm

- [ ] Hash Table
- [ ] Graph
- [ ] Heap
- [ ] Sort


## Project

- [ ] README
- [ ] Database Script
- [ ] Demo
- [ ] Presentation
