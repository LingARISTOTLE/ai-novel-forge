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
    <div class="section">
      <div class="header">
        <h3>My Novels</h3>
        <button @click="showCreateNovelModal = true">+</button>
      </div>
      <ul class="list">
        <li 
          v-for="novel in novels" 
          :key="novel.id" 
          :class="{ active: novel.id === selectedNovelId }"
          @click="selectNovel(novel.id)"
        >
          {{ novel.title }}
        </li>
      </ul>

      <div v-if="showCreateNovelModal" class="modal-overlay">
        <div class="modal">
          <h4>New Novel</h4>
          <input v-model="newNovelTitle" placeholder="Novel Title" @keyup.enter="createNovel" />
          <div class="actions">
            <button @click="createNovel">Create</button>
            <button @click="showCreateNovelModal = false">Cancel</button>
          </div>
        </div>
      </div>
    </div>

    <div class="section" v-if="selectedNovelId">
      <div class="header">
        <h3>Chapters</h3>
        <button @click="createChapter">+</button>
      </div>
      <ul class="list">
        <li 
          v-for="chapter in chapters" 
          :key="chapter.id"
          :class="{ active: chapter.id === selectedChapterId }"
          @click="selectChapter(chapter.id)"
        >
          {{ chapter.title }}
        </li>
        <li v-if="chapters.length === 0" class="empty">No chapters yet</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.sidebar {
  width: 250px;
  background: var(--color-background-soft);
  border-right: 1px solid var(--color-border);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.section {
  padding: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.header h3 {
  font-size: 1rem;
  font-weight: bold;
  margin: 0;
}

.header button {
  background: none;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  cursor: pointer;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.list li {
  padding: 0.5rem;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 2px;
}

.list li:hover {
  background: var(--color-background-mute);
}

.list li.active {
  background: hsla(160, 100%, 37%, 0.2);
  color: hsla(160, 100%, 37%, 1);
}

.empty {
  color: var(--vt-c-text-light-2);
  font-size: 0.9rem;
  font-style: italic;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: var(--color-background);
  padding: 1.5rem;
  border-radius: 8px;
  width: 300px;
}

.modal h4 {
  margin-bottom: 1rem;
}

.modal input {
  width: 100%;
  padding: 0.5rem;
  margin-bottom: 1rem;
}

.modal .actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
</style>
