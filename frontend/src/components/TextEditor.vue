<script setup>
import { ref, watch, onMounted } from 'vue';
import api from '../services/api';
import VintageIcon from './VintageIcons.vue';

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
          <VintageIcon name="robot" size="medium" style="margin-right: 0.3rem;" />
          AI 助手
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
      <VintageIcon name="empty" size="large" class="icon" />
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
  background: #f5f3f0;
  position: relative;
  /* 木纹桌面背景 */
  background-image: 
    linear-gradient(90deg, rgba(139, 115, 85, 0.02) 1px, transparent 1px),
    linear-gradient(rgba(139, 115, 85, 0.02) 1px, transparent 1px);
  background-size: 20px 20px;
}

.toolbar {
  height: 56px;
  border-bottom: 1px solid #e8e4dc;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  background: #faf8f5;
  z-index: 10;
  box-shadow: 0 1px 3px rgba(101, 84, 60, 0.08);
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
  border-radius: 6px;
  cursor: pointer;
  color: #8b7355;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.tool-btn:hover {
  background: #f0ebe3;
  color: #5d4e37;
}

.separator {
  width: 1px;
  height: 24px;
  background: #e5e7eb;
  margin: 0 0.5rem;
}

.ai-toggle-btn {
  border: 1px solid #d4c5b0;
  color: #6b5b45;
  background: #f5ede0;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.3rem;
}

.ai-toggle-btn:hover {
  background: #ebe0cf;
  border-color: #c4b59f;
  box-shadow: 0 2px 4px rgba(107, 91, 69, 0.15);
}

.ai-toggle-btn :deep(.vintage-icon) {
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.2));
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2.5rem 4rem 1rem 4rem;
  background: transparent;
}

.title-input {
  font-size: 2.2rem;
  font-weight: 700;
  border: none;
  background: transparent;
  color: #3a3025;
  width: 100%;
  outline: none;
  padding: 0.5rem 0;
  font-family: 'Songti SC', 'STSong', 'SimSun', serif;
  line-height: 1.4;
  letter-spacing: 0.02em;
}

.title-input::placeholder {
  color: #c4b59f;
}

.editor-content-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  overflow-y: auto;
  padding: 1.5rem 2rem 2rem 2rem;
}

/* Custom Scrollbar */
.editor-content-wrapper::-webkit-scrollbar {
  width: 10px;
}
.editor-content-wrapper::-webkit-scrollbar-track {
  background: #f5f3f0;
}
.editor-content-wrapper::-webkit-scrollbar-thumb {
  background-color: #d4c5b0;
  border-radius: 5px;
  border: 2px solid #f5f3f0;
}
.editor-content-wrapper::-webkit-scrollbar-thumb:hover {
  background-color: #c4b59f;
}

.content-editor {
  width: 100%;
  max-width: 750px;
  height: auto;
  min-height: calc(100% - 4rem);
  resize: none;
  border: none;
  /* 纸张效果 */
  background: #fffef9;
  background-image: 
    /* 纸张纹理 */
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 31px,
      rgba(139, 115, 85, 0.03) 31px,
      rgba(139, 115, 85, 0.03) 32px
    );
  font-family: 'Songti SC', 'STSong', 'SimSun', 'Noto Serif SC', 'Georgia', serif;
  font-size: 1.15rem;
  line-height: 2;
  color: #2d2419;
  outline: none;
  padding: 3rem 4rem 6rem 4rem;
  /* 纸张阴影和边框 */
  box-shadow: 
    0 0 0 1px rgba(139, 115, 85, 0.08),
    0 2px 4px rgba(101, 84, 60, 0.06),
    0 8px 16px rgba(101, 84, 60, 0.08),
    inset 0 0 100px rgba(255, 250, 240, 0.3);
  border-radius: 2px;
  margin: 0 auto;
}

.status-bar {
  height: 36px;
  border-top: 1px solid #e8e4dc;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  background: #faf8f5;
  font-size: 0.85rem;
  color: #a08968;
  font-family: -apple-system, BlinkMacSystemFont, sans-serif;
  box-shadow: 0 -1px 3px rgba(101, 84, 60, 0.05);
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
  color: #c2893a;
  font-weight: 500;
}

.status.saved {
  color: #7a9b6f;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #a08968;
  background: #f5f3f0;
}

.empty-content {
  text-align: center;
  background: #fffef9;
  padding: 3rem;
  border-radius: 8px;
  box-shadow: 
    0 0 0 1px rgba(139, 115, 85, 0.08),
    0 4px 12px rgba(101, 84, 60, 0.08),
    0 8px 24px rgba(101, 84, 60, 0.06);
  border: 1px solid #e8e4dc;
}

.empty-content .icon {
  display: block;
  margin: 0 auto 1.5rem;
  opacity: 0.6;
  filter: drop-shadow(2px 2px 4px rgba(101, 84, 60, 0.2));
}

.empty-content p {
  font-size: 1.1rem;
  color: #8b7355;
  font-family: 'Songti SC', 'STSong', serif;
}
</style>
