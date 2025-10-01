// frontend/src/utils/dataProcessor.ts
import type { IRegionsGeoJSON, IRegionStats } from '../types/region';

// В utils/dataProcessor.ts - обновляем getColorByValue
export const getColorByValue = (value: number): string => {
  if (value === 0) return '#f0f0f0'; // Серый - нет данных
  
  // Градиент от желтого (0%) к красному (100%)
  if (value < 0.1) return '#ffffcc'; // Очень светло-желтый
  if (value < 0.2) return '#ffeda0'; // Светло-желтый
  if (value < 0.4) return '#fed976'; // Желтый
  if (value < 0.6) return '#feb24c'; // Оранжево-желтый
  if (value < 0.8) return '#fd8d3c'; // Оранжевый
  if (value < 0.9) return '#fc4e2a'; // Красно-оранжевый
  return '#bd0026'; // Темно-красный - максимальная активность
};
export const processRegionData = (
  geoData: IRegionsGeoJSON,
  statsMap: Map<number, IRegionStats>
) => {
  let minFlights = Infinity;
  let maxFlights = 0;
  let totalRussiaFlights = 0;
  let totalDuration = 0;
  let durationCount = 0;
  let regionsWithData = 0;

  // Первый проход - находим min/max и общую статистику
  geoData.features.forEach(feature => {
    const stats = statsMap.get(feature.id);
    const totalFlights = stats?.totalFlights || 0;

    if (totalFlights > 0) {
      regionsWithData++;
      minFlights = Math.min(minFlights, totalFlights);
      maxFlights = Math.max(maxFlights, totalFlights);
      totalRussiaFlights += totalFlights;

      if (stats?.averageFlightDuration) {
        totalDuration += stats.averageFlightDuration;
        durationCount++;
      }
    }
  });

  // Если все регионы без данных
  if (minFlights === Infinity) minFlights = 0;

  // Второй проход - добавляем свойства для отображения
  const processedGeoData: IRegionsGeoJSON = {
    ...geoData,
    features: geoData.features.map(feature => {
      const stats = statsMap.get(feature.id);
      const totalFlights = stats?.totalFlights || 0;
      
      // Нормализуем значение от 0 до 1
      const normalizedValue = maxFlights > 0 ? totalFlights / maxFlights : 0;

      return {
        ...feature,
        properties: {
          ...feature.properties,
          id: feature.id,
          totalFlights,
          uniqueOperators: stats?.uniqueOperators || 0,
          averageFlightDuration: stats?.averageFlightDuration || 0,
          normalizedValue,
          color: getColorByValue(normalizedValue),
          flightsByAircraftType: stats?.flightsByAircraftType || {},
          centerCodes: stats?.centerCodes || []
        }
      };
    })
  };

  const averageRussiaDuration = durationCount > 0 ? totalDuration / durationCount : 0;

  return {
    geoData: processedGeoData,
    minFlights,
    maxFlights,
    totalRegions: geoData.features.length,
    regionsWithData,
    totalRussiaFlights,
    averageRussiaDuration
  };
};

// Функция для корректировки координат Чукотки
export const adjustChukotkaCoordinates = (coordinates: number[][][][]): number[][][][] => {
  return coordinates.map(polygonGroup =>
    polygonGroup.map(polygon =>
      polygon.map(coord => {
        let [lng, lat] = coord;
        // Сдвигаем Чукотку западнее
        if (lng > 160) {
          lng = lng - 30; // Сдвиг на 30 градусов
        }
        return [lng, lat];
      })
    )
  );
};
