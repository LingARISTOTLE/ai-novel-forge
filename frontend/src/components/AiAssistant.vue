<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import api from '../services/api';

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
const currentConversationId = ref(null);
const showHistory = ref(false);
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

async function switchConversation(id) {
  try {
    currentConversationId.value = id;
    const response = await api.getConversationMessages(id);
    messages.value = response.data.map(msg => ({
      id: msg.id,
      type: msg.role === 'assistant' ? 'ai' : 'user',
      content: msg.content
    }));
    showHistory.value = false;
    scrollToBottom();
  } catch (e) {
    console.error('Failed to load messages', e);
  }
}

function startNewConversation() {
  currentConversationId.value = null;
  messages.value = [{ id: Date.now(), type: 'ai', content: '你好！我是接入了 DeepSeek 的 AI 写作助手。可以在这里让我帮你生成大纲、续写剧情或设定角色。' }];
  showHistory.value = false;
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
  { label: '🪄 剧情续写', prompt: '请根据当前情节续写一段...' },
  { label: '✨ 润色文字', prompt: '请润色这段文字，使其更优美...' },
  { label: '💡 生成灵感', prompt: '给我三个关于后续发展的反转灵感...' },
  { label: '👤 角色起名', prompt: '生成几个符合这种风格的角色名字...' }
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
        <button class="history-btn" @click="showHistory = !showHistory" title="历史记录">
          ☰
        </button>
        <h3>AI 创意助手</h3>
      </div>
      <div class="header-right">
        <span class="status-dot" :style="{ background: statusColor }"></span>
        <span class="status-text">{{ statusText }}</span>
      </div>
    </div>
    
    <!-- History Drawer -->
    <div class="history-drawer" :class="{ open: showHistory }">
      <div class="drawer-header">
        <h4>历史对话</h4>
        <button class="close-btn" @click="showHistory = false">×</button>
      </div>
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

    <div class="chat-container">
      <div class="chat-history">
        <div 
          v-for="msg in messages" 
          :key="msg.id" 
          class="message"
          :class="msg.type"
        >
          <div class="avatar">{{ msg.type === 'ai' ? '🤖' : '👤' }}</div>
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
  width: 340px;
  background: #fcfcfc;
  border-left: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  height: 100%;
  flex-shrink: 0;
  position: relative;
}

.assistant-header {
  padding: 1.2rem;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
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
  color: #1f2937;
}

.history-btn {
  background: transparent;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  display: flex;
  align-items: center;
}

.history-btn:hover {
  color: #111827;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  font-weight: 500;
  color: #6b7280;
  background: #f3f4f6;
  padding: 0.3rem 0.6rem;
  border-radius: 20px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
  transition: background 0.3s;
}

/* History Drawer */
.history-drawer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #ffffff;
  z-index: 20;
  transform: translateX(-100%);
  transition: transform 0.3s ease;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
}

.history-drawer.open {
  transform: translateX(0);
}

.drawer-header {
  padding: 1.2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.drawer-header h4 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
}

.new-chat-btn {
  margin: 1rem;
  padding: 0.8rem;
  background: #2563eb;
  color: white;
  text-align: center;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
}

.new-chat-btn:hover {
  background: #1d4ed8;
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
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #374151;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.2s;
}

.conversation-list li:hover {
  background: #f3f4f6;
}

.conversation-list li.active {
  background: #eff6ff;
  color: #2563eb;
  font-weight: 500;
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
  color: #9ca3af;
  cursor: pointer;
  padding: 0.2rem;
  font-size: 1rem;
  line-height: 1;
}

.edit-conv:hover {
  color: #2563eb;
}

.delete-conv:hover {
  color: #ef4444;
}

.edit-box {
  flex: 1;
  width: 100%;
}

.edit-box input {
  width: 100%;
  padding: 0.2rem 0.4rem;
  border: 1px solid #2563eb;
  border-radius: 4px;
  font-size: 0.9rem;
  outline: none;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #ffffff; /* White background like screenshot */
  position: relative;
}

.chat-history {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* Input Area Redesign */
.input-wrapper {
  padding: 1.5rem 2rem 2rem 2rem;
  background: #ffffff;
}

.input-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 24px; /* Large rounded corners */
  padding: 1rem;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05); /* Soft shadow */
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.input-card:focus-within {
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
  border-color: #d1d5db;
}

.input-card textarea {
  border: none;
  background: transparent;
  padding: 0.5rem;
  font-size: 1rem;
  resize: none;
  min-height: 60px;
  max-height: 200px;
  outline: none;
  width: 100%;
  font-family: inherit;
  color: #374151;
}

.input-card textarea::placeholder {
  color: #9ca3af;
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
  background: #e5e7eb;
  color: #9ca3af;
  border: none;
  cursor: not-allowed;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.send-circle-btn:not(:disabled) {
  background: #000000;
  color: #ffffff;
  cursor: pointer;
}

.send-circle-btn:not(:disabled):hover {
  background: #333333;
  transform: scale(1.05);
}

/* Code Block Styling */
:deep(.code-block-wrapper) {
  margin: 1rem 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
}

:deep(.code-header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background: #f1f3f5;
  border-bottom: 1px solid #e1e4e8;
  font-size: 0.8rem;
  color: #666;
}

:deep(.code-lang) {
  font-weight: 600;
  text-transform: uppercase;
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
  color: #24292e;
}

:deep(.inline-code) {
  background: #f3f4f4;
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.9em;
  color: #e83e8c;
}

/* Scrollbar styling for webkit */
.chat-history::-webkit-scrollbar {
  width: 6px;
}
.chat-history::-webkit-scrollbar-track {
  background: transparent;
}
.chat-history::-webkit-scrollbar-thumb {
  background-color: #e5e7eb;
  border-radius: 3px;
}
.chat-history::-webkit-scrollbar-thumb:hover {
  background-color: #d1d5db;
}
</style>
