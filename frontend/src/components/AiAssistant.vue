<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import api from '../services/api';
import VintageIcon from './VintageIcons.vue';

// Simple Markdown Parser
function parseMarkdown(text) {
  if (!text) return '';
  
  // 1. Code Blocks
  let html = text.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
    lang = lang || 'text';
    return `
      <div class="code-block-wrapper">
        <div class="code-header">
          <span class="code-lang">${lang}</span>
          <div class="code-actions">
            <span class="icon">📋</span>
            <span class="icon">⤢</span>
          </div>
        </div>
        <pre><code class="language-${lang}">${code}</code></pre>
      </div>
    `;
  });

  // 2. Inline Code
  html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');

  // 3. Bold
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');

  // 4. Line breaks
  html = html.replace(/\n/g, '<br>');

  return html;
}

const messages = ref([
  { id: 1, type: 'ai', content: '你好！我是接入了 DeepSeek 的 AI 写作助手。可以在这里让我帮你生成大纲、续写剧情或设定角色。' }
]);
const userInput = ref('');
const aiState = ref('idle'); // idle, thinking, streaming
const currentView = ref('chat'); // 'chat' or 'history'
const currentConversationId = ref(null);
const conversations = ref([]);
const titleInput = ref(null);

onMounted(() => {
  loadConversations();
});

async function loadConversations() {
  try {
    const response = await api.getConversations();
    conversations.value = response.data;
  } catch (e) {
    console.error('Failed to load conversations', e);
  }
}

function toggleHistory() {
  if (currentView.value === 'chat') {
    currentView.value = 'history';
    loadConversations();
  } else {
    currentView.value = 'chat';
  }
}

async function switchConversation(id) {
  try {
    currentConversationId.value = id;
    const response = await api.getConversationMessages(id);
    messages.value = response.data.map(msg => ({
      id: msg.id,
      type: msg.role === 'assistant' ? 'ai' : 'user',
      content: msg.content
    }));
    currentView.value = 'chat';
    scrollToBottom();
  } catch (e) {
    console.error('Failed to load messages', e);
  }
}

function startNewConversation() {
  currentConversationId.value = null;
  messages.value = [{ id: Date.now(), type: 'ai', content: '你好！我是接入了 DeepSeek 的 AI 写作助手。可以在这里让我帮你生成大纲、续写剧情或设定角色。' }];
  currentView.value = 'chat';
}

async function deleteConversation(id, event) {
  event.stopPropagation();
  if (!confirm('确定删除这个对话吗？')) return;
  try {
    await api.deleteConversation(id);
    conversations.value = conversations.value.filter(c => c.id !== id);
    if (currentConversationId.value === id) {
      startNewConversation();
    }
  } catch (e) {
    console.error('Failed to delete conversation', e);
  }
}

const editingConvId = ref(null);
const editingTitle = ref('');

function startEditing(conv, event) {
  event.stopPropagation();
  editingConvId.value = conv.id;
  editingTitle.value = conv.title;
  nextTick(() => {
    // v-for ref is an array
    if (titleInput.value && titleInput.value.length > 0) {
      // Find the input that belongs to the current editing ID if needed, 
      // but since only one is rendered with v-if, the array might contain just that one or we need to be careful.
      // Actually, with v-if, ref inside v-for behaves intricately. 
      // Simple approach: The input is rendered only for one item.
      // Let's assume the last mounted one is the target or filter.
      const el = titleInput.value.find(el => el.value === conv.title); // This is tricky because value updates.
      // Better: Since only one is visible, just focus the visible one.
      const input = document.querySelector('.edit-box input');
      if (input) input.focus();
    }
  });
}

async function saveTitle(conv) {
  if (!editingTitle.value.trim() || editingTitle.value === conv.title) {
    editingConvId.value = null;
    return;
  }
  try {
    await api.updateConversation(conv.id, editingTitle.value);
    conv.title = editingTitle.value;
    editingConvId.value = null;
  } catch (e) {
    console.error('Failed to update conversation title', e);
  }
}

function cancelEditing() {
  editingConvId.value = null;
}

const statusText = computed(() => {
  switch (aiState.value) {
    case 'thinking': return '思考中...';
    case 'streaming': return '正在输入...';
    default: return '在线';
  }
});

const statusColor = computed(() => {
  switch (aiState.value) {
    case 'thinking': return '#e6a23c'; // Orange
    case 'streaming': return '#1890ff'; // Blue
    default: return '#52c41a'; // Green
  }
});

const quickActions = [
  { icon: 'wand', label: '剧情续写', prompt: '请根据当前情节续写一段...' },
  { icon: 'sparkle', label: '润色文字', prompt: '请润色这段文字，使其更优美...' },
  { icon: 'bulb', label: '生成灵感', prompt: '给我三个关于后续发展的反转灵感...' },
  { icon: 'person', label: '角色起名', prompt: '生成几个符合这种风格的角色名字...' }
];

async function sendMessage() {
  if (!userInput.value.trim() || aiState.value !== 'idle') return;
  
  const prompt = userInput.value;
  
  // 添加用户消息
  messages.value.push({
    id: Date.now(),
    type: 'user',
    content: prompt
  });

  userInput.value = '';
  aiState.value = 'thinking';
  scrollToBottom();

  try {
    // 创建一个新的空消息用于流式接收
    const aiMsgId = Date.now() + 1;
    messages.value.push({
      id: aiMsgId,
      type: 'ai',
      content: ''
    });
    
    const aiMsg = messages.value.find(m => m.id === aiMsgId);

    await api.streamChatWithAi(
      currentConversationId.value,
      prompt,
      (chunk) => {
        // 一旦收到第一个字符，状态切换为 streaming
        if (aiState.value === 'thinking') aiState.value = 'streaming';
        aiMsg.content += chunk;
        scrollToBottom();
      },
      (meta) => {
        // 收到元数据，更新 conversationId
        if (meta && meta.conversationId) {
            currentConversationId.value = meta.conversationId;
            
            // Update local list immediately
            const exists = conversations.value.find(c => c.id === meta.conversationId);
            if (!exists) {
                conversations.value.unshift({
                    id: meta.conversationId,
                    title: meta.title || '新对话',
                    updatedAt: new Date().toISOString()
                });
            }
        }
      },
      (error) => {
        console.error('AI Error:', error);
        aiMsg.content += '\n[出错: 连接中断]';
      }
    );
  } catch (error) {
    console.error('System Error:', error);
  } finally {
    aiState.value = 'idle';
    scrollToBottom();
  }
}


function useQuickAction(prompt) {
  userInput.value = prompt;
  // 自动聚焦输入框
  // 可以在这里直接发送，或者让用户修改
}

function scrollToBottom() {
  setTimeout(() => {
    const container = document.querySelector('.chat-history');
    if (container) container.scrollTop = container.scrollHeight;
  }, 100);
}
</script>

<template>
  <div class="ai-assistant">
    <div class="assistant-header">
      <div class="header-left">
        <button class="nav-btn" @click="toggleHistory" :title="currentView === 'chat' ? '历史记录' : '返回'">
          {{ currentView === 'chat' ? '☰' : '←' }}
        </button>
        <h3>{{ currentView === 'chat' ? 'AI 创意助手' : '历史对话' }}</h3>
      </div>
      <div class="header-right" v-if="currentView === 'chat'">
        <span class="status-dot" :style="{ background: statusColor }"></span>
        <span class="status-text">{{ statusText }}</span>
      </div>
    </div>
    
    <!-- History View -->
    <div class="history-view" v-if="currentView === 'history'">
      <div class="new-chat-btn" @click="startNewConversation">
        + 新对话
      </div>
      <ul class="conversation-list">
        <li 
          v-for="conv in conversations" 
          :key="conv.id"
          :class="{ active: conv.id === currentConversationId }"
          @click="switchConversation(conv.id)"
        >
          <div v-if="editingConvId === conv.id" class="edit-box" @click.stop>
            <input 
              v-model="editingTitle" 
              @keyup.enter="saveTitle(conv)" 
              @blur="saveTitle(conv)"
              @keyup.esc="cancelEditing"
              ref="titleInput"
              v-focus
            />
          </div>
          <template v-else>
            <span class="conv-title" @dblclick="startEditing(conv, $event)">{{ conv.title }}</span>
            <div class="conv-actions">
              <button class="edit-conv" @click="startEditing(conv, $event)" title="重命名">✎</button>
              <button class="delete-conv" @click="deleteConversation(conv.id, $event)" title="删除">×</button>
            </div>
          </template>
        </li>
      </ul>
    </div>

    <!-- Chat View -->
    <div class="chat-container" v-else>
      <div class="chat-history">
        <div 
          v-for="msg in messages" 
          :key="msg.id" 
          class="message"
          :class="msg.type"
        >
          <div class="avatar">
            <VintageIcon :name="msg.type === 'ai' ? 'robot' : 'user'" size="medium" />
          </div>
          <div class="bubble" v-html="parseMarkdown(msg.content)"></div>
        </div>
      </div>

      <div class="input-wrapper">
        <div class="input-card">
          <textarea 
            v-model="userInput" 
            placeholder="发送消息或输入..."
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>
          <div class="input-footer">
            <div class="footer-spacer"></div>
            <button 
              class="send-circle-btn" 
              @click="sendMessage" 
              :disabled="!userInput.trim() || aiState !== 'idle'"
            >
              ↑
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ai-assistant {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f5ede0 0%, #e8dcc8 100%);
  display: flex;
  flex-direction: column;
  position: relative;
  /* 羊皮纸纹理 */
  background-image: 
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 20px,
      rgba(139, 115, 85, 0.03) 20px,
      rgba(139, 115, 85, 0.03) 21px
    );
}

.assistant-header {
  padding: 1.2rem;
  background: linear-gradient(180deg, #6b5b45 0%, #5d4e37 100%);
  border-bottom: 2px solid #3a2f24;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.4);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.header-left h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: #d4af37;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Georgia', 'Songti SC', serif;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
  letter-spacing: 0.05em;
}

.nav-btn {
  background: transparent;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #d4c5b0;
  padding: 0;
  display: flex;
  align-items: center;
  transition: color 0.2s;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.nav-btn:hover {
  color: #fffef9;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  font-weight: 500;
  color: #3a2f24;
  background: rgba(212, 175, 55, 0.3);
  padding: 0.3rem 0.6rem;
  border-radius: 12px;
  border: 1px solid rgba(139, 115, 85, 0.3);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
  transition: background 0.3s;
}

/* History View */
.history-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: rgba(255, 250, 240, 0.5);
}

.new-chat-btn {
  margin: 1rem 1.5rem;
  padding: 0.8rem;
  background: linear-gradient(135deg, #8b6f47 0%, #6b5b45 100%);
  color: #fffef9;
  text-align: center;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.4);
  border: 1px solid #5d4e37;
  font-family: 'Songti SC', 'STSong', serif;
  letter-spacing: 0.05em;
}

.new-chat-btn:hover {
  background: linear-gradient(135deg, #a08968 0%, #8b7355 100%);
  transform: translateY(-1px);
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.5);
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  list-style: none;
  padding: 0 1rem 1rem 1rem;
  margin: 0;
}

.conversation-list li {
  padding: 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #3a3025;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.2s;
  background: rgba(255, 250, 240, 0.3);
  border: 1px solid transparent;
}

.conversation-list li:hover {
  background: rgba(139, 111, 71, 0.2);
  border-color: #c4b59f;
}

.conversation-list li.active {
  background: rgba(139, 111, 71, 0.3);
  color: #5d4e37;
  font-weight: 600;
  border-color: #8b7355;
  box-shadow: inset 2px 0 4px rgba(0, 0, 0, 0.2);
}

.conv-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  padding-right: 0.5rem;
}

.conv-actions {
  display: flex;
  gap: 0.2rem;
  opacity: 0;
  transition: opacity 0.2s;
}

.conversation-list li:hover .conv-actions {
  opacity: 1;
}

.edit-conv, .delete-conv {
  background: none;
  border: none;
  color: #a08968;
  cursor: pointer;
  padding: 0.2rem;
  font-size: 1rem;
  line-height: 1;
}

.edit-conv:hover {
  color: #6b5b45;
}

.delete-conv:hover {
  color: #8b4513;
}

.edit-box {
  flex: 1;
  width: 100%;
}

.edit-box input {
  width: 100%;
  padding: 0.2rem 0.4rem;
  border: 2px solid #8b7355;
  border-radius: 3px;
  font-size: 0.9rem;
  outline: none;
  background: #fffef9;
  color: #3a3025;
  font-family: 'Songti SC', 'STSong', serif;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: rgba(255, 250, 240, 0.5);
  position: relative;
}

.chat-history {
  flex: 1;
  padding: 2rem 1.5rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 消息样式 */
.message {
  display: flex;
  gap: 0.8rem;
  align-items: flex-start;
  animation: messageSlide 0.3s ease-out;
}

@keyframes messageSlide {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  flex-direction: row-reverse;
}

.message .avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(139, 115, 85, 0.15);
  border: 2px solid #c4b59f;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  padding: 5px;
}

.message.ai .avatar {
  background: linear-gradient(135deg, #8b6f47 0%, #6b5b45 100%);
  border-color: #5d4e37;
}

.message .avatar :deep(.vintage-icon) {
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.2));
}

.message .bubble {
  max-width: 75%;
  padding: 0.9rem 1.1rem;
  border-radius: 12px;
  font-size: 0.95rem;
  line-height: 1.6;
  font-family: 'Songti SC', 'STSong', serif;
  word-wrap: break-word;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.message.ai .bubble {
  background: #fffef9;
  color: #2d2419;
  border: 1px solid #e8e4dc;
  border-left: 3px solid #8b7355;
}

.message.user .bubble {
  background: linear-gradient(135deg, #8b6f47 0%, #6b5b45 100%);
  color: #fffef9;
  border: 1px solid #5d4e37;
  text-align: right;
}

/* Input Area Redesign */
.input-wrapper {
  padding: 1.5rem 1.5rem 2rem 1.5rem;
  background: rgba(245, 237, 224, 0.8);
  border-top: 2px solid #d4c5b0;
}

.input-card {
  background: #fffef9;
  border: 2px solid #c4b59f;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 4px 12px rgba(101, 84, 60, 0.2);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.input-card:focus-within {
  box-shadow: 0 6px 20px rgba(101, 84, 60, 0.3);
  border-color: #8b7355;
}

.input-card textarea {
  border: none;
  background: transparent;
  padding: 0.5rem;
  font-size: 0.95rem;
  resize: none;
  min-height: 60px;
  max-height: 200px;
  outline: none;
  width: 100%;
  font-family: 'Songti SC', 'STSong', serif;
  color: #2d2419;
  line-height: 1.6;
}

.input-card textarea::placeholder {
  color: #a08968;
}

.input-footer {
  display: flex;
  justify-content: flex-end; /* Align send button to right */
  align-items: center;
  padding-top: 0.5rem;
}

.footer-spacer {
  flex: 1;
}

.send-circle-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e8e4dc;
  color: #a08968;
  border: 1px solid #c4b59f;
  cursor: not-allowed;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.send-circle-btn:not(:disabled) {
  background: linear-gradient(135deg, #8b6f47 0%, #6b5b45 100%);
  color: #fffef9;
  border-color: #5d4e37;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.send-circle-btn:not(:disabled):hover {
  background: linear-gradient(135deg, #a08968 0%, #8b7355 100%);
  transform: scale(1.08);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.4);
}

/* Code Block Styling */
:deep(.code-block-wrapper) {
  margin: 1rem 0;
  border-radius: 6px;
  overflow: hidden;
  background: #f5ede0;
  border: 2px solid #c4b59f;
  box-shadow: 0 2px 6px rgba(101, 84, 60, 0.2);
}

:deep(.code-header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background: rgba(139, 115, 85, 0.15);
  border-bottom: 2px solid #d4c5b0;
  font-size: 0.8rem;
  color: #6b5b45;
  font-weight: 600;
}

:deep(.code-lang) {
  font-weight: 700;
  text-transform: uppercase;
  color: #8b7355;
  letter-spacing: 0.05em;
}

:deep(.code-actions) {
  display: flex;
  gap: 0.5rem;
}

:deep(pre) {
  margin: 0;
  padding: 1rem;
  overflow-x: auto;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 0.9rem;
  line-height: 1.5;
  color: #3a3025;
  background: #fffef9;
}

:deep(.inline-code) {
  background: rgba(139, 115, 85, 0.15);
  padding: 0.2rem 0.4rem;
  border-radius: 3px;
  font-family: monospace;
  font-size: 0.9em;
  color: #8b4513;
  border: 1px solid #d4c5b0;
}

/* Scrollbar styling for webkit */
.chat-history::-webkit-scrollbar {
  width: 8px;
}
.chat-history::-webkit-scrollbar-track {
  background: rgba(212, 197, 176, 0.2);
}
.chat-history::-webkit-scrollbar-thumb {
  background-color: #c4b59f;
  border-radius: 4px;
  border: 2px solid rgba(245, 237, 224, 0.5);
}
.chat-history::-webkit-scrollbar-thumb:hover {
  background-color: #a08968;
}

.conversation-list::-webkit-scrollbar {
  width: 6px;
}
.conversation-list::-webkit-scrollbar-track {
  background: rgba(212, 197, 176, 0.2);
}
.conversation-list::-webkit-scrollbar-thumb {
  background-color: #c4b59f;
  border-radius: 3px;
}
.conversation-list::-webkit-scrollbar-thumb:hover {
  background-color: #a08968;
}
</style>
