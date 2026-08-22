<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import DataTable from '../components/common/DataTable.vue'
import AppButton from '../components/common/AppButton.vue'
import AppModal from '../components/common/AppModal.vue'
import FormField from '../components/common/FormField.vue'
import Badge from '../components/common/Badge.vue'
import Icon from '../components/common/Icon.vue'
import FilterDropdown from '../components/common/FilterDropdown.vue'
import MiniStat from '../components/common/MiniStat.vue'
import { getPeople, createPerson, updatePerson } from '../api/person.js'
import { personStatusMeta, PERSON_STATUS_OPTIONS } from '../utils/statusMeta.js'
import { useToastStore } from '../stores/toast.js'
import { extractErrorMessage } from '../utils/errorMessage.js'
import { debounce } from '../utils/debounce.js'

// Module 1：人員管理頁（Issue #6）－ List / Create / Update
// 分頁／搜尋／排序都改成呼叫後端 API 處理（配合預期 200 筆以上的測試資料量，
// 前端不再把全部人員抓回來自己處理），DataTable.vue 只負責畫「目前這一頁」長什麼樣子。
const toast = useToastStore()

const PAGE_SIZE = 10
const people = ref([])                                          // 目前這一頁的資料
const loading = ref(true)
const totalItems = ref(0)
const page = ref(1)                                             // 畫面上的頁碼，1-indexed；呼叫 API 時要轉成後端的 0-indexed

const COLUMNS = [
  { key: 'name', label: '姓名', width: '20%' },
  { key: 'email', label: 'Email', width: '28%' },
  { key: 'phone', label: '電話', width: '16%' },
  { key: 'status', label: '狀態', width: '18%' },              // 固定寬度：狀態徽章文字長度不同（在學/停用）不會再讓欄位跳動
  { key: 'actions', label: '操作', width: '18%' },
]

// ---- 搜尋 + 狀態篩選：現在會實際打 API（帶 search/status query 參數），不是純前端過濾 ----
const searchQuery = ref('')
const statusFilter = ref('ALL')                                 // 'ALL' | 'ACTIVE' | 'INACTIVE'
const STATUS_FILTER_OPTIONS = [
  { value: 'ALL', label: '全部' },
  ...PERSON_STATUS_OPTIONS.map((value) => ({ value, label: personStatusMeta(value).label })),
]

async function loadPeople() {
  loading.value = true
  try {
    const result = await getPeople({
      page: page.value - 1,
      size: PAGE_SIZE,
      search: searchQuery.value.trim(),
      status: statusFilter.value === 'ALL' ? '' : statusFilter.value,
    })
    people.value = result.content
    totalItems.value = result.totalElements
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取人員列表失敗'))
  } finally {
    loading.value = false
  }
}

function handlePageChange(newPage) {
  page.value = newPage
  loadPeople()
}

// 搜尋輸入防抖：不要每打一個字就重打一次 API，等使用者停下來一下才真的查詢；
// 換頁碼/篩選狀態不用防抖，本來就是離散的單一動作
const debouncedSearch = debounce(() => {
  page.value = 1
  loadPeople()
}, 400)
watch(searchQuery, debouncedSearch)
watch(statusFilter, () => {
  page.value = 1
  loadPeople()
})

// ---- 統計數字（學員總數／在學／停用）：獨立於目前的搜尋/分頁狀態，永遠反映系統裡的真實總數 ----
// 分頁之後 people 陣列只剩「目前這一頁」的資料，沒辦法從裡面算出在學/停用各有幾筆，
// 所以另外發 3 支「只要總筆數、不要內容」的輕量查詢（size 給 1，只讀 totalElements）算出來，
// 這樣搜尋/篩選主列表時，這幾個數字也不會跟著抖動，永遠顯示系統裡的實際總數。
const totalPeopleCount = ref(0)
const activePeopleCount = ref(0)
const inactivePeopleCount = ref(0)

async function loadStats() {
  try {
    const [all, active, inactive] = await Promise.all([
      getPeople({ page: 0, size: 1 }),
      getPeople({ page: 0, size: 1, status: 'ACTIVE' }),
      getPeople({ page: 0, size: 1, status: 'INACTIVE' }),
    ])
    totalPeopleCount.value = all.totalElements
    activePeopleCount.value = active.totalElements
    inactivePeopleCount.value = inactive.totalElements
  } catch {
    // 統計數字讀取失敗不影響主要列表功能，安靜失敗就好，不用額外跳 toast 打擾使用者
  }
}

onMounted(() => {
  loadPeople()
  loadStats()
})

// ---- 新增 / 編輯共用的表單狀態 ----
const isModalOpen = ref(false)
const editingId = ref(null)                                    // null = 新增模式；有值 = 編輯模式
const isSubmitting = ref(false)
const form = reactive({ name: '', email: '', phone: '', status: 'ACTIVE' })
const errors = reactive({ name: '', email: '' })

const modalTitle = computed(() => (editingId.value ? '編輯人員資料' : '新增人員'))

function resetForm() {
  form.name = ''
  form.email = ''
  form.phone = ''
  form.status = 'ACTIVE'
  errors.name = ''
  errors.email = ''
}

function openCreateModal() {
  editingId.value = null
  resetForm()
  isModalOpen.value = true
}

function openEditModal(person) {
  editingId.value = person.id
  form.name = person.name
  form.email = person.email
  form.phone = person.phone ?? ''
  form.status = person.status
  errors.name = ''
  errors.email = ''
  isModalOpen.value = true
}

function closeModal() {
  isModalOpen.value = false
}

// 送出前先做簡單的前端驗證（在 blur/submit 時才驗證，不要打字每一下就跳錯誤，符合 inline-validation 準則）
function validate() {
  errors.name = form.name.trim() ? '' : '姓名為必填欄位'
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  errors.email = emailPattern.test(form.email) ? '' : '請輸入正確的 Email 格式'
  return !errors.name && !errors.email
}

async function handleSubmit() {
  if (!validate()) return
  isSubmitting.value = true
  try {
    if (editingId.value) {
      // 注意：docs/03_api_spec.md 的 PUT /api/people/{id} 範例只示範了 name/phone，
      // 沒有明確列出 status 能不能改。狀態切換（在學/停用）是必要的管理功能，所以這裡還是把 status 一併送出，
      // 等後端實作 PersonController 時，要記得讓 PersonUpdateRequest 也接受 status 欄位，不然這裡會被忽略。
      await updatePerson(editingId.value, { name: form.name, phone: form.phone, status: form.status })
      toast.success('已更新人員資料')
    } else {
      await createPerson({ name: form.name, email: form.email, phone: form.phone })
      toast.success('已新增人員')
    }
    closeModal()
    await loadPeople()                                          // 重新讀取目前這一頁，確保畫面跟資料來源一致
    await loadStats()                                            // 新增/編輯後總數或在學/停用分佈可能改變，統計數字也要一起更新
  } catch (error) {
    // 用 extractErrorMessage 取後端真正的錯誤原因（例如「Email 重複」），抓不到才用通用文字頂著
    toast.error(extractErrorMessage(error, editingId.value ? '更新人員失敗' : '新增人員失敗'))
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="page">
    <!-- 學員總數/在學/停用 小型統計：透過 Teleport 塞進 AppNavbar.vue 的導覽列裡顯示（見該檔案的 #navbar-stats-target），
         不是直接畫在頁面內容裡——之前放在頁面裡、浮在導覽列跟工具列中間，回饋是看起來很突兀 -->
    <Teleport to="#navbar-stats-target">
      <MiniStat label="學員總數" :value="totalPeopleCount" icon="users" accent="var(--color-brand-600)" label-first />
      <MiniStat label="在學" :value="activePeopleCount" icon="check" accent="var(--color-brand-900)" label-first />
      <MiniStat label="停用" :value="inactivePeopleCount" icon="close" accent="var(--color-text-muted)" label-first />
    </Teleport>

    <!-- 搜尋在左、新增＋篩選靠右並排在同一列，排版方式參考你提供的圖片 -->
    <div class="list-toolbar">
      <div class="search-box">
        <Icon name="search" :size="16" class="search-icon" />
        <input
          v-model="searchQuery"
          type="search"
          placeholder="搜尋姓名或 Email…"
          aria-label="搜尋人員：依姓名或 Email"
        />
      </div>

      <div class="toolbar-actions">
        <AppButton variant="primary" icon-only tooltip="新增人員" @click="openCreateModal">
          <Icon name="plus" :size="16" />
        </AppButton>
        <FilterDropdown v-model="statusFilter" label="依狀態篩選" :options="STATUS_FILTER_OPTIONS" />
      </div>
    </div>

    <DataTable
      :columns="COLUMNS"
      :rows="people"
      :loading="loading"
      empty-message="找不到符合搜尋條件的人員"
      :page="page"
      :page-size="PAGE_SIZE"
      :total-items="totalItems"
      @update:page="handlePageChange"
    >
      <template #cell-name="{ row }">
        <!-- 姓名連到詳情頁：Module 1 的「查詢單一人員」＋ Module 3 的修課紀錄都整合在詳情頁裡 -->
        <RouterLink :to="`/people/${row.id}`" class="name-link">{{ row.name }}</RouterLink>
      </template>
      <template #cell-status="{ row }">
        <Badge
          :color="personStatusMeta(row.status).color"
          :label="personStatusMeta(row.status).label"
          :icon="personStatusMeta(row.status).icon"
          :solid="personStatusMeta(row.status).solid"
        />
      </template>
      <template #cell-actions="{ row }">
        <!-- 改成純圖示按鈕，滑鼠停留/鍵盤 focus 才彈出文字提示，操作欄不會被文字撐得很寬 -->
        <div class="row-actions">
          <AppButton variant="ghost" icon-only tooltip="學習記錄管理" :to="`/people/${row.id}`">
            <Icon name="users" :size="16" />
          </AppButton>
          <AppButton variant="ghost" icon-only tooltip="編輯人員" @click="openEditModal(row)">
            <Icon name="pencil" :size="16" />
          </AppButton>
        </div>
      </template>
    </DataTable>

    <AppModal v-if="isModalOpen" :title="modalTitle" @close="closeModal">
      <form @submit.prevent="handleSubmit">
        <FormField id="person-name" label="姓名" required :error="errors.name" v-slot="{ describedBy }">
          <input id="person-name" v-model="form.name" type="text" :aria-describedby="describedBy" autocomplete="name" />
        </FormField>

        <FormField
          id="person-email"
          label="Email"
          required
          :error="errors.email"
          :hint="editingId ? 'Email 建立後不可修改（避免帳號識別衝突）' : ''"
          v-slot="{ describedBy }"
        >
          <input
            id="person-email"
            v-model="form.email"
            type="email"
            :aria-describedby="describedBy"
            :disabled="Boolean(editingId)"
            autocomplete="email"
          />
        </FormField>

        <FormField id="person-phone" label="電話" v-slot="{ describedBy }">
          <input id="person-phone" v-model="form.phone" type="tel" :aria-describedby="describedBy" autocomplete="tel" />
        </FormField>

        <!-- 狀態只在「編輯」時可以改：新增人員一律從 ACTIVE 開始（對應 API 規格新增回應的預設值），
             這裡就是狀態被控管的地方——之前沒有這欄位，是漏掉了，所以列表上一直看得到徽章、卻找不到能改的地方 -->
        <FormField v-if="editingId" id="person-status" label="狀態" v-slot="{ describedBy }">
          <select id="person-status" v-model="form.status" :aria-describedby="describedBy">
            <option v-for="option in PERSON_STATUS_OPTIONS" :key="option" :value="option">
              {{ personStatusMeta(option).label }}
            </option>
          </select>
        </FormField>

        <div class="form-actions">
          <AppButton variant="secondary" type="button" @click="closeModal">取消</AppButton>
          <AppButton variant="primary" type="submit" :loading="isSubmitting">
            {{ editingId ? '儲存變更' : '確認新增' }}
          </AppButton>
        </div>
      </form>
    </AppModal>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: var(--space-4); }

/* 搜尋在左（可伸縮）、新增＋篩選靠右並排，同一列 —— 排版方式參考提供的圖片 */
.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

.search-box {
  position: relative;
  flex: 1 1 280px;                                              /* 可以伸縮，但不會小於 280px */
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
  border-radius: var(--radius-pill);                            /* 膠囊造型的搜尋框，跟圖片參考一致 */
  background: var(--color-bg-surface);
  font-size: var(--font-size-sm);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}
.search-box input:focus-visible {
  outline: none;
  border-color: var(--color-brand-600);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-brand-600) 20%, transparent);
}

.name-link {
  font-weight: 600;
  color: var(--color-text-primary);
}
.name-link:hover { color: var(--color-brand-600); }

.row-actions { display: flex; gap: var(--space-2); flex-wrap: wrap; align-items: center; }

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-5);
}
</style>
