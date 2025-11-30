<script setup>
import { ref, computed } from 'vue';
import api from '../services/api';

const messages = ref([
  { id: 1, type: 'ai', content: '你好！我是接入了 DeepSeek 的 AI 写作助手。可以在这里让我帮你生成大纲、续写剧情或设定角色。' }
]);
const userInput = ref('');
const aiState = ref('idle'); // idle, thinking, streaming

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
      prompt,
      (chunk) => {
        // 一旦收到第一个字符，状态切换为 streaming
        if (aiState.value === 'thinking') aiState.value = 'streaming';
        aiMsg.content += chunk;
        scrollToBottom();
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
        <h3>🤖 AI 创意助手</h3>
      </div>
      <div class="header-right">
        <span class="status-dot" :style="{ background: statusColor }"></span>
        <span class="status-text">{{ statusText }}</span>
      </div>
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
          <div class="bubble">{{ msg.content }}</div>
        </div>
      </div>

      <div class="quick-actions">
        <button 
          v-for="(action, index) in quickActions" 
          :key="index"
          class="action-tag"
          @click="useQuickAction(action.prompt)"
        >
          {{ action.label }}
        </button>
      </div>

      <div class="input-area">
        <textarea 
          v-model="userInput" 
          placeholder="输入指令，例如：帮我描写一个赛博朋克风格的街道..."
          @keydown.enter.exact.prevent="sendMessage"
        ></textarea>
        <button class="send-btn" @click="sendMessage" :disabled="!userInput.trim() || isLoading">
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ai-assistant {
  width: 340px; /* Slightly wider */
  background: #fcfcfc;
  border-left: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  height: 100%;
  flex-shrink: 0;
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

.header-left h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 0.5rem;
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

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f9fafb;
}

.chat-history {
  flex: 1;
  padding: 1.5rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.message {
  display: flex;
  gap: 0.8rem;
  max-width: 95%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.message.ai .avatar {
  background: #ffffff;
  border: 1px solid #e5e7eb;
}

.message.user .avatar {
  background: #dbeafe; /* Light blue */
  color: #2563eb;
}

.bubble {
  padding: 1rem 1.2rem;
  border-radius: 16px;
  font-size: 0.95rem;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
  box-shadow: 0 2px 5px rgba(0,0,0,0.03);
  position: relative;
}

.message.ai .bubble {
  background: #ffffff;
  color: #374151;
  border: 1px solid #f3f4f6;
  border-top-left-radius: 4px;
}

.message.user .bubble {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  border-top-right-radius: 4px;
  box-shadow: 0 4px 6px rgba(37, 99, 235, 0.2);
}

.quick-actions {
  padding: 1rem 1.2rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  background: #ffffff;
  border-top: 1px solid #f3f4f6;
}

.action-tag {
  background: #eff6ff;
  color: #2563eb;
  border: 1px solid transparent;
  border-radius: 20px;
  padding: 0.4rem 0.8rem;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-tag:hover {
  background: #dbeafe;
  border-color: #bfdbfe;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.1);
}

.input-area {
  padding: 1.2rem;
  background: #ffffff;
  border-top: 1px solid #f3f4f6;
  display: flex;
  gap: 0.8rem;
  align-items: flex-end;
  box-shadow: 0 -4px 12px rgba(0,0,0,0.02);
}

textarea {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 0.8rem;
  font-size: 0.95rem;
  resize: none;
  height: 50px;
  min-height: 50px;
  outline: none;
  font-family: inherit;
  background: #f9fafb;
  transition: all 0.2s;
  line-height: 1.4;
}

textarea:focus {
  background: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  height: 70px; /* Expand on focus */
}

.send-btn {
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 12px;
  padding: 0 1.2rem;
  cursor: pointer;
  font-weight: 600;
  height: 50px; /* Match textarea min-height */
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
  font-size: 0.9rem;
}

.send-btn:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
  box-shadow: none;
}

.send-btn:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(37, 99, 235, 0.3);
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
