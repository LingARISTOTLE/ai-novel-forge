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
    <aside class="right-panel" v-show="showAiAssistant">
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
  background: #fcfcfc;
}

.main-content {
  flex: 1;
  height: 100%;
  min-width: 0; /* Prevent flex item overflow */
  position: relative;
  background: #ffffff;
  box-shadow: 0 0 30px rgba(0,0,0,0.04); /* Softer shadow */
  z-index: 1;
}

.right-panel {
  width: 340px;
  height: 100%;
  flex-shrink: 0;
  border-left: 1px solid #f0f0f0;
  background: #fcfcfc;
  transition: transform 0.3s ease;
}
</style>
