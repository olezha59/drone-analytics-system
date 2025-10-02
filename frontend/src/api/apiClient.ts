import axios from 'axios';

// ВАЖНО: Используем относительный путь чтобы Vite проксировал запросы
const apiClient = axios.create({
  baseURL: '/api', // Относительный путь - Vite проксирует на localhost:8080
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor для автоматического добавления токена к запросам
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    console.log('🔐 API Request Interceptor - Token:', token ? 'present' : 'missing');
    console.log('🔐 Request URL:', config.url);
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('✅ Token added to headers');
    } else {
      console.log('❌ No token found in localStorage');
    }
    
    return config;
  },
  (error) => {
    console.error('❌ Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Interceptor для обработки ошибок
apiClient.interceptors.response.use(
  (response) => {
    console.log('✅ Response received:', response.status, response.config.url);
    return response;
  },
  (error) => {
    console.error('❌ Response error:', error.response?.status, error.config?.url);
    console.error('❌ Error details:', error.response?.data);
    
    if (error.response?.status === 401 || error.response?.status === 403) {
      console.log('🔐 Token invalid - logging out');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.reload();
    }
    return Promise.reject(error);
  }
);

export default apiClient;
