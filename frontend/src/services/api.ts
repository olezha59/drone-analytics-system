import axios from 'axios';
import type { IRegionsGeoJSON, IRegionStats } from './types/mapTypes';

// Используем относительный путь - Vite прокси будет перенаправлять на бэкенд
const API_BASE = '/api';

const api = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
});

export const geoApi = {
  getRegionsGeoJSON: async (): Promise<IRegionsGeoJSON> => {
    const response = await api.get<IRegionsGeoJSON>('/geo/regions');
    return response.data;
  },
};
export const analyticsApi = {
  getRussiaSummary: async (): Promise<{
    totalOperators: number;
    dataLastUpdated: string;
    totalRegions: number;
    totalFlights: number;
  }> => {
    const response = await api.get('/analytics/summary');
    return response.data;
  },
};

export const regionsApi = {
  getRegionStats: async (regionId: number): Promise<IRegionStats> => {
    const response = await api.get<IRegionStats>(`/regions/${regionId}/stats`);
    return response.data;
  },

  getAllRegionsStats: async (regionIds: number[]): Promise<Map<number, IRegionStats>> => {
    const statsMap = new Map<number, IRegionStats>();
    
    console.log(`Loading stats for all ${regionIds.length} regions...`);
    
    const batchSize = 5;
    let loadedCount = 0;
    
    for (let i = 0; i < regionIds.length; i += batchSize) {
      const batch = regionIds.slice(i, i + batchSize);
      const promises = batch.map(async (regionId) => {
        try {
          const stats = await regionsApi.getRegionStats(regionId);
          return { regionId, stats, success: true };
        } catch (error) {
          console.warn(`No stats for region ${regionId}`);
          return { regionId, stats: null, success: false };
        }
      });

      const results = await Promise.all(promises);
      
      results.forEach(({ regionId, stats, success }) => {
        if (success && stats) {
          statsMap.set(regionId, stats);
          loadedCount++;
        }
      });
      
      await new Promise(resolve => setTimeout(resolve, 300));
    }
    
    console.log(`✅ Loaded stats for ${loadedCount} regions`);
    
    return statsMap;
  },
};
