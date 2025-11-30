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

// Expose methods to parent
defineExpose({
  refreshChapters: () => {
    if (props.selectedNovelId) {
      fetchChapters(props.selectedNovelId);
    }
  }
});

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
  background: #fcfcfc;
  border-right: 1px solid var(--color-border);
  height: 100%;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.section {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
}

.novels-section {
  border-bottom: 1px solid var(--color-border);
  max-height: 40%;
  min-height: 200px;
  overflow-y: auto;
}

.chapters-section {
  flex: 1;
  overflow-y: auto;
  background: #fcfcfc;
}

.empty-selection {
  color: #9ca3af;
  font-size: 0.9rem;
  text-align: center;
  padding-top: 3rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2rem;
}

.header h3 {
  font-size: 0.95rem;
  font-weight: 700;
  color: #374151; /* Dark gray */
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.add-btn {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  color: #6b7280;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.add-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  color: #111827;
  transform: translateY(-1px);
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.list li {
  padding: 0.75rem 1rem;
  cursor: pointer;
  border-radius: 8px;
  margin-bottom: 4px;
  transition: all 0.2s ease;
  font-size: 0.95rem;
  color: #4b5563;
  border: 1px solid transparent;
}

.list li:hover {
  background: #ffffff;
  color: #111827;
  box-shadow: 0 2px 5px rgba(0,0,0,0.03);
}

.list li.active {
  background: #ffffff;
  color: #2563eb; /* Brand blue */
  font-weight: 600;
  border-color: #e5e7eb;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.08);
  border-left: 3px solid #2563eb;
}

.novel-item, .chapter-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.novel-icon, .chapter-icon {
  font-size: 1.1rem;
  opacity: 0.6;
}

.list li.active .novel-icon,
.list li.active .chapter-icon {
  opacity: 1;
  transform: scale(1.1);
  transition: transform 0.2s;
}

.chapter-title, .novel-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty {
  color: #9ca3af;
  font-size: 0.9rem;
  padding: 1.5rem;
  text-align: center;
  background: repeating-linear-gradient(
    45deg,
    #f9fafb,
    #f9fafb 10px,
    #f3f4f6 10px,
    #f3f4f6 20px
  );
  border-radius: 8px;
  border: 1px dashed #e5e7eb;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #ffffff;
  padding: 2rem;
  border-radius: 16px;
  width: 380px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1), 0 4px 10px rgba(0,0,0,0.05);
  border: 1px solid #f3f4f6;
  transform: scale(1);
  animation: modalPop 0.2s ease-out;
}

@keyframes modalPop {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.modal h4 {
  margin-bottom: 1.5rem;
  font-size: 1.2rem;
  font-weight: 700;
  color: #111827;
}

.modal input {
  width: 100%;
  padding: 0.8rem 1rem;
  margin-bottom: 1.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  background: #f9fafb;
  transition: all 0.2s;
}

.modal input:focus {
  outline: none;
  border-color: #2563eb;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.modal .actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
}

.modal button {
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  border: none;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-cancel {
  background: #f3f4f6;
  color: #4b5563;
}

.btn-cancel:hover {
  background: #e5e7eb;
  color: #111827;
}

.btn-primary {
  background: #2563eb;
  color: white;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.btn-primary:hover {
  background: #1d4ed8;
  box-shadow: 0 4px 8px rgba(37, 99, 235, 0.3);
  transform: translateY(-1px);
}
</style>
