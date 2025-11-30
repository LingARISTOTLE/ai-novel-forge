<script setup>
import { ref } from 'vue';
import NovelSidebar from './components/NovelSidebar.vue';
import TextEditor from './components/TextEditor.vue';
import AiAssistant from './components/AiAssistant.vue';

const selectedNovelId = ref(null);
const selectedChapterId = ref(null);
const showAiAssistant = ref(true);

function onNovelSelected(id) {
  selectedNovelId.value = id;
  selectedChapterId.value = null; // Reset chapter when switching novel
}

function onChapterSelected(id) {
  selectedChapterId.value = id;
}

function onChapterUpdated() {
  // Could trigger refresh of lists if needed
}

function toggleAiAssistant() {
  showAiAssistant.value = !showAiAssistant.value;
}
</script>

<template>
  <div class="app-container">
    <NovelSidebar 
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
  background: var(--color-background);
}

.main-content {
  flex: 1;
  height: 100%;
  min-width: 0; /* 防止 flex 子项溢出 */
  position: relative;
  background: var(--color-background);
  box-shadow: -1px 0 10px rgba(0,0,0,0.02);
  z-index: 1;
}

.right-panel {
  width: 320px;
  height: 100%;
  flex-shrink: 0;
  border-left: 1px solid var(--color-border);
  transition: transform 0.3s ease;
}
</style>
