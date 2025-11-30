<script setup>
import { ref } from 'vue';
import api from '../services/api';

const messages = ref([
  { id: 1, type: 'ai', content: '你好！我是接入了 DeepSeek 的 AI 写作助手。可以在这里让我帮你生成大纲、续写剧情或设定角色。' }
]);
const userInput = ref('');
const isLoading = ref(false);

const quickActions = [
  { label: '🪄 剧情续写', prompt: '请根据当前情节续写一段...' },
  { label: '✨ 润色文字', prompt: '请润色这段文字，使其更优美...' },
  { label: '💡 生成灵感', prompt: '给我三个关于后续发展的反转灵感...' },
  { label: '👤 角色起名', prompt: '生成几个符合这种风格的角色名字...' }
];

async function sendMessage() {
  if (!userInput.value.trim() || isLoading.value) return;
  
  const prompt = userInput.value;
  
  // 添加用户消息
  messages.value.push({
    id: Date.now(),
    type: 'user',
    content: prompt
  });

  userInput.value = '';
  isLoading.value = true;
  scrollToBottom();

  try {
    // 创建一个新的空消息用于流式接收
    const aiMsgId = Date.now() + 1;
    messages.value.push({
      id: aiMsgId,
      type: 'ai',
      content: ''
    });
    
    // 查找刚才添加的消息引用
    const aiMsg = messages.value.find(m => m.id === aiMsgId);

    // 调用流式接口
    await api.streamChatWithAi(
      prompt,
      (chunk) => {
        // 收到数据片段，追加到消息内容
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
    isLoading.value = false;
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
      <h3>🤖 AI 创意助手</h3>
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
        <div v-if="isLoading" class="message ai">
          <div class="avatar">🤖</div>
          <div class="bubble loading">思考中...</div>
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
  width: 320px;
  background: #fff;
  border-left: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  height: 100%;
  flex-shrink: 0;
}

.assistant-header {
  padding: 1rem;
  border-bottom: 1px solid var(--color-border);
  background: #f8f9fa;
}

.assistant-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-history {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background: #fafafa;
}

.message {
  display: flex;
  gap: 0.5rem;
  max-width: 90%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  flex-shrink: 0;
}

.bubble {
  padding: 0.8rem;
  border-radius: 12px;
  font-size: 0.9rem;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.message.ai .bubble {
  background: #fff;
  border: 1px solid #eee;
  border-top-left-radius: 2px;
  color: #333;
}

.message.user .bubble {
  background: #1890ff;
  color: white;
  border-top-right-radius: 2px;
}

.loading {
  color: #999;
  font-style: italic;
}

.quick-actions {
  padding: 0.8rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  background: #fff;
  border-top: 1px solid #eee;
}

.action-tag {
  background: #f0f7ff;
  color: #1890ff;
  border: 1px solid #d6e4ff;
  border-radius: 12px;
  padding: 0.2rem 0.6rem;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.action-tag:hover {
  background: #e6f7ff;
  transform: translateY(-1px);
}

.input-area {
  padding: 1rem;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  gap: 0.5rem;
  align-items: flex-end;
}

textarea {
  flex: 1;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 0.6rem;
  font-size: 0.9rem;
  resize: none;
  height: 60px;
  outline: none;
  font-family: inherit;
}

textarea:focus {
  border-color: #1890ff;
}

.send-btn {
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  cursor: pointer;
  font-weight: 500;
  height: 40px;
  transition: background 0.2s;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.send-btn:hover:not(:disabled) {
  background: #096dd9;
}
</style>
