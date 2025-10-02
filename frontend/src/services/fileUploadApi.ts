import apiClient from '../api/apiClient';

export const fileUploadApi = {
  uploadExcelFile: async (file: File): Promise<any> => {
    console.log('📤 Uploading file:', file.name, file.size, file.type);
    
    const formData = new FormData();
    formData.append('file', file);
    
    // ВАЖНО: Для FormData НЕ указываем Content-Type - браузер сам установит
    const response = await apiClient.post('/admin/upload-excel', formData, {
      headers: {
        'Content-Type': undefined, // Позволяет браузеру установить правильный Content-Type с boundary
      },
      timeout: 300000, // 5 минут таймаут для больших файлов
    });
    
    return response.data;
  },

  // Проверка доступности Python
  checkPythonHealth: async (): Promise<string> => {
    const response = await apiClient.get('/admin/health/python');
    return response.data;
  },

  // Проверка наличия скрипта
  checkScriptHealth: async (): Promise<string> => {
    const response = await apiClient.get('/admin/health/script');
    return response.data;
  },
};
