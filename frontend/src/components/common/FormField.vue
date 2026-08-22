<script setup>
import { computed } from 'vue'

// 表單欄位的共用外殼：負責畫「看得見的 label」＋「說明文字」＋「錯誤訊息」，
// 實際的 <input>/<select>/<textarea> 由使用端透過 slot 自己放，維持彈性。
//
// 用法範例：
// <FormField id="name" label="姓名" required :error="errors.name" v-slot="{ describedBy }">
//   <input id="name" v-model="form.name" :aria-describedby="describedBy" />
// </FormField>
const props = defineProps({
  id: { type: String, required: true },
  label: { type: String, required: true },
  required: { type: Boolean, default: false },
  hint: { type: String, default: '' },
  error: { type: String, default: '' },
})

const errorId = computed(() => `${props.id}-error`)
const hintId = computed(() => `${props.id}-hint`)
// 沒有錯誤時把 hint 的 id 帶給 input，有錯誤時改帶 error 的 id，
// 這樣螢幕閱讀器唸出欄位時，永遠只會唸到「目前真正該唸的那一句」
const describedBy = computed(() => (props.error ? errorId.value : props.hint ? hintId.value : undefined))
</script>

<template>
  <div class="field">
    <label :for="id">
      {{ label }}
      <span v-if="required" class="required-mark" aria-hidden="true">*</span>
    </label>

    <slot :described-by="describedBy" />

    <p v-if="!error && hint" :id="hintId" class="hint">{{ hint }}</p>
    <p v-if="error" :id="errorId" class="error" role="alert">{{ error }}</p>
    <!-- role="alert"：欄位驗證失敗時，螢幕閱讀器會立刻唸出錯誤內容，不用使用者自己找 -->
  </div>
</template>

<style scoped>
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: var(--space-4);
}

label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

.required-mark {
  color: var(--color-accent-warn);                            /* 必填星號用暖色點綴，跟一般文字明顯區隔 */
}

.hint {
  margin: 0;
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.error {
  margin: 0;
  font-size: var(--font-size-xs);
  color: #b3401f;                                              /* 由 --color-accent-warn 加深，確保小字對比足夠 */
  font-weight: 600;
}

/* 統一表單元件外觀（input/select/textarea 共用），寫在這裡用 :deep 往 slot 內部套 */
.field :deep(input),
.field :deep(select),
.field :deep(textarea) {
  width: 100%;
  min-height: 44px;                                           /* 觸控熱區 44px，touch-target-size 準則 */
  padding: 10px var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg-surface);
  font-size: var(--font-size-base);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}
.field :deep(textarea) { min-height: 80px; resize: vertical; }

.field :deep(input:focus-visible),
.field :deep(select:focus-visible),
.field :deep(textarea:focus-visible) {
  border-color: var(--color-brand-600);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-brand-600) 20%, transparent);
  outline: none;                                               /* 用 box-shadow 自製更明顯的焦點樣式，取代預設 outline */
}
</style>
