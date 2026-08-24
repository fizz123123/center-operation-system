# Center Operation System

中心營運分析系統 MVP，使用 Spring Boot、MySQL 與 Vue 3 建置。系統提供人員、課程、修課紀錄、
課程先修圖、學習路徑、Dashboard 與警示優先級管理。

## 環境需求

- Java 21+
- Docker Desktop（MySQL 8.4）
- Node.js 與 npm（只有修改或重建 Frontend 時需要）

## 啟動方式

1. 啟動 MySQL：

   ```bash
   docker compose up -d mysql
   ```

   第一次建立 Docker volume 時，MySQL 會自動依序執行 `database/schema.sql` 與
   `database/sample_data.sql`。

2. 啟動 Spring Boot：

   macOS／Linux：

   ```bash
   ./mvnw spring-boot:run
   ```

   Windows：

   ```bat
   mvnw.cmd spring-boot:run
   ```

3. 開啟 <http://localhost:8080>。

Frontend 的 Vite 建置成品已放在 `src/main/resources/static/`，正常啟動不需要另外執行 Frontend dev server。

## 重設 Demo 資料

`database/sample_data.sql` 只適用於本機、Demo 與測試環境。完整執行會清除現有業務資料並重建固定基準：

| 資料 | 筆數 |
|-|-:|
| Person | 200 |
| Course | 20 |
| Enrollment | 1,000 |
| Course Prerequisite | 26 |
| Alert | 150 |

所有 Demo 姓名都是不重複的三字中文姓名；全部 Enrollment 都符合直接及間接先修關係；Alert 高、中、低
優先權各 50 筆。

macOS／Linux 可執行：

```bash
docker exec -i center-operation-mysql mysql --default-character-set=utf8mb4 -uroot -proot < database/sample_data.sql
```

## 測試與 Frontend 建置

執行完整後端測試：

```bash
./mvnw test
```

修改 Frontend 後重新產生 Spring Boot 靜態檔案：

```bash
cd frontend
npm install
npm run build
```

## 自訂資料結構與演算法

- `CustomHashTable`：CourseGraph 內部索引。
- `CourseGraph`：保存課程先修有向圖。
- `MaxHeap`：警示全域優先級排序後再分頁。
- `DFS`：Cycle 檢查，以及學員直接／間接先修完成資格判斷。
- `TopologicalSort`：產生合法順序與可平行學習階段。
- `MergeSort`：修課紀錄依課程名稱全域排序後再分頁。

## 主要文件

- [專案範圍](docs/01_project_scope.md)
- [資料庫設計](docs/02_database_design.md)
- [API 規格](docs/03_api_spec.md)
- [系統架構](docs/04_project_architecture.md)
- [測試策略](docs/06_test_strategy.md)
- [演算法模組總覽](docs/07_algorithm_module_overview.md)
