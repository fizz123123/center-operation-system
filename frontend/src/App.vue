<script setup>
import AppShell from './components/layout/AppShell.vue'   // 版面骨架（Navbar + Sidebar + 內容區）
</script>

<template>
  <AppShell />                                              <!-- 全站只有一個版面骨架，內容由 router-view 換頁 -->
</template>
