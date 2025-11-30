<script setup>
import { ref } from 'vue';
import NovelSidebar from './components/NovelSidebar.vue';
import TextEditor from './components/TextEditor.vue';

const selectedNovelId = ref(null);
const selectedChapterId = ref(null);

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
      />
    </main>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.main-content {
  flex: 1;
  height: 100%;
  background: var(--color-background);
}
</style>
