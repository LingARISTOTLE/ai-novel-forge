<script setup>
import { ref } from 'vue';
import NovelSidebar from './components/NovelSidebar.vue';
import TextEditor from './components/TextEditor.vue';
import AiAssistant from './components/AiAssistant.vue';

const selectedNovelId = ref(null);
const selectedChapterId = ref(null);
const showAiAssistant = ref(true);
const sidebarRef = ref(null); // Reference to sidebar component

function onNovelSelected(id) {
  selectedNovelId.value = id;
  selectedChapterId.value = null; // Reset chapter when switching novel
}

function onChapterSelected(id) {
  selectedChapterId.value = id;
}

function onChapterUpdated() {
  // Refresh chapter list in sidebar to reflect title changes
  if (sidebarRef.value) {
    sidebarRef.value.refreshChapters();
  }
}

function toggleAiAssistant() {
  showAiAssistant.value = !showAiAssistant.value;
}
</script>

<template>
  <div class="app-container">
    <NovelSidebar 
      ref="sidebarRef"
      :selectedNovelId="selectedNovelId"
      :selectedChapterId="selectedChapterId"
      @select-novel="onNovelSelected"
      @select-chapter="onChapterSelected"
    />
    <main class="main-content">
      <TextEditor 
        :chapterId="selectedChapterId" 
        @update-chapter="onChapterUpdated"
        @toggle-ai="toggleAiAssistant"
      />
    </main>
    <!-- 遮罩层 -->
    <div 
      class="drawer-overlay" 
      :class="{ active: showAiAssistant }"
      @click="toggleAiAssistant"
    ></div>
    <!-- 抽屉面板 -->
    <aside class="right-panel drawer" :class="{ open: showAiAssistant }">
      <AiAssistant />
    </aside>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #f5f3f0;
  position: relative;
}

.main-content {
  flex: 1;
  height: 100%;
  min-width: 0; /* Prevent flex item overflow */
  position: relative;
  background: #f5f3f0;
  box-shadow: 0 0 30px rgba(101, 84, 60, 0.06);
  z-index: 1;
  transition: filter 0.3s ease;
}

/* 遮罩层 */
.drawer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(45, 36, 25, 0.4);
  backdrop-filter: blur(2px);
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
  z-index: 98;
}

.drawer-overlay.active {
  opacity: 1;
  visibility: visible;
}

/* 抽屉面板 */
.right-panel.drawer {
  position: fixed;
  top: 0;
  right: 0;
  width: 340px;
  height: 100%;
  border-left: 3px solid #8b7355;
  background: #faf8f5;
  transform: translateX(100%);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.4s ease;
  z-index: 99;
  /* 抽屉木质边缘效果 */
  box-shadow: -4px 0 12px rgba(0, 0, 0, 0.3);
}

.right-panel.drawer.open {
  transform: translateX(0);
  box-shadow: -8px 0 24px rgba(0, 0, 0, 0.4);
}

/* 抽屉把手装饰 */
.right-panel.drawer::before {
  content: '';
  position: absolute;
  left: -3px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60px;
  background: linear-gradient(to bottom,
    transparent,
    rgba(139, 115, 85, 0.5) 20%,
    rgba(212, 175, 55, 0.6) 50%,
    rgba(139, 115, 85, 0.5) 80%,
    transparent);
  border-radius: 0 2px 2px 0;
  box-shadow: inset 0 0 4px rgba(212, 175, 55, 0.4);
  transition: all 0.3s ease;
}

.right-panel.drawer.open::before {
  box-shadow: 
    inset 0 0 6px rgba(212, 175, 55, 0.6),
    0 0 8px rgba(212, 175, 55, 0.3);
}
</style>
