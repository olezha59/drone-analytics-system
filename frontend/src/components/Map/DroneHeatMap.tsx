// frontend/src/components/Map/DroneHeatMap.tsx
import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, GeoJSON, useMap } from 'react-leaflet';
import { geoApi, regionsApi } from '../../services/api';
import { processRegionData } from '../../utils/dataProcessor';
import type { IRegionsGeoJSON } from '../../types/region';
import 'leaflet/dist/leaflet.css';
import './DroneHeatMap.css';
import HeatMapLegend from './HeatMapLegend';

// Компонент для управления viewport карты
const MapController: React.FC<{ geoData: IRegionsGeoJSON }> = ({ geoData }) => {
  const map = useMap();
  
  useEffect(() => {
    if (geoData.features.length > 0) {
      // Вычисляем границы всех регионов
      const bounds = getGeoJSONBounds(geoData);
      if (bounds) {
        map.fitBounds(bounds, { padding: [20, 20] });
      }
    }
  }, [map, geoData]);
  
  return null;
};

// Функция для вычисления границ GeoJSON
const getGeoJSONBounds = (geoData: RegionsGeoJSON): [number, number][] | null => {
  const coords: [number, number][] = [];
  
  geoData.features.forEach(feature => {
    if (feature.geometry.type === 'MultiPolygon') {
      feature.geometry.coordinates.forEach(polygonGroup => {
        polygonGroup.forEach(polygon => {
          polygon.forEach(coord => {
            // GeoJSON координаты: [lng, lat], Leaflet: [lat, lng]
            coords.push([coord[1], coord[0]]);
          });
        });
      });
    }
  });
  
  if (coords.length === 0) return null;
  
  return coords;
};

const DroneHeatMap: React.FC = () => {
  const [processedData, setProcessedData] = useState<ReturnType<typeof processRegionData> | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [progress, setProgress] = useState<number>(0);

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);
        setProgress(0);

        // 1. Загружаем GeoJSON
        console.log('Loading GeoJSON...');
        const geoData = await geoApi.getRegionsGeoJSON();
        setProgress(30);

        // 2. Получаем список ID регионов
        const regionIds = geoData.features.map(feature => feature.id);
        console.log(`Found ${regionIds.length} regions:`, regionIds);
        
        // 3. Загружаем статистику для всех регионов
        setProgress(50);
        console.log('Loading region statistics...');
        const statsMap = await regionsApi.getAllRegionsStats(regionIds);
        setProgress(80);
        
        console.log(`Loaded stats for ${statsMap.size} regions`);

        // 4. Обрабатываем данные
        const processed = processRegionData(geoData, statsMap);
        setProcessedData(processed);
        setProgress(100);

        if (processed.regionsWithData === 0) {
  		setError('Нет данных о полетах для отображения');
	} else {
 		 console.log(`🎯 Found ${processed.regionsWithData} regions with flight data`);
  		console.log(`📈 Flight range: ${processed.minFlights} - ${processed.maxFlights}`);
	}

      } catch (err) {
        console.error('Error loading map data:', err);
        setError(`Ошибка загрузки: ${err instanceof Error ? err.message : 'Unknown error'}`);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  // Стиль для регионов
  const regionStyle = (feature: any) => {
    const props = feature.properties;
    
    const defaultStyle = {
      fillColor: 'gray',
      fillOpacity: 0.2,
      color: '#666',
      weight: 1,
      opacity: 0.5,
    };

    if (!props.totalFlights && props.totalFlights !== 0) {
      return defaultStyle;
    }

    if (props.totalFlights === 0) {
      return {
        fillColor: 'lightgray',
        fillOpacity: 0.3,
        color: '#999',
        weight: 1,
        opacity: 0.7,
      };
    }

    return {
      fillColor: props.color,
      fillOpacity: 0.7,
      color: 'white',
      weight: 1,
      opacity: 1,
    };
  };

  // Обработчик для каждого региона
  const onEachFeature = (feature: any, layer: any) => {
    const props = feature.properties;
    
    const popupContent = `
      <div style="padding: 8px; min-width: 250px;">
        <h3 style="margin: 0 0 8px 0; color: #333; border-bottom: 1px solid #eee; padding-bottom: 4px;">
          ${props.name || `Регион ${feature.id}`}
        </h3>
        <div style="font-size: 14px;">
          <p style="margin: 4px 0;">
            <strong>ID региона:</strong> ${feature.id}
          </p>
          <p style="margin: 4px 0;">
            <strong>Всего полетов:</strong> ${props.totalFlights || 0}
          </p>
          ${props.uniqueOperators ? `
            <p style="margin: 4px 0;">
              <strong>Уникальных операторов:</strong> ${props.uniqueOperators}
            </p>
          ` : ''}
          ${props.averageFlightDuration ? `
            <p style="margin: 4px 0;">
              <strong>Ср. длительность:</strong> ${props.averageFlightDuration.toFixed(1)} мин
            </p>
          ` : ''}
          ${props.normalizedValue !== undefined ? `
            <p style="margin: 4px 0;">
              <strong>Интенсивность:</strong> ${Math.round(props.normalizedValue * 100)}%
            </p>
          ` : ''}
        </div>
      </div>
    `;
    
    layer.bindPopup(popupContent);
    
    // Эффекты при наведении
    layer.on('mouseover', function (e: any) {
      layer.setStyle({
        weight: 3,
        color: '#fff',
        fillOpacity: 0.9,
      });
    });
    
    layer.on('mouseout', function (e: any) {
      layer.setStyle(regionStyle(feature));
    });
  };

  if (loading) {
    return (
      <div className="map-loading">
        <div className="loading-content">
          <h3>Загрузка тепловой карты полетов БПЛА</h3>
          <div className="progress-bar">
            <div 
              className="progress-fill" 
              style={{ width: `${progress}%` }}
            />
          </div>
          <p>{progress < 50 ? 'Загрузка геоданных...' : 'Загрузка статистики...'}</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="map-error">
        <div className="error-content">
          <h3>Ошибка загрузки карты</h3>
          <p>{error}</p>
          <p style={{ fontSize: '12px', marginTop: '10px' }}>
            Убедитесь, что бэкенд запущен на localhost:8080
          </p>
          <button 
            className="retry-button"
            onClick={() => window.location.reload()}
          >
            Обновить страницу
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="drone-heatmap-container">
      <MapContainer
        center={[55.7558, 37.6173]}
        zoom={4}
        style={{ height: '100%', width: '100%' }}
        scrollWheelZoom={true}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='BRZteam | Аналитика полетов БПЛА'        
        />
        
        {processedData && (
          <>
            <MapController geoData={processedData.geoData} />
            <GeoJSON
              key={processedData.geoData.features.length} // Принудительный ререндер при изменении данных
              data={processedData.geoData}
              style={regionStyle}
              onEachFeature={onEachFeature}
            />
          </>
        )}
      </MapContainer>
      
      {processedData && (
        <HeatMapLegend 
          minFlights={processedData.minFlights}
          maxFlights={processedData.maxFlights}
          totalRegions={processedData.totalRegions}
          regionsWithData={processedData.regionsWithData}
        />
      )}
    </div>
  );
};

export default DroneHeatMap;
