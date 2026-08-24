# 中心營運分析系統 - Project Scope

## 1. 專案概述

本專題為「資料結構與演算法」課程期末專題。

原始需求為建立一套中心營運管理系統，整合人員管理、課程管理、資料分析與通知功能。

考量：

- 三人小組
- 有限開發時間
- 課程核心為資料結構與演算法
- 需要展示資料庫與系統整合能力

本組將系統範圍縮小為：

> 中心課程營運分析系統

主要提供：

- 學員資料管理
- 課程資料管理
- 學習進度管理
- 課程關係分析
- 基礎營運統計

並透過自訂資料結構與演算法完成資料分析功能。

---

# 2. 專案目標

本系統希望達成：

## 系統功能目標

- 建立學員與課程管理流程
- 保存學員學習紀錄
- 分析課程先修關係
- 提供課程學習路徑
- 提供營運統計資訊
- 產生課程相關警示


## 課程學習目標

透過專案實作：

- 關聯式資料庫設計
- Spring Boot Web 開發
- Spring Data JPA 使用
- REST API 設計
- 自訂資料結構
- 演算法應用
- 模組化軟體設計

---

# 3. 系統範圍

## 3.1 包含功能

---

# Module 1：人員管理

## 功能

- 新增人員
- 分頁查詢人員列表
- 依姓名或 Email 搜尋人員
- 依在學／停用狀態篩選人員
- 查詢總人數、在學人數與停用人數
- 查詢單一人員
- 修改人員資料


## 管理資料

- 姓名
- Email
- 電話
- 狀態


---

# Module 2：課程管理

## 功能

- 新增課程
- 分頁查詢課程
- 依課程代碼或名稱搜尋課程
- 修改課程
- 建立課程先修關係
- 移除課程先修關係
- 只提供不會形成 Cycle 的可用先修課程選項


## 管理資料

- 課程代碼
- 課程名稱
- 課程描述
- 先修課程


---

# Module 3：學習進度管理

## 功能

- 學員註冊課程
- 只顯示所有直接與間接先修皆已完成的可註冊課程
- Backend 在註冊時再次驗證先修條件
- 更新課程狀態
- 分頁查詢學習紀錄
- 依課程名稱升冪或降冪排列學習紀錄


## 學習狀態

```text
NOT_STARTED

IN_PROGRESS

COMPLETED
```

狀態只允許向前轉換，可直接將尚未開始的紀錄標記為已完成：

```text
NOT_STARTED → IN_PROGRESS
NOT_STARTED → COMPLETED
IN_PROGRESS → COMPLETED
COMPLETED → 無後續狀態
```

`COMPLETED` 是終止狀態，不可再修改。前端應將操作設為唯讀，Backend 仍須保留驗證以維護資料一致性。

---

# Module 4：Dashboard 分析

提供：

- 總人員數
- 總課程數
- 總註冊數
- 課程完成率
- 依優先級排列與篩選的警示


---

# Module 5：資料結構與演算法

## Custom Hash Table

用途：

快速查詢資料。


範例：

```text
personId → Person
```


功能：

- put
- get
- remove
- containsKey


---

## Course Graph

用途：

表示課程先修關係。


範例：

```text
Java ─→ OOP ─→ Data Structure ─→ Algorithm
  │
  └─→ Spring Boot ─┐
Database ──────────┴─→ Backend ─→ Cloud
```


實作：

- adjacency list
- BFS
- DFS
- Topological Sort


---

## Heap

用途：

管理課程警示優先級。

`MaxHeap` 只負責依既有 priority 排定處理順序，不負責決定警示內容或 priority。警示產生與優先級判定由 Alert Generator 的業務規則負責。


功能：

- insert
- peek
- remove


---

## Sorting

實作：

Merge Sort


用途：

課程完成率、熱門程度或學習紀錄課程名稱排序。若用於分頁列表，必須先對完整查詢結果排序再切頁，不能只排序前端目前載入的一頁。

---

# 4. 不包含範圍

為控制開發時間，下列功能不實作：

- 聯絡紀錄管理
- 書籍庫存管理
- 財務管理
- 活動管理
- AI 預測
- 機器學習模型
- 複雜權限系統
- 微服務架構
- Docker 部署


---

# 5. 技術架構

## Backend

使用：

- Java 17+
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL


架構：

```text
Controller

↓

Service

↓

Repository

↓

Database
```

---

## Frontend

採用：

Vue 3 + Vite，並由 Spring Boot 提供正式建置成品。


位置：

```text
frontend/                         # Vue/Vite 原始碼
src/main/resources/static/       # Vite 編譯後成品
```


技術：

- Vue 3
- Vite
- JavaScript
- CSS


原因：

- 降低部署複雜度
- 開發時以 Vite proxy、部署時以同源靜態資源避免 CORS 問題
- 適合短期專題開發


---

# 6. Demo 流程

展示順序：

## Step 1

新增學員


## Step 2

新增課程


## Step 3

建立課程先修關係


## Step 4

學員註冊課程


## Step 5

更新學習狀態


## Step 6

查看 Dashboard


## Step 7

展示 Graph 學習路徑


## Step 8

展示 Heap 警示排序


---

# 7. 開發原則

本專案遵循：

- Layered Architecture
- DTO Pattern
- REST API Design
- Separation of Responsibility


禁止：

- Controller 直接操作 Repository
- Controller 直接依賴 ServiceImpl
- Entity 直接回傳給前端
- SQL 寫在 Controller
- 資料結構與 Controller 混合


---

# 8. 預期成果

完成後系統具備：

- Web 操作介面
- REST API
- MySQL 資料保存
- 自訂資料結構
- 演算法分析
- 測試紀錄
- 專題展示流程
