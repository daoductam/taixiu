import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth APIs
export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

// User APIs
export const userApi = {
  getMe: () => api.get('/users/me'),
  getBalance: () => api.get('/users/balance'),
  redeemCode: (code) => api.post('/users/redeem-code', { code }),
  getLeaderboard: () => api.get('/users/leaderboard'),
};

// Game APIs
export const gameApi = {
  getCurrent: () => api.get('/game/current'),
  getHistory: () => api.get('/game/history'),
  placeBet: (betType, amount) => api.post('/game/bet', { betType, amount }),
};

// Chat APIs
export const chatApi = {
  getMessages: () => api.get('/chat'),
  sendMessage: (content) => api.post('/chat', { content }),
};

// Admin APIs
export const adminApi = {
  getUsers: (page = 0, size = 20) => api.get(`/admin/users?page=${page}&size=${size}`),
  addBalance: (userId, amount) => api.post(`/admin/users/${userId}/balance`, { amount }),
  toggleUserStatus: (userId) => api.post(`/admin/users/${userId}/toggle-status`),
  getGiftCodes: (page = 0, size = 20) => api.get(`/admin/gift-codes?page=${page}&size=${size}`),
  createGiftCode: (amount, expiredAt) => api.post('/admin/gift-codes', { amount, expiredAt }),
  deleteGiftCode: (id) => api.delete(`/admin/gift-codes/${id}`),
};

export default api;
