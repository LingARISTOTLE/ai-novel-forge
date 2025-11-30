<script setup>
import { ref, onMounted, watch } from 'vue';
import api from '../services/api';

const props = defineProps({
  selectedNovelId: Number,
  selectedChapterId: Number
});

const emit = defineEmits(['select-novel', 'select-chapter']);

const novels = ref([]);
const chapters = ref([]);
const showCreateNovelModal = ref(false);
const newNovelTitle = ref('');

onMounted(async () => {
  await fetchNovels();
});

// Watch for novel selection to fetch chapters
watch(() => props.selectedNovelId, async (newVal) => {
  if (newVal) {
    await fetchChapters(newVal);
  } else {
    chapters.value = [];
  }
});

async function fetchNovels() {
  try {
    const response = await api.getNovels();
    novels.value = response.data;
  } catch (error) {
    console.error('Failed to fetch novels:', error);
  }
}

async function fetchChapters(novelId) {
  try {
    const response = await api.getChapters(novelId);
    chapters.value = response.data;
  } catch (error) {
    console.error('Failed to fetch chapters:', error);
  }
}

async function createNovel() {
  if (!newNovelTitle.value) return;
  try {
    const response = await api.createNovel({ title: newNovelTitle.value, description: '' });
    novels.value.push(response.data);
    newNovelTitle.value = '';
    showCreateNovelModal.value = false;
    selectNovel(response.data.id);
  } catch (error) {
    console.error('Failed to create novel:', error);
  }
}

async function createChapter() {
  if (!props.selectedNovelId) return;
  try {
    const response = await api.createChapter(props.selectedNovelId, { title: 'New Chapter', content: '' });
    chapters.value.push(response.data);
    selectChapter(response.data.id);
  } catch (error) {
    console.error('Failed to create chapter:', error);
  }
}

function selectNovel(id) {
  emit('select-novel', id);
}

function selectChapter(id) {
  emit('select-chapter', id);
}
</script>

<template>
  <div class="sidebar">
    <div class="section novels-section">
      <div class="header">
        <h3>我的小说</h3>
        <button class="add-btn" @click="showCreateNovelModal = true" title="新建小说">
          <i class="icon">+</i>
        </button>
      </div>
      <ul class="list">
        <li 
          v-for="novel in novels" 
          :key="novel.id" 
          :class="{ active: novel.id === selectedNovelId }"
          @click="selectNovel(novel.id)"
        >
          <div class="novel-item">
            <span class="novel-icon">📖</span>
            <span class="novel-title">{{ novel.title }}</span>
          </div>
        </li>
      </ul>

      <div v-if="showCreateNovelModal" class="modal-overlay">
        <div class="modal">
          <h4>新建小说</h4>
          <input v-model="newNovelTitle" placeholder="请输入小说标题" @keyup.enter="createNovel" autofocus />
          <div class="actions">
            <button class="btn-cancel" @click="showCreateNovelModal = false">取消</button>
            <button class="btn-primary" @click="createNovel">创建</button>
          </div>
        </div>
      </div>
    </div>

    <div class="section chapters-section" v-if="selectedNovelId">
      <div class="header">
        <h3>章节列表</h3>
        <button class="add-btn" @click="createChapter" title="新建章节">
          <i class="icon">+</i>
        </button>
      </div>
      <div class="chapter-list-container">
        <ul class="list">
          <li 
            v-for="chapter in chapters" 
            :key="chapter.id"
            :class="{ active: chapter.id === selectedChapterId }"
            @click="selectChapter(chapter.id)"
          >
            <div class="chapter-item">
              <span class="chapter-icon">📄</span>
              <span class="chapter-title">{{ chapter.title }}</span>
            </div>
          </li>
          <li v-if="chapters.length === 0" class="empty">暂无章节，点击右上角创建</li>
        </ul>
      </div>
    </div>
    <div v-else class="section empty-selection">
      <p>请选择或创建一本小说</p>
    </div>
  </div>
</template>

<style scoped>
.sidebar {
  width: 280px;
  background: #f8f9fa;
  border-right: 1px solid var(--color-border);
  height: 100%;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.section {
  padding: 1.2rem;
  display: flex;
  flex-direction: column;
}

.novels-section {
  border-bottom: 1px solid var(--color-border);
  max-height: 40%;
  overflow-y: auto;
}

.chapters-section {
  flex: 1;
  overflow-y: auto;
}

.empty-selection {
  color: var(--vt-c-text-light-2);
  font-size: 0.9rem;
  text-align: center;
  padding-top: 2rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.header h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--vt-c-text-light-1);
  margin: 0;
}

.add-btn {
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  cursor: pointer;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  color: var(--vt-c-text-light-2);
}

.add-btn:hover {
  background: var(--color-background);
  color: var(--color-heading);
  border-color: var(--color-border-hover);
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.list li {
  padding: 0.6rem 0.8rem;
  cursor: pointer;
  border-radius: 6px;
  margin-bottom: 4px;
  transition: all 0.2s;
  font-size: 0.95rem;
  color: var(--color-text);
  border: 1px solid transparent;
}

.list li:hover {
  background: #fff;
  border-color: var(--color-border);
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.list li.active {
  background: #e6f7ff;
  color: #1890ff;
  font-weight: 500;
  border-color: #bae7ff;
}

.novel-item, .chapter-item {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.novel-icon, .chapter-icon {
  font-size: 1.1rem;
  opacity: 0.7;
}

.chapter-title, .novel-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty {
  color: var(--vt-c-text-light-2);
  font-size: 0.9rem;
  padding: 1rem;
  text-align: center;
  background: rgba(0,0,0,0.02);
  border-radius: 4px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: var(--color-background);
  padding: 2rem;
  border-radius: 12px;
  width: 360px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.modal h4 {
  margin-bottom: 1.5rem;
  font-size: 1.2rem;
  font-weight: 600;
}

.modal input {
  width: 100%;
  padding: 0.8rem;
  margin-bottom: 1.5rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 1rem;
}

.modal input:focus {
  outline: none;
  border-color: #1890ff;
}

.modal .actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
}

.modal button {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  border: none;
  transition: opacity 0.2s;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.modal button:hover {
  opacity: 0.9;
}
</style>
