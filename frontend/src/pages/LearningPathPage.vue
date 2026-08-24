<script setup>
import { computed, onMounted, ref } from 'vue'
import Icon from '../components/common/Icon.vue'
import FormField from '../components/common/FormField.vue'
import { getLearningPath, getCourseOptions } from '../api/course.js' // 引入 getCourseOptions
import { useToastStore } from '../stores/toast.js'
import { extractErrorMessage } from '../utils/errorMessage.js'

// Module 5：課程學習路徑頁（對應 Demo Step 7「展示 Graph 學習路徑」）
//
// 這一頁分成兩段：
// 1. 「選課程看關係」：選一門課，只顯示跟它「直接」相關的先修／後續課程——
//    資料直接讀 course.prerequisiteIds 這個欄位（來自 GET /api/courses），純粹是欄位查詢，
//    不做遞迴、不走訪整張圖，所以不算「前端實作圖演算法」。
// 2. 「建議學習階段」：後端用 CourseGraph 與拓樸排序（Topological Sort）將同一輪可修的課程分組，
//    對應 GET /api/courses/learning-path，前端只負責顯示 stages，排序邏輯完全在後端／資料結構模組。
const toast = useToastStore()

const learningPathNodes = ref([]) // 儲存 learning-path API 返回的 nodes
const learningPathEdges = ref([]) // 儲存 learning-path API 返回的 edges
const topologicalStages = ref([]) // 儲存可平行學習的拓樸階段
const loadingPath = ref(true)

const courses = ref([]) // 儲存 getCourseOptions 返回的課程列表
const loadingCourses = ref(true)
const selectedCourseId = ref('')

async function loadPath() {
  loadingPath.value = true
  try {
    const result = await getLearningPath()
    learningPathNodes.value = result.nodes
    learningPathEdges.value = result.edges
    topologicalStages.value = result.stages?.length
      ? result.stages
      : result.topologicalOrder?.length
        ? [result.topologicalOrder]
        : []
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取學習路徑失敗'))
  } finally {
    loadingPath.value = false
  }
}

async function loadCourses() {
  loadingCourses.value = true
  try {
    // 使用 getCourseOptions 取得不分頁的輕量資料
    courses.value = await getCourseOptions()
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

// 將每一階段的課程 ID 轉換為畫面需要的課程資料
const learningStages = computed(() => {
  return topologicalStages.value.map((courseIds, index) => ({
    number: index + 1,
    courses: courseIds.map((id) => {
      const course = learningPathNodes.value.find((node) => node.id === id)
      return course ?? { id, code: `#${id}`, name: '未知課程' }
    }),
  }))
})
</script>

<template>
  <div class="page">
    <!-- 第一段：選課程看「直接」關係 -->
    <section class="panel">
      <h2>課程先修關係（有向圖）</h2>
      <p class="algorithm-note">
        系統以課程作為節點、先修關係作為有方向的連線；選擇課程後，可查看與它直接相連的先修與後續課程。
      </p>
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

    <!-- 第二段：依拓樸排序輪次呈現可平行學習的階段 -->
    <section class="panel">
      <h2>建議學習階段（拓撲排序）</h2>
      <p class="algorithm-note">
        系統依據先修關係將課程分成多個階段；同一階段可平行學習，後續階段則需先完成必要的先修課程。
      </p>

      <div v-if="loadingPath" class="path-skeleton" aria-hidden="true">
        <span v-for="n in 4" :key="n"></span>
      </div>

      <div v-else-if="learningStages.length === 0" class="empty-state">
        <Icon name="route" :size="24" />
        <p>目前還沒有課程資料，無法產生學習路徑。</p>
      </div>

      <div v-else class="stage-list" aria-label="依先修關係分組的建議學習階段">
        <section v-for="stage in learningStages" :key="stage.number" class="learning-stage">
          <div class="stage-heading">
            <h3>階段 {{ stage.number }}</h3>
            <span>同一階段可平行學習</span>
          </div>
          <ul class="stage-courses">
            <li v-for="course in stage.courses" :key="course.id" class="stage-course">
              <span class="course-code">{{ course.code }}</span>
              <span class="course-name">{{ course.name }}</span>
            </li>
          </ul>
        </section>
      </div>
    </section>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: var(--space-5); }
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

.stage-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.learning-stage {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.stage-heading {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.stage-heading h3 {
  margin: 0;
  font-size: var(--font-size-base);
}
.stage-heading span {
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
}

.stage-courses {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(280px, 100%), 1fr));
  gap: var(--space-2);
}
.stage-course {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 88px;
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg-surface);
}
.course-code { color: var(--color-text-muted); font-size: var(--font-size-xs); font-weight: 600; }
.course-name { color: var(--color-text-primary); font-weight: 600; }

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
