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
  }
};
