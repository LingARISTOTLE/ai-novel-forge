<script setup>
import { ref, watch, onMounted } from 'vue';
import api from '../services/api';

const props = defineProps({
  chapterId: Number
});

const emit = defineEmits(['update-chapter', 'toggle-ai']);

const chapter = ref(null);
const loading = ref(false);
const saving = ref(false);
const wordCount = ref(0);
const lastSavedTime = ref(null);
let saveTimeout = null;

watch(() => props.chapterId, async (newId) => {
  if (newId) {
    await loadChapter(newId);
    updateStats();
  } else {
    chapter.value = null;
  }
});

async function loadChapter(id) {
  loading.value = true;
  try {
    const response = await api.getChapter(id);
    chapter.value = response.data;
    lastSavedTime.value = new Date().toLocaleTimeString();
  } catch (error) {
    console.error('Failed to load chapter:', error);
  } finally {
    loading.value = false;
  }
}

// Auto-save functionality
function onInput() {
  updateStats();
  if (saveTimeout) clearTimeout(saveTimeout);
  saving.value = true;
  saveTimeout = setTimeout(async () => {
    await saveChapter();
    saving.value = false;
    lastSavedTime.value = new Date().toLocaleTimeString();
  }, 1000);
}

function updateStats() {
  if (chapter.value && chapter.value.content) {
    // 简单的字数统计，排除空白字符
    wordCount.value = chapter.value.content.replace(/\s+/g, '').length;
  } else {
    wordCount.value = 0;
  }
}

function toggleAi() {
  emit('toggle-ai');
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
    <div class="toolbar">
      <div class="left-tools">
        <button class="tool-btn" title="撤销">↩</button>
        <button class="tool-btn" title="重做">↪</button>
        <div class="separator"></div>
        <button class="tool-btn" title="加粗">B</button>
        <button class="tool-btn" title="斜体">I</button>
      </div>
      <div class="right-tools">
        <button class="ai-toggle-btn" @click="toggleAi">
          🤖 AI 助手
        </button>
      </div>
    </div>

    <div class="editor-header">
      <input v-model="chapter.title" @input="onInput" class="title-input" placeholder="输入章节标题" />
    </div>

    <div class="editor-content-wrapper">
      <textarea 
        v-model="chapter.content" 
        @input="onInput" 
        class="content-editor" 
        placeholder="开始写作..."
      ></textarea>
    </div>

    <div class="status-bar">
      <div class="stats-info">
        <span>{{ wordCount }} 字</span>
        <span class="separator">|</span>
        <span>预计阅读 {{ Math.ceil(wordCount / 300) }} 分钟</span>
      </div>
      <div class="save-info">
        <span v-if="saving" class="status saving">保存中...</span>
        <span v-else class="status saved">已保存 {{ lastSavedTime }}</span>
      </div>
    </div>
  </div>
  <div v-else class="empty-state">
    <div class="empty-content">
      <i class="icon">📝</i>
      <p>请选择一个章节开始写作</p>
    </div>
  </div>
</template>

<style scoped>
.editor-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: #ffffff;
  position: relative;
}

.toolbar {
  height: 56px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  background: #ffffff;
  z-index: 10;
}

.left-tools {
  display: flex;
  gap: 0.8rem;
  align-items: center;
}

.tool-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  color: #6b7280;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.tool-btn:hover {
  background: #f3f4f6;
  color: #111827;
}

.separator {
  width: 1px;
  height: 24px;
  background: #e5e7eb;
  margin: 0 0.5rem;
}

.ai-toggle-btn {
  border: 1px solid #bfdbfe;
  color: #2563eb;
  background: #eff6ff;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.ai-toggle-btn:hover {
  background: #dbeafe;
  border-color: #93c5fd;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.1);
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2.5rem 4rem 1rem 4rem;
  background: #ffffff;
}

.title-input {
  font-size: 2.2rem;
  font-weight: 800;
  border: none;
  background: transparent;
  color: #111827;
  width: 100%;
  outline: none;
  padding: 0.5rem 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, serif;
  line-height: 1.3;
}

.title-input::placeholder {
  color: #d1d5db;
}

.editor-content-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  overflow-y: auto;
  padding: 0 2rem;
}

/* Custom Scrollbar */
.editor-content-wrapper::-webkit-scrollbar {
  width: 8px;
}
.editor-content-wrapper::-webkit-scrollbar-thumb {
  background-color: #e5e7eb;
  border-radius: 4px;
}

.content-editor {
  width: 100%;
  max-width: 750px; /* Optimal reading width */
  height: 100%;
  resize: none;
  border: none;
  background: transparent;
  font-family: 'Georgia', 'Cambria', 'Times New Roman', serif;
  font-size: 1.2rem;
  line-height: 1.8;
  color: #374151;
  outline: none;
  padding: 0 1rem 6rem 1rem;
}

.status-bar {
  height: 36px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  background: #ffffff;
  font-size: 0.85rem;
  color: #9ca3af;
  font-family: -apple-system, BlinkMacSystemFont, sans-serif;
}

.stats-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.save-info {
  display: flex;
  align-items: center;
}

.status.saving {
  color: #f59e0b; /* Amber */
  font-weight: 500;
}

.status.saved {
  color: #10b981; /* Emerald */
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9ca3af;
  background: #fcfcfc;
}

.empty-content {
  text-align: center;
  background: white;
  padding: 3rem;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  border: 1px solid #f3f4f6;
}

.empty-content .icon {
  font-size: 3.5rem;
  display: block;
  margin-bottom: 1.5rem;
  opacity: 0.8;
}

.empty-content p {
  font-size: 1.1rem;
  color: #6b7280;
}
</style>
