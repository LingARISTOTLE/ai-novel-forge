<script setup>
import { ref, watch, onMounted } from 'vue';
import api from '../services/api';

const props = defineProps({
  chapterId: Number
});

const emit = defineEmits(['update-chapter']);

const chapter = ref(null);
const loading = ref(false);
const saving = ref(false);
let saveTimeout = null;

watch(() => props.chapterId, async (newId) => {
  if (newId) {
    await loadChapter(newId);
  } else {
    chapter.value = null;
  }
});

async function loadChapter(id) {
  loading.value = true;
  try {
    const response = await api.getChapter(id);
    chapter.value = response.data;
  } catch (error) {
    console.error('Failed to load chapter:', error);
  } finally {
    loading.value = false;
  }
}

// Auto-save functionality
function onInput() {
  if (saveTimeout) clearTimeout(saveTimeout);
  saving.value = true;
  saveTimeout = setTimeout(async () => {
    await saveChapter();
    saving.value = false;
  }, 1000);
}

async function saveChapter() {
  if (!chapter.value) return;
  try {
    await api.updateChapter(chapter.value.id, {
      title: chapter.value.title,
      content: chapter.value.content
    });
    emit('update-chapter', chapter.value); // Notify parent to refresh lists if title changed
  } catch (error) {
    console.error('Failed to save chapter:', error);
  }
}
</script>

<template>
  <div class="editor-container" v-if="chapter">
    <div class="editor-header">
      <input v-model="chapter.title" @input="onInput" class="title-input" placeholder="Chapter Title" />
      <span v-if="saving" class="status">Saving...</span>
      <span v-else class="status">Saved</span>
    </div>
    <textarea 
      v-model="chapter.content" 
      @input="onInput" 
      class="content-editor" 
      placeholder="Start writing..."
    ></textarea>
  </div>
  <div v-else class="empty-state">
    <p>Select a chapter to start writing.</p>
  </div>
</template>

<style scoped>
.editor-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 2rem;
  width: 100%;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.title-input {
  font-size: 1.5rem;
  font-weight: bold;
  border: none;
  background: transparent;
  color: var(--color-heading);
  width: 100%;
  outline: none;
}

.status {
  font-size: 0.8rem;
  color: var(--vt-c-text-light-2);
  white-space: nowrap;
  margin-left: 1rem;
}

.content-editor {
  flex: 1;
  resize: none;
  border: none;
  background: transparent;
  font-family: 'Georgia', serif;
  font-size: 1.1rem;
  line-height: 1.6;
  color: var(--color-text);
  outline: none;
  padding: 1rem;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--vt-c-text-light-2);
}
</style>
