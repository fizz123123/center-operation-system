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

com.example.center

├── service

├── controller

├── datastructure

├── algorithm

└── integration
```


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


---

## CourseService


測試：

- 建立課程
- 查詢課程
- 重複課程代碼


---

## EnrollmentService


測試：

- 正常註冊
- 重複註冊
- 狀態更新


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


---

# Scenario 4：Dashboard


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

- [ ] Code 完成
- [ ] Unit Test 通過
- [ ] API 可正常呼叫
- [ ] 文件更新
- [ ] Demo Flow 可執行


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