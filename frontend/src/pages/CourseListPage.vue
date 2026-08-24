<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import DataTable from '../components/common/DataTable.vue'
import AppButton from '../components/common/AppButton.vue'
import AppModal from '../components/common/AppModal.vue'
import FormField from '../components/common/FormField.vue'
import Icon from '../components/common/Icon.vue'
import MiniStat from '../components/common/MiniStat.vue'
import { getCourses, createCourse, updateCourse, addPrerequisite, removePrerequisite, getAvailablePrerequisites, getCourseOptions } from '../api/course.js'
import { useToastStore } from '../stores/toast.js'
import { extractErrorMessage } from '../utils/errorMessage.js'
import { debounce } from '../utils/debounce.js'

// Module 2：課程管理頁（Issue #7）－ List / Create / Update / 建立先修關係
// 分頁／搜尋／排序改成呼叫後端 API 處理（配合預期 200 筆以上的測試資料量）。
//
// 這個頁面比人員管理多一個麻煩：先修課程的「標籤名稱」跟「勾選清單」需要知道『全部』課程
// （不能只知道目前這一頁的 10 筆，不然標籤會對不到名字、勾選清單也選不到不在這一頁的課程），
// 所以另外維護一份 allCourses（用一個夠大的 size 一次抓回來，涵蓋預期的最大筆數），
// 跟給表格顯示用的 courses（目前這一頁）分開管理，兩者用途不同、不能共用同一個變數。
const toast = useToastStore()

const PAGE_SIZE = 10
const courses = ref([])                                         // 目前這一頁的資料，給表格顯示用
const allCourses = ref([])                                      // 全部課程，給先修標籤查名字／先修勾選清單用
const loading = ref(true)
const totalItems = ref(0)
const page = ref(1)

const COLUMNS = [
  { key: 'code', label: '課程代碼', width: '12%' },
  { key: 'name', label: '課程名稱', width: '20%' },
  { key: 'description', label: '描述', width: '32%' },
  { key: 'prerequisites', label: '建議先修', width: '22%' },
  { key: 'actions', label: '操作', width: '14%' },
]

// 依 id 查課程名稱，畫先修課程標籤用——查 allCourses（全部課程），不能查 courses（只有目前這一頁），
// 不然先修課程剛好不在目前這一頁時，標籤會顯示成 #id 而不是真正的名字
function courseNameById(id) {
  return allCourses.value.find((c) => c.id === id)?.name ?? `#${id}`
}

async function loadCourses() {
  loading.value = true
  try {
    const result = await getCourses({
      page: page.value - 1,
      size: PAGE_SIZE,
      search: searchQuery.value.trim(),
    })
    courses.value = result.content
    totalItems.value = result.totalElements
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取課程列表失敗'))
  } finally {
    loading.value = false
  }
}

async function loadAllCourses() {
  try {
    // 使用新的 getCourseOptions API 取得不分頁的輕量資料
    allCourses.value = await getCourseOptions()
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取課程清單失敗'))
  }
}

function handlePageChange(newPage) {
  page.value = newPage
  loadCourses()
}

// 課程總數：另外用「只要總筆數、不要內容」的輕量查詢算，搜尋時上面的總數才不會跟著抖動，
// 永遠反映系統裡的實際總數，不是搜尋後的筆數
const totalCoursesCount = ref(0)
async function loadStats() {
  try {
    const result = await getCourses({ page: 0, size: 1 })
    totalCoursesCount.value = result.totalElements
  } catch {
    // 統計數字讀取失敗不影響主要列表功能，安靜失敗就好
  }
}

// ---- 搜尋：依課程代碼／名稱過濾，現在會實際打 API（帶 search query 參數） ----
const searchQuery = ref('')
const debouncedSearch = debounce(() => {
  page.value = 1
  loadCourses()
}, 400)
watch(searchQuery, debouncedSearch)

onMounted(() => {
  loadCourses()
  loadAllCourses()
  loadStats()
})

// =====================================================================
// 新增 / 編輯課程
// =====================================================================
const isFormModalOpen = ref(false)
const editingId = ref(null)
const isSubmitting = ref(false)
const form = reactive({ code: '', name: '', description: '' })
const errors = reactive({ code: '', name: '' })

const formModalTitle = computed(() => (editingId.value ? '編輯課程' : '新增課程'))

function resetForm() {
  form.code = ''
  form.name = ''
  form.description = ''
  errors.code = ''
  errors.name = ''
}

function openCreateModal() {
  editingId.value = null
  resetForm()
  isFormModalOpen.value = true
}

function openEditModal(course) {
  editingId.value = course.id
  form.code = course.code
  form.name = course.name
  form.description = course.description ?? ''
  errors.code = ''
  errors.name = ''
  isFormModalOpen.value = true
}

function closeFormModal() {
  isFormModalOpen.value = false
}

function validateForm() {
  errors.code = form.code.trim() ? '' : '課程代碼為必填欄位'
  errors.name = form.name.trim() ? '' : '課程名稱為必填欄位'
  return !errors.code && !errors.name
}

async function handleFormSubmit() {
  if (!validateForm()) return
  isSubmitting.value = true
  try {
    if (editingId.value) {
      // 注意：docs/03_api_spec.md §4.3 PUT /api/courses/{id} 完全沒有列出 Request Body 範例，
      // 這裡假設只能改 name/description（code 當唯一識別碼維持不可變），是我方的推測，還沒跟後端確認過。
      await updateCourse(editingId.value, { name: form.name, description: form.description })
      toast.success('已更新課程資料')
    } else {
      await createCourse({ code: form.code, name: form.name, description: form.description })
      toast.success('已新增課程')
    }
    closeFormModal()
    await loadCourses()
    await loadAllCourses()                                       // 新增/改名後，先修標籤查表用的全部課程清單也要跟著更新
    await loadStats()
  } catch (error) {
    toast.error(extractErrorMessage(error, editingId.value ? '更新課程失敗' : '新增課程失敗'))
  } finally {
    isSubmitting.value = false
  }
}

// =====================================================================
// 建立先修關係（對應 Course Graph 的一條邊：courseId 建議先修 prerequisiteId）
// 改成勾選式多選：可以一次勾多門課程再送出，也可以取消勾選已確認的項目。
// =====================================================================
const isPrereqModalOpen = ref(false)
const targetCourse = ref(null)                                 // 目前正在設定先修關係的課程
const checkedPrereqIds = ref([])                                // Modal 內目前勾選的先修課程 id（複選）
const availablePrerequisites = ref([])                          // 可供選擇的先修課程列表
const isSavingPrereqs = ref(false)

// 可新增的候選由後端排除 Cycle；既有先修課程則從 allCourses 補回清單，
// 讓使用者仍能看到已勾選項目並取消設定。
const otherCourses = computed(() => {
  const existingIds = new Set(targetCourse.value?.prerequisiteIds ?? [])
  const existingPrerequisites = allCourses.value.filter((course) => existingIds.has(course.id))
  const coursesById = new Map(
    [...existingPrerequisites, ...availablePrerequisites.value]
      .map((course) => [course.id, course]),
  )
  return [...coursesById.values()].sort((first, second) => first.id - second.id)
})

async function openPrereqModal(course) {
  targetCourse.value = course
  checkedPrereqIds.value = [...(course.prerequisiteIds ?? [])]  // 用複本初始化，取消勾選不會直接動到原始資料
  isPrereqModalOpen.value = true
  try {
    availablePrerequisites.value = await getAvailablePrerequisites(course.id)
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取可用先修課程失敗'))
  }
}

function closePrereqModal() {
  isPrereqModalOpen.value = false
  targetCourse.value = null
  availablePrerequisites.value = []
}

async function handleSavePrerequisites() {
  if (!targetCourse.value) return

  const courseId = targetCourse.value.id
  const original = new Set(targetCourse.value.prerequisiteIds ?? [])
  const current = new Set(checkedPrereqIds.value)
  const toAdd = [...current].filter((id) => !original.has(id))
  const toRemove = [...original].filter((id) => !current.has(id)) // 改名為 toRemove

  if (toAdd.length === 0 && toRemove.length === 0) {
    closePrereqModal()
    return
  }

  isSavingPrereqs.value = true
  try {
    // 處理新增
    for (const prerequisiteId of toAdd) {
      await addPrerequisite(courseId, prerequisiteId)
    }

    // 處理移除
    for (const prerequisiteId of toRemove) {
      await removePrerequisite(courseId, prerequisiteId)
    }

    toast.success('已更新建議先修課程設定')
    closePrereqModal()
    await loadCourses()                                          // 重新讀取目前這一頁，確保畫面跟資料來源一致
    await loadAllCourses()                                       // 重新讀取全部課程，確保先修標籤查表用的清單更新
  } catch (error) {
    toast.error(extractErrorMessage(error, '設定先修關係失敗，可能會造成循環先修（Cycle）'))
  } finally {
    isSavingPrereqs.value = false
  }
}
</script>

<template>
  <div class="page">
    <!-- 課程總數小型統計：透過 Teleport 塞進 AppNavbar.vue 的導覽列裡顯示，排版跟人員管理一致（見該檔案的 #navbar-stats-target） -->
    <Teleport to="#navbar-stats-target">
      <MiniStat label="課程總數" :value="totalCoursesCount" icon="book" accent="var(--color-brand-400)" label-first />
    </Teleport>

    <!-- 搜尋在左、新增課程靠右，同一列 -->
    <div class="list-toolbar">
      <div class="search-box">
        <Icon name="search" :size="16" class="search-icon" />
        <input v-model="searchQuery" type="search" placeholder="搜尋課程代碼或名稱…" aria-label="搜尋課程：依代碼或名稱" />
      </div>

      <AppButton variant="primary" icon-only tooltip="新增課程" @click="openCreateModal">
        <Icon name="plus" :size="16" />
      </AppButton>
    </div>

    <DataTable
      :columns="COLUMNS"
      :rows="courses"
      :loading="loading"
      empty-message="找不到符合搜尋條件的課程"
      :page="page"
      :page-size="PAGE_SIZE"
      :total-items="totalItems"
      @update:page="handlePageChange"
    >
      <template #cell-description="{ row }">
        <span class="desc-cell">{{ row.description || '—' }}</span>
      </template>

      <template #cell-prerequisites="{ row }">
        <div v-if="row.prerequisiteIds?.length" class="prereq-tags">
          <span v-for="pid in row.prerequisiteIds" :key="pid" class="prereq-tag">{{ courseNameById(pid) }}</span>
        </div>
        <span v-else class="desc-cell">—</span>
      </template>

      <template #cell-actions="{ row }">
        <div class="row-actions">
          <AppButton variant="ghost" icon-only tooltip="編輯課程" @click="openEditModal(row)">
            <Icon name="pencil" :size="16" />
          </AppButton>
          <AppButton variant="ghost" icon-only tooltip="設定建議先修" @click="openPrereqModal(row)">
            <Icon name="route" :size="16" />
          </AppButton>
        </div>
      </template>
    </DataTable>

    <!-- 新增 / 編輯課程 -->
    <AppModal v-if="isFormModalOpen" :title="formModalTitle" @close="closeFormModal">
      <form @submit.prevent="handleFormSubmit">
        <FormField id="course-code" label="課程代碼" required :error="errors.code" :hint="editingId ? '課程代碼建立後不可修改' : ''" v-slot="{ describedBy }">
          <input id="course-code" v-model="form.code" type="text" :aria-describedby="describedBy" :disabled="Boolean(editingId)" placeholder="例如 JAVA01" />
        </FormField>

        <FormField id="course-name" label="課程名稱" required :error="errors.name" v-slot="{ describedBy }">
          <input id="course-name" v-model="form.name" type="text" :aria-describedby="describedBy" />
        </FormField>

        <FormField id="course-description" label="課程描述" v-slot="{ describedBy }">
          <textarea id="course-description" v-model="form.description" :aria-describedby="describedBy"></textarea>
        </FormField>

        <div class="form-actions">
          <AppButton variant="secondary" type="button" @click="closeFormModal">取消</AppButton>
          <AppButton variant="primary" type="submit" :loading="isSubmitting">
            {{ editingId ? '儲存變更' : '確認新增' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- 設定先修課程：勾選式多選 -->
    <AppModal v-if="isPrereqModalOpen" :title="`設定建議先修課程：${targetCourse?.name}`" @close="closePrereqModal">
      <fieldset class="prereq-fieldset">
        <legend>勾選建議先修課程（可複選）</legend>

        <div v-if="otherCourses.length > 0" class="prereq-checklist">
          <label v-for="c in otherCourses" :key="c.id" class="prereq-check-item">
            <input v-model="checkedPrereqIds" type="checkbox" :value="c.id" />
            <span>{{ c.code }} － {{ c.name }}</span>
          </label>
        </div>
        <p v-else class="desc-cell">目前沒有其他課程可以設定。</p>
      </fieldset>

      <div class="form-actions">
        <AppButton variant="secondary" type="button" @click="closePrereqModal">取消</AppButton>
        <AppButton variant="primary" :loading="isSavingPrereqs" @click="handleSavePrerequisites">
          儲存設定
        </AppButton>
      </div>
    </AppModal>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: var(--space-4); }

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1 1 280px;
  max-width: 360px;
}
.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-muted);
  pointer-events: none;
}
.search-box input {
  width: 100%;
  min-height: 40px;
  padding: 0 var(--space-3) 0 40px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-pill);
  background: var(--color-bg-surface);
  font-size: var(--font-size-sm);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}
.search-box input:focus-visible {
  outline: none;
  border-color: var(--color-brand-600);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-brand-600) 20%, transparent);
}

.desc-cell {
  color: var(--color-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;                                       /* 描述文字最多顯示兩行，避免表格被超長描述撐高 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: normal;                                         /* 覆寫 DataTable 預設的 nowrap，這一欄需要真的換行才能顯示兩行 */
  max-width: 260px;
}

.prereq-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.prereq-tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--color-brand-100);
  color: var(--color-brand-800);
  font-size: var(--font-size-xs);
  font-weight: 600;
  white-space: nowrap;
}

.row-actions { display: flex; gap: var(--space-2); flex-wrap: wrap; }

.prereq-fieldset {
  border: none;
  padding: 0;
  margin: 0 0 var(--space-4);
}
.prereq-fieldset legend {
  font-size: var(--font-size-sm);
  font-weight: 600;
  padding: 0;
  margin-bottom: var(--space-2);
}

.prereq-checklist {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 260px;
  overflow-y: auto;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-2);
}
.prereq-check-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-2);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  cursor: pointer;
}
.prereq-check-item:hover { background: var(--color-brand-100); }
.prereq-check-item input[type='checkbox'] {
  width: 18px;
  height: 18px;
  min-height: 0;                                                /* 覆寫 FormField 對一般 input 的 min-height:44px，checkbox 不需要那麼大 */
  accent-color: var(--color-brand-600);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-5);
}
</style>
