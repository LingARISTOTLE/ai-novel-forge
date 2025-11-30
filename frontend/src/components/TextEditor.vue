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
  background: var(--color-background);
}

.toolbar {
  height: 48px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  background: #fff;
}

.left-tools {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.tool-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  color: var(--color-text);
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tool-btn:hover {
  background: var(--color-background-mute);
}

.separator {
  width: 1px;
  height: 20px;
  background: var(--color-border);
  margin: 0 0.5rem;
}

.ai-toggle-btn {
  border: 1px solid #1890ff;
  color: #1890ff;
  background: transparent;
  padding: 0.3rem 0.8rem;
  border-radius: 16px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.ai-toggle-btn:hover {
  background: #e6f7ff;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem 3rem 1rem 3rem;
  background: var(--color-background);
}

.title-input {
  font-size: 2rem;
  font-weight: 700;
  border: none;
  background: transparent;
  color: var(--color-heading);
  width: 100%;
  outline: none;
  padding: 0.5rem 0;
  font-family: inherit;
}

.title-input::placeholder {
  color: var(--vt-c-text-light-2);
  opacity: 0.4;
}

.editor-content-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  overflow-y: auto;
  padding: 0 2rem;
}

.content-editor {
  width: 100%;
  max-width: 800px;
  height: 100%;
  resize: none;
  border: none;
  background: transparent;
  font-family: 'Georgia', 'Source Serif Pro', serif;
  font-size: 1.25rem;
  line-height: 1.8;
  color: var(--color-text);
  outline: none;
  padding: 0 1rem 4rem 1rem;
}

.status-bar {
  height: 32px;
  border-top: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  background: #f8f9fa;
  font-size: 0.8rem;
  color: var(--vt-c-text-light-2);
}

.stats-info {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.save-info {
  display: flex;
  align-items: center;
}

.status.saving {
  color: #e6a23c;
}

.status.saved {
  color: #67c23a;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--vt-c-text-light-2);
  background: var(--color-background-soft);
}

.empty-content {
  text-align: center;
}

.empty-content .icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-content p {
  font-size: 1.1rem;
}
</style>
