<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import DataTable from '../components/common/DataTable.vue'
import AppButton from '../components/common/AppButton.vue'
import AppModal from '../components/common/AppModal.vue'
import FormField from '../components/common/FormField.vue'
import Badge from '../components/common/Badge.vue'
import Icon from '../components/common/Icon.vue'
import { getPerson, updatePerson, getPersonEnrollments } from '../api/person.js'
import { getCourses } from '../api/course.js'
import { createEnrollment, updateEnrollmentStatus } from '../api/enrollment.js'
import { personStatusMeta, PERSON_STATUS_OPTIONS, enrollmentStatusMeta, ENROLLMENT_STATUS_OPTIONS } from '../utils/statusMeta.js'
import { useToastStore } from '../stores/toast.js'
import { extractErrorMessage } from '../utils/errorMessage.js'

// 人員詳情頁：對應 Module 1「查詢單一人員」＋ Module 3「學習進度管理」（註冊課程／更新狀態／查詢學習紀錄）。
// Module 3 原本有一個獨立的 /enrollments 分頁（用下拉選單先選學員、再操作），
// 跟這裡的「學習紀錄」區塊功能完全重複，所以已經拿掉那個獨立分頁，統一從人員列表點進來這裡處理。
//
// 學習紀錄表格改成分頁（配合預期 200 筆以上的測試資料量，雖然單一學員的修課數量實際上不會真的到 200 筆，
// 但為了跟人員/課程列表的做法一致，這裡也採用一樣的分頁模式）。
const route = useRoute()
const toast = useToastStore()

const personId = computed(() => route.params.id)
const person = ref(null)
const loadingPerson = ref(true)

const ALL_SIZE = 500                                             // 「查全部、不分頁」用的緩衝 size，涵蓋預期的最大筆數
const courses = ref([])                                          // 全部課程，給「註冊新課程」下拉選單用

const PAGE_SIZE = 10
const enrollments = ref([])                                      // 目前這一頁的學習紀錄，給表格顯示用
const loadingEnrollments = ref(true)
const totalEnrollments = ref(0)
const page = ref(1)
const sortBy = ref('')
const sortDir = ref('asc')

// 這個人「全部」已註冊的課程 id（不只是目前這一頁）——「註冊新課程」的下拉選單要用這個排除已經修過的課程，
// 不能只看 enrollments（分頁後只剩目前這一頁），不然分頁到別頁的課程會被誤判成「還沒修過」而重複出現在下拉選單裡
const enrolledCourseIds = ref(new Set())

const ENROLLMENT_COLUMNS = [
  { key: 'courseName', label: '課程名稱', sortable: true, width: '55%' },
  { key: 'status', label: '狀態', width: '20%' },
  { key: 'actions', label: '操作', width: '25%' },
]

async function loadPerson() {
  loadingPerson.value = true
  try {
    person.value = await getPerson(personId.value)
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取人員資料失敗'))
  } finally {
    loadingPerson.value = false
  }
}

async function loadEnrollments() {
  loadingEnrollments.value = true
  try {
    const result = await getPersonEnrollments(personId.value, {
      page: page.value - 1,
      size: PAGE_SIZE,
      sort: sortBy.value ? `${sortBy.value},${sortDir.value}` : '',
    })
    enrollments.value = result.content
    totalEnrollments.value = result.totalElements
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取學習紀錄失敗'))
  } finally {
    loadingEnrollments.value = false
  }
}

async function loadEnrolledCourseIds() {
  try {
    const result = await getPersonEnrollments(personId.value, { page: 0, size: ALL_SIZE })
    enrolledCourseIds.value = new Set(result.content.map((e) => e.courseId))
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取學習紀錄失敗'))
  }
}

function handleSortChange({ key, dir }) {
  sortBy.value = key
  sortDir.value = dir
  page.value = 1
  loadEnrollments()
}

function handlePageChange(newPage) {
  page.value = newPage
  loadEnrollments()
}

async function loadCourses() {
  // 「全部課程」給下拉選單用，跟 CourseListPage.vue 的 allCourses 是同一種需求：size 給大一點，一次抓回來
  const result = await getCourses({ page: 0, size: ALL_SIZE })
  courses.value = result.content
}

onMounted(() => {
  loadPerson()
  loadEnrollments()
  loadEnrolledCourseIds()
  loadCourses()
})

// 路由參數變了（例如從這個人的詳情頁直接跳到另一個人，網址列 id 換了但元件沒重建）就重新載入，
// 正常情況下 AppShell 會用 route.path 當 key 強制重建元件，這裡多加一層是保險，不依賴那個機制也能正確運作
watch(personId, () => {
  page.value = 1
  loadPerson()
  loadEnrollments()
  loadEnrolledCourseIds()
})

// =====================================================================
// 編輯基本資料
// =====================================================================
const isEditModalOpen = ref(false)
const isSubmitting = ref(false)
const form = reactive({ name: '', phone: '', status: 'ACTIVE' })
const errors = reactive({ name: '' })

function openEditModal() {
  form.name = person.value.name
  form.phone = person.value.phone ?? ''
  form.status = person.value.status
  errors.name = ''
  isEditModalOpen.value = true
}
function closeEditModal() {
  isEditModalOpen.value = false
}

async function handleEditSubmit() {
  errors.name = form.name.trim() ? '' : '姓名為必填欄位'
  if (errors.name) return
  isSubmitting.value = true
  try {
    // 同 PersonListPage.vue 的註記：status 是否真的會被後端接受，要等 PersonUpdateRequest DTO 確認
    await updatePerson(personId.value, { name: form.name, phone: form.phone, status: form.status })
    toast.success('已更新人員資料')
    closeEditModal()
    await loadPerson()
  } catch (error) {
    toast.error(extractErrorMessage(error, '更新人員失敗'))
  } finally {
    isSubmitting.value = false
  }
}

// =====================================================================
// 直接從詳情頁註冊新課程
// =====================================================================
const availableCourses = computed(() => courses.value.filter((c) => !enrolledCourseIds.value.has(c.id)))

const isEnrollModalOpen = ref(false)
const selectedCourseId = ref('')
const isEnrolling = ref(false)

function openEnrollModal() {
  selectedCourseId.value = ''
  isEnrollModalOpen.value = true
}
function closeEnrollModal() {
  isEnrollModalOpen.value = false
}

async function handleEnroll() {
  if (!selectedCourseId.value) return
  isEnrolling.value = true
  try {
    await createEnrollment({ personId: personId.value, courseId: Number(selectedCourseId.value) })
    toast.success('已完成課程註冊')
    closeEnrollModal()
    page.value = 1
    await loadEnrollments()
    await loadEnrolledCourseIds()
  } catch (error) {
    toast.error(extractErrorMessage(error, '註冊課程失敗'))
  } finally {
    isEnrolling.value = false
  }
}

// ---- 更新學習狀態 ----
const updatingId = ref(null)

async function handleStatusChange(enrollment, newStatus) {
  if (newStatus === enrollment.status) return
  const previousStatus = enrollment.status
  updatingId.value = enrollment.id
  enrollment.status = newStatus
  try {
    await updateEnrollmentStatus(enrollment.id, newStatus)
    toast.success('已更新學習狀態')
  } catch (error) {
    enrollment.status = previousStatus
    toast.error(extractErrorMessage(error, '更新學習狀態失敗'))
  } finally {
    updatingId.value = null
  }
}
</script>

<template>
  <div class="page">
    <RouterLink to="/people" class="back-link">
      <Icon name="chevron-left" :size="16" />
      返回人員管理
    </RouterLink>

    <div v-if="loadingPerson" class="profile-skeleton" aria-hidden="true"></div>

    <template v-else-if="person">
      <article class="profile-card">
        <div class="profile-avatar" aria-hidden="true">{{ person.name.slice(0, 1) }}</div>
        <div class="profile-info">
          <div class="profile-heading">
            <h2>{{ person.name }}</h2>
            <Badge
              :color="personStatusMeta(person.status).color"
              :label="personStatusMeta(person.status).label"
              :icon="personStatusMeta(person.status).icon"
              :solid="personStatusMeta(person.status).solid"
            />
          </div>
          <dl class="profile-fields">
            <div><dt>Email</dt><dd>{{ person.email }}</dd></div>
            <div><dt>電話</dt><dd>{{ person.phone || '—' }}</dd></div>
          </dl>
        </div>
        <AppButton variant="secondary" @click="openEditModal">
          <Icon name="pencil" :size="16" />
          編輯基本資料
        </AppButton>
      </article>

      <section class="enrollment-section">
        <div class="page-toolbar">
          <h3>學習紀錄</h3>
          <AppButton variant="primary" icon-only tooltip="註冊新課程" @click="openEnrollModal">
            <Icon name="plus" :size="16" />
          </AppButton>
        </div>

        <DataTable
          :columns="ENROLLMENT_COLUMNS"
          :rows="enrollments"
          :loading="loadingEnrollments"
          empty-message="這位學員目前還沒有任何學習紀錄"
          :page="page"
          :page-size="PAGE_SIZE"
          :total-items="totalEnrollments"
          :sort-by="sortBy"
          :sort-dir="sortDir"
          @update:page="handlePageChange"
          @sort-change="handleSortChange"
        >
          <template #cell-status="{ row }">
            <Badge :color="enrollmentStatusMeta(row.status).color" :label="enrollmentStatusMeta(row.status).label" :icon="enrollmentStatusMeta(row.status).icon" />
          </template>
          <template #cell-actions="{ row }">
            <select
              class="status-select"
              :value="row.status"
              :disabled="updatingId === row.id"
              :aria-label="`更新「${row.courseName}」的學習狀態`"
              @change="handleStatusChange(row, $event.target.value)"
            >
              <option v-for="option in ENROLLMENT_STATUS_OPTIONS" :key="option" :value="option">
                {{ enrollmentStatusMeta(option).label }}
              </option>
            </select>
          </template>
        </DataTable>
      </section>
    </template>

    <div v-else class="empty-state">
      <Icon name="users" :size="24" />
      <p>找不到這位人員，可能已經被移除。</p>
    </div>

    <!-- 編輯基本資料 -->
    <AppModal v-if="isEditModalOpen" title="編輯人員資料" @close="closeEditModal">
      <form @submit.prevent="handleEditSubmit">
        <FormField id="detail-name" label="姓名" required :error="errors.name" v-slot="{ describedBy }">
          <input id="detail-name" v-model="form.name" type="text" :aria-describedby="describedBy" autocomplete="name" />
        </FormField>
        <FormField id="detail-phone" label="電話" v-slot="{ describedBy }">
          <input id="detail-phone" v-model="form.phone" type="tel" :aria-describedby="describedBy" autocomplete="tel" />
        </FormField>
        <FormField id="detail-status" label="狀態" v-slot="{ describedBy }">
          <select id="detail-status" v-model="form.status" :aria-describedby="describedBy">
            <option v-for="option in PERSON_STATUS_OPTIONS" :key="option" :value="option">
              {{ personStatusMeta(option).label }}
            </option>
          </select>
        </FormField>
        <div class="form-actions">
          <AppButton variant="secondary" type="button" @click="closeEditModal">取消</AppButton>
          <AppButton variant="primary" type="submit" :loading="isSubmitting">儲存變更</AppButton>
        </div>
      </form>
    </AppModal>

    <!-- 註冊新課程 -->
    <AppModal v-if="isEnrollModalOpen" title="註冊新課程" @close="closeEnrollModal">
      <FormField id="enroll-course" label="選擇課程" required v-slot="{ describedBy }">
        <select id="enroll-course" v-model="selectedCourseId" :aria-describedby="describedBy">
          <option value="" disabled>請選擇課程</option>
          <option v-for="c in availableCourses" :key="c.id" :value="c.id">{{ c.code }} － {{ c.name }}</option>
        </select>
      </FormField>
      <p v-if="availableCourses.length === 0" class="empty-note">這位學員已經註冊完所有課程了。</p>

      <div class="form-actions">
        <AppButton variant="secondary" type="button" @click="closeEnrollModal">取消</AppButton>
        <AppButton variant="primary" :disabled="!selectedCourseId" :loading="isEnrolling" @click="handleEnroll">
          確認註冊
        </AppButton>
      </div>
    </AppModal>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: var(--space-5); }

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
  width: fit-content;
}
.back-link:hover { color: var(--color-brand-600); text-decoration: none; }

.profile-card {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  padding: var(--space-5);
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  flex-wrap: wrap;
}

.profile-avatar {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  flex-shrink: 0;
  border-radius: 50%;
  background: linear-gradient(145deg, var(--color-brand-400), var(--color-brand-600));
  color: var(--color-text-inverse);
  font-size: var(--font-size-xl);
  font-weight: 700;
}

.profile-info { flex: 1; min-width: 200px; }
.profile-heading { display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-2); }
.profile-heading h2 { margin: 0; font-size: var(--font-size-lg); }

.profile-fields {
  display: flex;
  gap: var(--space-6);
  margin: 0;
  flex-wrap: wrap;
}
.profile-fields dt { font-size: var(--font-size-xs); color: var(--color-text-muted); margin-bottom: 2px; }
.profile-fields dd { margin: 0; font-size: var(--font-size-sm); color: var(--color-text-primary); }

.profile-skeleton {
  height: 112px;
  border-radius: var(--radius-lg);
  background: linear-gradient(90deg, var(--color-border) 25%, #eef1ee 50%, var(--color-border) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

.enrollment-section { display: flex; flex-direction: column; gap: var(--space-4); }

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  flex-wrap: wrap;
}
.page-toolbar h3 { margin: 0; font-size: var(--font-size-md); }

.status-select {
  width: 100%;                                                 /* 填滿固定寬度的儲存格，不再依選到的文字長度自己撐寬/縮窄 */
  min-height: 36px;
  padding: 4px var(--space-2);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-bg-surface);
  font-size: var(--font-size-sm);
}

.empty-note {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  margin-top: calc(-1 * var(--space-2));
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-6);
  color: var(--color-text-muted);
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-5);
}
</style>
