<script setup>
import { computed, onMounted, ref } from 'vue'
import Icon from '../components/common/Icon.vue'
import FormField from '../components/common/FormField.vue'
import { getLearningPath, getCourses } from '../api/course.js'
import { useToastStore } from '../stores/toast.js'
import { extractErrorMessage } from '../utils/errorMessage.js'

// Module 5：課程學習路徑頁（對應 Demo Step 7「展示 Graph 學習路徑」）
//
// 這一頁分成兩段：
// 1. 「選課程看關係」：選一門課，只顯示跟它「直接」相關的先修／後續課程——
//    資料直接讀 course.prerequisiteIds 這個欄位（來自 GET /api/courses），純粹是欄位查詢，
//    不做遞迴、不走訪整張圖，所以不算「前端實作圖演算法」。
// 2. 「完整學習路徑」：後端用 CourseGraph 做拓樸排序（Topological Sort）後的完整建議修課順序，
//    對應 GET /api/courses/learning-path，前端只負責顯示陣列，排序邏輯完全在後端／資料結構模組。
const toast = useToastStore()

const path = ref([])
const loadingPath = ref(true)
const courses = ref([])
const loadingCourses = ref(true)
const selectedCourseId = ref('')

async function loadPath() {
  loadingPath.value = true
  try {
    path.value = await getLearningPath()
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取學習路徑失敗'))
  } finally {
    loadingPath.value = false
  }
}

async function loadCourses() {
  loadingCourses.value = true
  try {
    // getCourses() 現在回傳分頁格式（{ content, totalElements, ... }），這裡要顯示的下拉選單需要「全部課程」，
    // 用一個夠大的 size（500，涵蓋預期的最大筆數 200+）一次抓回來，不需要真的做分頁瀏覽
    const result = await getCourses({ page: 0, size: 500 })
    courses.value = result.content
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取課程列表失敗'))
  } finally {
    loadingCourses.value = false
  }
}

onMounted(() => {
  loadPath()
  loadCourses()
})

function courseNameById(id) {
  return courses.value.find((c) => c.id === id)?.name ?? `#${id}`
}

const selectedCourse = computed(() => courses.value.find((c) => c.id === Number(selectedCourseId.value)) ?? null)

// 這門課「直接」需要的先修課程——只取 prerequisiteIds 這一層，不遞迴往上找
const directPrerequisites = computed(() => {
  if (!selectedCourse.value) return []
  return (selectedCourse.value.prerequisiteIds ?? []).map((id) => ({ id, name: courseNameById(id) }))
})

// 反過來：哪些課程把「這門課」設為直接先修——同樣只查一層，用 Array.filter 找符合的課程，不是圖走訪
const directFollowUps = computed(() => {
  if (!selectedCourse.value) return []
  return courses.value.filter((c) => c.prerequisiteIds?.includes(selectedCourse.value.id))
})
</script>

<template>
  <div class="page">
    <!-- 第一段：選課程看「直接」關係 -->
    <section class="panel">
      <h2>選課程看先修關係</h2>
      <FormField id="course-relation-select" label="選擇課程" v-slot="{ describedBy }">
        <select id="course-relation-select" v-model="selectedCourseId" :aria-describedby="describedBy" :disabled="loadingCourses">
          <option value="">請選擇課程</option>
          <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.code }} － {{ c.name }}</option>
        </select>
      </FormField>

      <div v-if="!selectedCourse" class="empty-state">
        <Icon name="route" :size="24" />
        <p>選一門課程，查看跟它直接相關的建議先修／後續課程。</p>
      </div>

      <div v-else class="relation-card">
        <h3>{{ selectedCourse.name }}</h3>

        <div class="relation-group">
          <h4>需要先修</h4>
          <div v-if="directPrerequisites.length > 0" class="tag-list">
            <span v-for="p in directPrerequisites" :key="p.id" class="tag">{{ p.name }}</span>
          </div>
          <p v-else class="muted">沒有設定建議先修課程</p>
        </div>

        <div class="relation-group">
          <h4>是以下課程的先修</h4>
          <div v-if="directFollowUps.length > 0" class="tag-list">
            <span v-for="f in directFollowUps" :key="f.id" class="tag">{{ f.name }}</span>
          </div>
          <p v-else class="muted">目前沒有其他課程把這門課設為建議先修</p>
        </div>
      </div>
    </section>

    <!-- 第二段：完整拓樸排序結果（範例展示用，對應 Demo Step 7） -->
    <section class="panel">
      <h2>完整課程學習路徑</h2>

      <div v-if="loadingPath" class="path-skeleton" aria-hidden="true">
        <span v-for="n in 4" :key="n"></span>
      </div>

      <div v-else-if="path.length === 0" class="empty-state">
        <Icon name="route" :size="24" />
        <p>目前還沒有課程資料，無法產生學習路徑。</p>
      </div>

      <!-- role="list" 讓這條「視覺上是流程圖、語意上是有序清單」的內容，對螢幕閱讀器來說仍然是一份清單 -->
      <ol v-else class="path-flow" aria-label="課程學習路徑，依先修順序排列">
        <li v-for="(courseName, index) in path" :key="courseName" class="path-step">
          <div class="path-node">
            <span class="path-index">{{ index + 1 }}</span>
            <span class="path-name">{{ courseName }}</span>
          </div>
          <Icon v-if="index < path.length - 1" name="chevron-down" :size="18" class="path-arrow" />
        </li>
      </ol>
    </section>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: var(--space-5); }
.page-desc { margin: 0; color: var(--color-text-secondary); font-size: var(--font-size-sm); max-width: 68ch; }

.panel {
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}
.panel h2 { font-size: var(--font-size-md); margin: 0 0 var(--space-3); }

.relation-card {
  margin-top: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}
.relation-card h3 { margin: 0 0 var(--space-4); font-size: var(--font-size-base); }

.relation-group { margin-bottom: var(--space-4); }
.relation-group:last-child { margin-bottom: 0; }
.relation-group h4 {
  margin: 0 0 var(--space-2);
  font-size: var(--font-size-xs);
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.02em;
}
.muted { margin: 0; font-size: var(--font-size-sm); color: var(--color-text-muted); }

.tag-list { display: flex; flex-wrap: wrap; gap: 6px; }
.tag {
  padding: 3px 10px;
  border-radius: 999px;
  background: var(--color-brand-100);
  color: var(--color-brand-800);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.path-flow {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-1);
}

.path-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.path-node {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  width: 100%;
  max-width: 420px;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--color-brand-100);
  border: 1px solid var(--color-border);
}

.path-index {
  display: grid;
  place-items: center;
  width: 26px;
  height: 26px;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--color-brand-600);
  color: var(--color-text-inverse);
  font-size: var(--font-size-xs);
  font-weight: 700;
}

.path-name {
  font-weight: 600;
  color: var(--color-text-primary);
}

.path-arrow {
  color: var(--color-brand-400);
  margin: 2px 0;
}

.path-skeleton {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-3);
}
.path-skeleton span {
  width: 100%;
  max-width: 420px;
  height: 48px;
  border-radius: var(--radius-md);
  background: linear-gradient(90deg, var(--color-border) 25%, #eef1ee 50%, var(--color-border) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-6);
  color: var(--color-text-muted);
}

</style>
