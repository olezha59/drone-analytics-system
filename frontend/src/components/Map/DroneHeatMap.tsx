// frontend/src/components/Map/DroneHeatMap.tsx
import React, { useState, useEffect, useCallback } from 'react';
import { MapContainer, TileLayer, GeoJSON, useMap } from 'react-leaflet';
import { geoApi, regionsApi } from '../../services/api';
import { processRegionData } from '../../utils/dataProcessor';
import type { IRegionsGeoJSON, IRegionStats } from '../../types/mapTypes';
import 'leaflet/dist/leaflet.css';
import './DroneHeatMap.css';
import RegionTooltip from './RegionTooltip';
import RegionDetails from '../Analytics/RegionDetails';
import RegionsSidebar from './RegionsSidebar';
import { useAuth } from '../../context/AuthContext';

// Импорт словаря названий регионов
import regionNames from '../../data/regionNames';

// Границы карты России для ограничения области просмотра
const RUSSIA_BOUNDS: [[number, number], [number, number]] = [
  [35.0, 19.0],
  [82.0, 190.0]
];

interface TooltipData {
  regionId: number;
  regionName: string;
  totalFlights: number;
  normalizedValue: number;
  mostPopularDrone: string;
  x: number;
  y: number;
}

const MapController: React.FC<{ 
  geoData: IRegionsGeoJSON;
  onRegionHover: (data: TooltipData | null) => void;
  onRegionClick: (regionId: number) => void;
}> = ({ geoData, onRegionHover, onRegionClick }) => {
  const map = useMap();
  
  useEffect(() => {
    if (geoData.features.length > 0) {
      const bounds = getGeoJSONBounds(geoData);
      if (bounds) {
        map.fitBounds(bounds, { padding: [20, 20] });
      }
      
      map.setMaxBounds(RUSSIA_BOUNDS);
    }
  }, [map, geoData]);

  return null;
};

const getGeoJSONBounds = (geoData: IRegionsGeoJSON): [number, number][] | null => {
  const coords: [number, number][] = [];
  
  geoData.features.forEach(feature => {
    if (feature.geometry.type === 'MultiPolygon') {
      feature.geometry.coordinates.forEach(polygonGroup => {
        polygonGroup.forEach(polygon => {
          polygon.forEach(coord => {
            let [lng, lat] = coord;
            if (lng > 160) {
              lng = lng - 30;
            }
            coords.push([lat, lng]);
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
  const [tooltipData, setTooltipData] = useState<TooltipData | null>(null);
  const [selectedRegionId, setSelectedRegionId] = useState<number | null>(null);
  const [regionStats, setRegionStats] = useState<IRegionStats | null>(null);
  const [selectedRegionName, setSelectedRegionName] = useState<string>('');
  const [hoveredRegionId, setHoveredRegionId] = useState<number | null>(null);
  const { user } = useAuth();

  // Загрузка данных
  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);
        setProgress(0);

        console.log('🔄 Загрузка GeoJSON...');
        console.log('👤 Current user:', user?.username);
        
        const geoData = await geoApi.getRegionsGeoJSON();
        setProgress(30);

        const regionIds = geoData.features.map(feature => feature.id);
        console.log(`📍 Найдено регионов: ${regionIds.length}`);
        
        setProgress(50);
        console.log('📊 Загрузка статистики регионов...');
        const statsMap = await regionsApi.getAllRegionsStats(regionIds);
        setProgress(80);
        
        console.log(`✅ Загружена статистика для ${statsMap.size} регионов`);

        const processed = processRegionData(geoData, statsMap);
        setProcessedData(processed);
        setProgress(100);

        if (processed.regionsWithData === 0) {
          setError('Нет данных о полетах для отображения');
        } else {
          console.log(`🎯 Регионов с данными: ${processed.regionsWithData}`);
        }

      } catch (err) {
        console.error('❌ Ошибка загрузки данных карты:', err);
        setError(`Ошибка загрузки: ${err instanceof Error ? err.message : 'Unknown error'}`);
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      loadData();
    }
  }, [user]);

  // Загрузка статистики региона при клике
  useEffect(() => {
    if (selectedRegionId && user) {
      const loadRegionStats = async () => {
        try {
          const stats = await regionsApi.getRegionStats(selectedRegionId);
          setRegionStats(stats);
        } catch (err) {
          console.error('❌ Ошибка загрузки статистики региона:', err);
        }
      };
      loadRegionStats();
    }
  }, [selectedRegionId, user]);

  // Обработчик наведения на регион
  const handleRegionHover = useCallback((feature: any, event: any) => {
    const props = feature.properties;
    const regionId = feature.id;
    
    const russianName = regionNames[regionId as keyof typeof regionNames] || `Регион ${regionId}`;
    
    const droneTypes = props.flightsByAircraftType || {};
    const mostPopularDrone = Object.keys(droneTypes).reduce((a, b) => 
      droneTypes[a] > droneTypes[b] ? a : b, Object.keys(droneTypes)[0] || 'N/A'
    );

    setTooltipData({
      regionId,
      regionName: russianName,
      totalFlights: props.totalFlights || 0,
      normalizedValue: props.normalizedValue || 0,
      mostPopularDrone,
      x: event.originalEvent.clientX,
      y: event.originalEvent.clientY
    });
  }, []);

  // Обработчик ухода мыши с региона
  const handleRegionMouseOut = useCallback(() => {
    setTooltipData(null);
  }, []);

  // Обработчик клика по региону
  const handleRegionClick = useCallback((feature: any) => {
    const regionId = feature.id;
    const regionName = regionNames[regionId as keyof typeof regionNames] || `Регион ${regionId}`;
    
    setSelectedRegionId(regionId);
    setSelectedRegionName(regionName);
    setTooltipData(null);
  }, []);

  // Обработчик наведения на регион в сайдбаре
  const handleSidebarRegionHover = useCallback((regionId: number) => {
    setHoveredRegionId(regionId);
  }, []);

  // Обработчик ухода мыши с региона в сайдбаре
  const handleSidebarRegionLeave = useCallback(() => {
    setHoveredRegionId(null);
  }, []);

  // Обработчик выбора региона из сайдбара
  const handleSidebarRegionSelect = useCallback((regionId: number) => {
    const regionName = regionNames[regionId as keyof typeof regionNames] || `Регион ${regionId}`;
    setSelectedRegionId(regionId);
    setSelectedRegionName(regionName);
  }, []);

  // Закрытие детальной статистики
  const handleCloseDetails = useCallback(() => {
    setSelectedRegionId(null);
    setRegionStats(null);
  }, []);

  // Стиль для регионов
  const regionStyle = (feature: any) => {
    const props = feature.properties;
    const regionId = feature.id;
    const isSelected = selectedRegionId === regionId;
    const isHovered = tooltipData?.regionId === regionId || hoveredRegionId === regionId;

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
        fillOpacity: isSelected ? 0.6 : isHovered ? 0.5 : 0.3,
        color: isSelected ? '#1890ff' : '#999',
        weight: isSelected ? 3 : isHovered ? 2 : 1,
        opacity: 1,
      };
    }

    return {
      fillColor: props.color,
      fillOpacity: isSelected ? 0.9 : isHovered ? 0.8 : 0.7,
      color: isSelected ? '#1890ff' : 'white',
      weight: isSelected ? 3 : isHovered ? 2 : 1,
      opacity: 1,
    };
  };

  // Обработчик для каждого региона
  const onEachFeature = (feature: any, layer: any) => {
    const props = feature.properties;
    const regionId = feature.id;

    layer.on('mouseover', (e: any) => {
      handleRegionHover(feature, e);
      layer.setStyle({
        weight: 2,
        color: '#fff',
        fillOpacity: 0.9,
      });
    });
    
    layer.on('mouseout', (e: any) => {
      handleRegionMouseOut();
      layer.setStyle(regionStyle(feature));
    });
    
    layer.on('click', (e: any) => {
      handleRegionClick(feature);
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
        center={[65, 90]}
        zoom={3}
        style={{ height: '100%', width: '100%' }}
        scrollWheelZoom={true}
        maxBounds={RUSSIA_BOUNDS}
        minZoom={3}
        maxZoom={8}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='BRZteam | Аналитика полетов БПЛА'
        />
        
        {processedData && (
          <>
            <MapController 
              geoData={processedData.geoData}
              onRegionHover={setTooltipData}
              onRegionClick={setSelectedRegionId}
            />
            <GeoJSON
              key={processedData.geoData.features.length}
              data={processedData.geoData}
              style={regionStyle}
              onEachFeature={onEachFeature}
            />
          </>
        )}
      </MapContainer>
      
      {/* Всплывающая подсказка при наведении */}
      {tooltipData && (
        <RegionTooltip 
          regionName={tooltipData.regionName}
          totalFlights={tooltipData.totalFlights}
          intensityPercentage={Math.round(tooltipData.normalizedValue * 100)}
          mostPopularDrone={tooltipData.mostPopularDrone}
          x={tooltipData.x}
          y={tooltipData.y}
        />
      )}
      
      {/* Детальная статистика региона (слева) */}
      {selectedRegionId && regionStats && (
        <div style={{
          position: 'absolute',
          top: '20px',
          left: '20px',
          maxHeight: '80vh',
          overflowY: 'auto',
          zIndex: 1000
        }}>
          <RegionDetails 
            regionStats={regionStats}
            regionName={selectedRegionName}
            onClose={handleCloseDetails}
          />
        </div>
      )}
      
      {/* Боковая панель с регионами (справа) */}
      {processedData && (
        <RegionsSidebar
          regions={processedData.geoData.features.map(feature => ({
            id: feature.id,
            name: regionNames[feature.id as keyof typeof regionNames] || `Регион ${feature.id}`,
            totalFlights: feature.properties.totalFlights || 0,
            normalizedValue: feature.properties.normalizedValue || 0,
            color: feature.properties.color || 'gray'
          }))}
          selectedRegionId={selectedRegionId}
          onRegionSelect={handleSidebarRegionSelect}
          onRegionHover={handleSidebarRegionHover}
          onRegionLeave={handleSidebarRegionLeave}
        />
      )}
    </div>
  );
};

export default DroneHeatMap;