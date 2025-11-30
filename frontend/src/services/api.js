import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

export default {
  // Novels
  getNovels() {
    return apiClient.get('/novels');
  },
  getNovel(id) {
    return apiClient.get(`/novels/${id}`);
  },
  createNovel(novel) {
    return apiClient.post('/novels', novel);
  },
  updateNovel(id, novel) {
    return apiClient.put(`/novels/${id}`, novel);
  },
  deleteNovel(id) {
    return apiClient.delete(`/novels/${id}`);
  },

  // Chapters
  getChapters(novelId) {
    return apiClient.get(`/novels/${novelId}/chapters`);
  },
  getChapter(id) {
    return apiClient.get(`/chapters/${id}`);
  },
  createChapter(novelId, chapter) {
    return apiClient.post(`/novels/${novelId}/chapters`, chapter);
  },
  updateChapter(id, chapter) {
    return apiClient.put(`/chapters/${id}`, chapter);
  },
  deleteChapter(id) {
    return apiClient.delete(`/chapters/${id}`);
  },

  // AI
  chatWithAi(prompt) {
    return apiClient.post('/ai/chat', { prompt });
  },
  
  async streamChatWithAi(prompt, onMessage, onError) {
    try {
      const response = await fetch('/api/ai/chat/stream', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ prompt })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        
        const chunk = decoder.decode(value);
        const lines = chunk.split('\n');
        
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5);
            onMessage(data);
          }
        }
      }
    } catch (error) {
      if (onError) onError(error);
      else console.error('Stream error:', error);
    }
  }
};
