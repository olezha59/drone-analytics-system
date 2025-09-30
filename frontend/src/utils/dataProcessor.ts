// ~/drone-analytics-system/frontend/src/utils/dataProcessor.ts
import type { IRegionsGeoJSON, IRegionStats, IRegionFeature } from '../types/region';

export interface ProcessedRegionData {
  geoData: IRegionsGeoJSON;
  maxFlights: number;
  minFlights: number;
  totalRegions: number;
  regionsWithData: number;
}

export const processRegionData = (
  geoData: IRegionsGeoJSON,
  statsMap: Map<number, IRegionStats>
): ProcessedRegionData => {
  
  const flightCounts: number[] = [];
  
  const enrichedFeatures: IRegionFeature[] = geoData.features.map(feature => {
    const regionStats = statsMap.get(feature.id);
    const totalFlights = regionStats?.totalFlights || 0;
    
    if (regionStats && totalFlights > 0) {
      flightCounts.push(totalFlights);
    }

    return {
      ...feature,
      properties: {
        ...feature.properties,
        totalFlights,
        uniqueOperators: regionStats?.uniqueOperators,
        averageFlightDuration: regionStats?.averageFlightDuration,
        normalizedValue: 0,
        color: 'gray',
      },
    };
  });

  const maxFlights = flightCounts.length > 0 ? Math.max(...flightCounts) : 0;
  const minFlights = flightCounts.length > 0 ? Math.min(...flightCounts) : 0;

  const finalFeatures: IRegionFeature[] = enrichedFeatures.map(feature => {
    const totalFlights = feature.properties.totalFlights || 0;
    const normalizedValue = maxFlights > 0 ? totalFlights / maxFlights : 0;
    
    return {
      ...feature,
      properties: {
        ...feature.properties,
        normalizedValue,
        color: getColorByValue(normalizedValue),
      },
    };
  });

  return {
    geoData: {
      ...geoData,
      features: finalFeatures,
    },
    maxFlights,
    minFlights,
    totalRegions: geoData.features.length,
    regionsWithData: flightCounts.length,
  };
};

export const getColorByValue = (value: number): string => {
  const hue = 60 - (value * 60);
  return `hsl(${hue}, 100%, 50%)`;
};
