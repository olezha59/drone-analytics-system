// ~/drone-analytics-system/frontend/src/services/api.ts
import axios from 'axios';
import type { RegionsGeoJSON, RegionStats } from '../types/region';

const API_BASE = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
});

export const geoApi = {
  getRegionsGeoJSON: async (): Promise<RegionsGeoJSON> => {
    // ИСПРАВЛЕНО: убрал лишний /api
    const response = await api.get<RegionsGeoJSON>('/geo/regions');
    return response.data;
  },
};

export const regionsApi = {
  getRegionStats: async (regionId: number): Promise<RegionStats> => {
    const response = await api.get<RegionStats>(`/regions/${regionId}/stats`);
    return response.data;
  },

  getAllRegionsStats: async (regionIds: number[]): Promise<Map<number, RegionStats>> => {
    const statsMap = new Map<number, RegionStats>();
    
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
          if (stats.totalFlights > 0) {
            console.log(`✓ Region ${regionId}: ${stats.totalFlights} flights`);
          }
        }
      });
      
      await new Promise(resolve => setTimeout(resolve, 300));
      console.log(`Progress: ${Math.min(i + batchSize, regionIds.length)}/${regionIds.length} regions`);
    }
    
    console.log(`✅ Loaded stats for ${loadedCount} regions total`);
    console.log(`📊 Regions with flight data: ${Array.from(statsMap.values()).filter(s => s.totalFlights > 0).length}`);
    
    return statsMap;
  },
};
