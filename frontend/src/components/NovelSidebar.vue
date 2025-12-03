<script setup>
import { ref, onMounted, watch } from 'vue';
import api from '../services/api';
import VintageIcon from './VintageIcons.vue';

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
            <VintageIcon name="book" size="medium" class="novel-icon" />
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
              <VintageIcon name="paper" size="small" class="chapter-icon" />
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
  background: linear-gradient(135deg, #4a3f34 0%, #3d3329 100%);
  border-right: 3px solid #8b7355;
  height: 100%;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  font-family: 'Songti SC', 'STSong', 'SimSun', serif;
  /* 皮革质感纹理 */
  background-image: 
    /* 微妙的皮革颗粒 */
    radial-gradient(ellipse at 20% 30%, rgba(139, 115, 85, 0.1) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 70%, rgba(93, 78, 55, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(139, 115, 85, 0.05) 0%, transparent 50%),
    /* 书脊装饰线 */
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 80px,
      rgba(212, 175, 55, 0.15) 80px,
      rgba(212, 175, 55, 0.15) 81px,
      transparent 81px,
      transparent 82px,
      rgba(139, 115, 85, 0.2) 82px,
      rgba(139, 115, 85, 0.2) 83px
    );
  box-shadow: 
    inset -2px 0 8px rgba(0, 0, 0, 0.3),
    inset 3px 0 6px rgba(212, 175, 55, 0.1);
}

.section {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
}

/* 书柜背景 */
.novels-section {
  border-bottom: 8px solid #3e2b1f;
  max-height: 40%;
  min-height: 200px;
  overflow-y: auto;
  position: relative;
  /* 深红木书柜背景 */
  background-color: #2c1e16;
  background-image: 
    /* 层板阴影（每80px一层，配合列表项高度） */
    repeating-linear-gradient(
      0deg,
      rgba(0,0,0,0.5) 0px,
      rgba(0,0,0,0.2) 2px,
      transparent 4px,
      transparent 76px,
      rgba(0,0,0,0.4) 78px,
      #1a110d 80px
    ),
    /* 垂直木纹 */
    repeating-linear-gradient(
      90deg,
      rgba(255,255,255,0.03) 0px,
      rgba(255,255,255,0.03) 1px,
      transparent 1px,
      transparent 3px,
      rgba(0,0,0,0.1) 4px,
      rgba(0,0,0,0.1) 5px,
      transparent 5px,
      transparent 10px
    );
  background-attachment: local; /* 让背景随内容滚动 */
  box-shadow: inset 0 0 20px #000000;
}

/* 软木图钉板背景 */
.chapters-section {
  flex: 1;
  overflow-y: auto;
  position: relative;
  /* 逼真的软木质感 */
  background-color: #c7b299;
  background-image: 
    /* 噪点层1：深色颗粒 */
    radial-gradient(rgba(139, 115, 85, 0.4) 1px, transparent 1px),
    /* 噪点层2：浅色颗粒 */
    radial-gradient(rgba(255, 255, 255, 0.3) 1px, transparent 1px),
    /* 纹理层：不规则斑点 */
    repeating-radial-gradient(
      circle at 50% 50%,
      rgba(160, 137, 104, 0.1),
      rgba(160, 137, 104, 0.1) 2px,
      transparent 3px,
      transparent 5px
    );
  background-size: 
    4px 4px,   /* 颗粒层1尺寸 */
    6px 6px,   /* 颗粒层2尺寸 */
    20px 20px; /* 纹理层尺寸 */
  background-position: 
    0 0, 
    3px 3px, 
    10px 10px;
  box-shadow: 
    inset 0 2px 10px rgba(0, 0, 0, 0.2),
    inset 0 0 30px rgba(93, 78, 55, 0.15);
}

/* 软木板木质边框 */
.chapters-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  /* 内边框 */
  border: 6px solid transparent;
  border-image: linear-gradient(to bottom right, #8b7355, #5d4e37) 1;
  opacity: 0.6;
  pointer-events: none;
  box-shadow: inset 0 0 10px rgba(0,0,0,0.3);
  z-index: 1;
}

/* 移除之前的装饰，改用更自然的阴影 */
.chapters-section::after {
  display: none;
}

.empty-selection {
  color: #f0e6d6;
  font-size: 0.9rem;
  text-align: center;
  padding-top: 3rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2rem;
  position: relative;
}

.header h3 {
  font-size: 0.85rem;
  font-weight: 700;
  color: #2d2419;
  margin: 0;
  padding: 0.5rem 1rem 0.5rem 0.8rem;
  letter-spacing: 0.15em;
  font-family: 'Georgia', 'Times New Roman', serif;
  position: relative;
  /* 复古铭牌效果 */
  background: linear-gradient(135deg, #f5ede0 0%, #e8dcc8 100%);
  border: 2px solid #8b7355;
  border-radius: 0 8px 8px 0;
  box-shadow: 
    2px 2px 4px rgba(0, 0, 0, 0.4),
    inset 1px 1px 2px rgba(255, 255, 255, 0.3),
    inset -1px -1px 2px rgba(0, 0, 0, 0.1);
  text-transform: none;
}

/* 铭牌左侧装饰 */
.header h3::before {
  content: '';
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 70%;
  background: linear-gradient(to bottom,
    rgba(139, 115, 85, 0.6),
    rgba(212, 175, 55, 0.8),
    rgba(139, 115, 85, 0.6));
  border-radius: 2px 0 0 2px;
  box-shadow: inset 1px 0 2px rgba(212, 175, 55, 0.5);
}

/* 铭牌上的小钉子装饰 */
.header h3::after {
  content: '';
  position: absolute;
  right: 0.4rem;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 4px;
  background: radial-gradient(circle, #8b7355 0%, #5d4e37 100%);
  border-radius: 50%;
  box-shadow: 
    0 1px 2px rgba(0, 0, 0, 0.5),
    inset 0 1px 1px rgba(255, 255, 255, 0.3);
}

.add-btn {
  background: linear-gradient(135deg, #8b6f47 0%, #6b5b45 100%);
  border: 1px solid #d4af37;
  border-radius: 4px;
  cursor: pointer;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  color: #fffef9;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.add-btn:hover {
  background: linear-gradient(135deg, #a08968 0%, #8b7355 100%);
  border-color: #f0d77d;
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.5);
}


.list {
  list-style: none;
  padding: 0;
  margin: 0;
}

/* 小说列表 - 书本样式 */
.novels-section .list li {
  padding: 0.75rem 0.5rem 0.75rem 1.2rem;
  cursor: pointer;
  margin-bottom: 8px;
  transition: all 0.2s ease;
  font-size: 0.95rem;
  color: #f5f0e8;
  position: relative;
  /* 书本外观 */
  background: linear-gradient(to right, 
    #5d4e37 0%, 
    #6b5b45 3%, 
    #4a3f34 4%, 
    #4a3f34 100%);
  border-radius: 0 4px 4px 0;
  border-right: 3px solid #8b7355;
  border-top: 1px solid rgba(139, 115, 85, 0.3);
  border-bottom: 1px solid rgba(0, 0, 0, 0.4);
  box-shadow: 
    inset 3px 0 3px rgba(212, 175, 55, 0.1),
    2px 2px 4px rgba(0, 0, 0, 0.3),
    inset -1px 0 2px rgba(0, 0, 0, 0.2);
}

.novels-section .list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(to bottom, 
    rgba(212, 175, 55, 0.4),
    rgba(139, 115, 85, 0.3),
    rgba(212, 175, 55, 0.4));
  border-radius: 2px 0 0 2px;
}

.novels-section .list li:hover {
  transform: translateX(3px);
  background: linear-gradient(to right, 
    #6d5e47 0%, 
    #7a6b55 3%, 
    #5a4f44 4%, 
    #5a4f44 100%);
  box-shadow: 
    inset 3px 0 3px rgba(212, 175, 55, 0.2),
    3px 3px 6px rgba(0, 0, 0, 0.4),
    inset -1px 0 2px rgba(0, 0, 0, 0.2);
}

.novels-section .list li.active {
  background: linear-gradient(to right, 
    #8b7355 0%, 
    #a08968 3%, 
    #6b5b45 4%, 
    #6b5b45 100%);
  color: #ffd700;
  font-weight: 600;
  transform: translateX(5px);
  border-right-color: #d4af37;
  box-shadow: 
    inset 3px 0 3px rgba(255, 215, 0, 0.3),
    4px 4px 8px rgba(0, 0, 0, 0.5),
    0 0 12px rgba(212, 175, 55, 0.3);
}

.novels-section .list li.active::before {
  background: linear-gradient(to bottom, 
    rgba(255, 215, 0, 0.6),
    rgba(212, 175, 55, 0.5),
    rgba(255, 215, 0, 0.6));
}

/* 章节列表 - 图钉钉纸样式 */
.chapters-section .list li {
  padding: 0.7rem 1rem 0.7rem 2.2rem;
  cursor: pointer;
  margin-bottom: 10px;
  transition: all 0.2s ease;
  font-size: 0.9rem;
  color: #3a3025;
  position: relative;
  /* 纸张效果 */
  background: linear-gradient(135deg, #fffef9 0%, #f5ede0 100%);
  border-radius: 3px;
  border: 1px solid #e8e4dc;
  box-shadow: 
    2px 2px 4px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(139, 115, 85, 0.1);
  transform: rotate(0deg);
}

/* 图钉效果 */
.chapters-section .list li::before {
  content: '📌';
  position: absolute;
  left: 0.6rem;
  top: 50%;
  transform: translateY(-50%) rotate(-15deg);
  font-size: 1rem;
  filter: drop-shadow(1px 1px 2px rgba(0, 0, 0, 0.3));
}

.chapters-section .list li:hover {
  transform: rotate(0.5deg) translateY(-1px);
  box-shadow: 
    3px 3px 6px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(139, 115, 85, 0.2);
  background: linear-gradient(135deg, #ffffff 0%, #f5f0e8 100%);
}

.chapters-section .list li.active {
  background: linear-gradient(135deg, #fff9e6 0%, #f5ede0 100%);
  color: #5d4e37;
  font-weight: 600;
  border-color: #d4af37;
  box-shadow: 
    3px 3px 8px rgba(0, 0, 0, 0.3),
    0 0 12px rgba(212, 175, 55, 0.2),
    inset 0 0 20px rgba(255, 215, 0, 0.1);
  transform: rotate(0deg) translateY(-2px);
}

.chapters-section .list li.active::before {
  transform: translateY(-50%) rotate(-10deg) scale(1.1);
  filter: drop-shadow(1px 1px 3px rgba(212, 175, 55, 0.5));
}

.novel-item, .chapter-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

/* 小说图标 */
.novels-section .novel-icon {
  opacity: 0.9;
  filter: drop-shadow(1px 1px 2px rgba(0, 0, 0, 0.4));
  transition: all 0.2s;
}

.novels-section .list li.active .novel-icon {
  opacity: 1;
  transform: scale(1.15);
  filter: drop-shadow(0 2px 4px rgba(255, 215, 0, 0.5));
}

/* 章节图标隐藏（用图钉代替） */
.chapters-section .chapter-icon {
  display: none;
}

.novel-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.chapter-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: 'Songti SC', 'STSong', serif;
}

.empty {
  color: #d4c5b0;
  font-size: 0.9rem;
  padding: 1.5rem;
  text-align: center;
  background: rgba(58, 47, 36, 0.4);
  border-radius: 4px;
  border: 1px dashed #8b7355;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: linear-gradient(135deg, #fffef9 0%, #f5ede0 100%);
  padding: 2rem;
  border-radius: 8px;
  width: 380px;
  box-shadow: 
    0 0 0 2px #8b7355,
    0 10px 25px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  border: 2px solid #c4b59f;
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
  color: #3a3025;
  font-family: 'Songti SC', 'STSong', serif;
  text-align: center;
  letter-spacing: 0.1em;
}

.modal input {
  width: 100%;
  padding: 0.8rem 1rem;
  margin-bottom: 1.5rem;
  border: 2px solid #d4c5b0;
  border-radius: 4px;
  font-size: 1rem;
  background: #fffef9;
  transition: all 0.2s;
  font-family: 'Songti SC', 'STSong', serif;
  color: #3a3025;
}

.modal input:focus {
  outline: none;
  border-color: #8b7355;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(139, 115, 85, 0.2);
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
  background: #e8e4dc;
  color: #6b5b45;
  border: 1px solid #c4b59f;
}

.btn-cancel:hover {
  background: #d4c5b0;
  color: #3a3025;
}

.btn-primary {
  background: linear-gradient(135deg, #8b6f47 0%, #6b5b45 100%);
  color: #fffef9;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  border: 1px solid #5d4e37;
}

.btn-primary:hover {
  background: linear-gradient(135deg, #a08968 0%, #8b7355 100%);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.4);
  transform: translateY(-1px);
}
</style>
