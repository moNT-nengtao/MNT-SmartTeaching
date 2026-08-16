<template>
  <div class="layout-container">
    <Sidebar />
    <div class="layout-main" :class="{ collapsed: appStore.sidebarCollapsed }">
      <Header />
      <div class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: var(--sidebar-width);
  transition: margin-left var(--transition-duration);
  overflow: hidden;
}

.layout-main.collapsed {
  margin-left: var(--sidebar-collapsed-width);
}

.layout-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: var(--bg-color);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
