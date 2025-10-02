import apiClient from '../api/apiClient';
import type { IRegionsGeoJSON, IRegionStats } from '../types/mapTypes';

export const geoApi = {
  getRegionsGeoJSON: async (): Promise<IRegionsGeoJSON> => {
    console.log('🗺️ Fetching GeoJSON data via apiClient...');
    const response = await apiClient.get('/geo/regions');
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
    const response = await apiClient.get('/analytics/summary');
    return response.data;
  },
};

export const regionsApi = {
  getRegionStats: async (regionId: number): Promise<IRegionStats> => {
    console.log(`📊 Fetching stats for region ${regionId} via apiClient...`);
    const response = await apiClient.get(`/regions/${regionId}/stats`);
    return response.data;
  },

  getAllRegionsStats: async (regionIds: number[]): Promise<Map<number, IRegionStats>> => {
    const statsMap = new Map<number, IRegionStats>();
    
    console.log(`Loading stats for all ${regionIds.length} regions via apiClient...`);
    
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
