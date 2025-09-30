import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, GeoJSON, useMapEvents } from 'react-leaflet';
import { Card, Alert, List, Typography, Spin } from 'antd';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { Region, RegionStats } from '../../types/region';
import { analyticsAPI } from '../../services/api';

const { Title, Text } = Typography;

// Центр России
const CENTER_RUSSIA: [number, number] = [65, 90];
const ZOOM_LEVEL = 3;

// Стили для GeoJSON регионов
const getRegionStyle = (flights: number, isSelected: boolean = false) => {
  const baseColor = getColorByFlights(flights);
  
  return {
    fillColor: baseColor,
    weight: isSelected ? 3 : 1,
    color: isSelected ? '#1890ff' : '#333',
    fillOpacity: isSelected ? 0.8 : 0.6,
  };
};

// Цвета для тепловой карты
const getColorByFlights = (flights: number) => {
  if (flights > 1000) return '#ff4d4f';
  if (flights > 500) return '#faad14';
  if (flights > 100) return '#52c41a';
  return '#d9d9d9';
};

// Компонент для обработки кликов по карте
const MapClickHandler: React.FC<{ onRegionClick: (region: Region) => void }> = ({ onRegionClick }) => {
  useMapEvents({
    click: (e) => {
      // В реальном приложении здесь будет логика определения региона по координатам
      console.log('Координаты клика:', e.latlng);
    },
  });
  return null;
};

const RussiaMap: React.FC = () => {
  const [selectedRegion, setSelectedRegion] = useState<Region | null>(null);
  const [regionStats, setRegionStats] = useState<RegionStats | null>(null);
  const [regions, setRegions] = useState<Region[]>([]);
  const [loading, setLoading] = useState(true);

  // Загрузка данных регионов
  useEffect(() => {
    const loadRegions = async () => {
      try {
        const regionsData = await analyticsAPI.getRegions();
        setRegions(regionsData);
      } catch (error) {
        console.error('Ошибка загрузки регионов:', error);
      } finally {
        setLoading(false);
      }
    };
    
    loadRegions();
  }, []);

  // Загрузка статистики при выборе региона
  useEffect(() => {
    if (selectedRegion) {
      setRegionStats(null);
      analyticsAPI.getRegionStats(selectedRegion.id).then(setRegionStats);
    }
  }, [selectedRegion]);

  // Обработчик клика по региону
  const handleRegionClick = (region: Region) => {
    setSelectedRegion(region);
  };

  // Заглушка GeoJSON данных (в реальном приложении будет загрузка с сервера)
  const russiaGeoJSON = {
    type: "FeatureCollection",
    features: regions.map(region => ({
      type: "Feature",
      properties: {
        id: region.id,
        name: region.name,
        flights: Math.floor(Math.random() * 1500), // Заглушка
      },
      geometry: {
        type: "Polygon",
        coordinates: [[]] // Упрощенная геометрия
      }
    }))
  };

  if (loading) {
    return (
      <Card title="🗺️ Интерактивная карта России" style={{ marginTop: '24px' }}>
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
          <div style={{ marginTop: '16px' }}>Загрузка карты регионов...</div>
        </div>
      </Card>
    );
  }

  return (
    <Card title="🗺️ Тепловая карта активности БПЛА по регионам РФ" style={{ marginTop: '24px' }}>
      <div style={{ display: 'flex', gap: '20px', minHeight: '600px' }}>
        {/* Левая часть - карта */}
        <div style={{ flex: 3, position: 'relative', borderRadius: '8px', overflow: 'hidden' }}>
          <MapContainer
            center={CENTER_RUSSIA}
            zoom={ZOOM_LEVEL}
            style={{ height: '100%', width: '100%' }}
            scrollWheelZoom={true}
          >
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; OpenStreetMap contributors'
            />
            
            {/* Временная заглушка вместо реального GeoJSON */}
            <MapClickHandler onRegionClick={handleRegionClick} />
          </MapContainer>

          {/* Временный список регионов поверх карты */}
          <div style={{ 
            position: 'absolute', 
            top: '10px', 
            left: '10px', 
            background: 'rgba(255,255,255,0.95)',
            padding: '15px',
            borderRadius: '8px',
            maxWidth: '300px',
            maxHeight: '400px',
            overflowY: 'auto'
          }}>
            <Text strong>Выберите регион:</Text>
            <List
              size="small"
              dataSource={regions}
              renderItem={(region) => (
                <List.Item 
                  style={{ 
                    cursor: 'pointer',
                    background: selectedRegion?.id === region.id ? '#e6f7ff' : 'transparent',
                    padding: '8px'
                  }}
                  onClick={() => handleRegionClick(region)}
                >
                  <List.Item.Meta
                    title={region.name}
                    description={
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <div 
                          style={{ 
                            width: '12px', 
                            height: '12px', 
                            background: getColorByFlights(Math.random() * 1500),
                            borderRadius: '2px'
                          }} 
                        />
                        <span>{Math.floor(Math.random() * 1500)} полётов</span>
                      </div>
                    }
                  />
                </List.Item>
              )}
            />
          </div>
        </div>

        {/* Правая часть - информация о регионе */}
        <div style={{ flex: 2, minWidth: '300px' }}>
          {selectedRegion && regionStats ? (
            <Card 
              title={`📊 ${selectedRegion.name}`}
              extra={<Tag color={getColorByFlights(regionStats.flightCount)}>
                {regionStats.flightCount > 1000 ? 'Высокая' : regionStats.flightCount > 500 ? 'Средняя' : 'Низкая'} активность
              </Tag>}
            >
              <List size="small">
                <List.Item>
                  <List.Item.Meta 
                    title="Количество полётов" 
                    description={<Text strong>{regionStats.flightCount.toLocaleString()}</Text>} 
                  />
                </List.Item>
                <List.Item>
                  <List.Item.Meta 
                    title="Уникальных операторов" 
                    description={regionStats.operatorCount} 
                  />
                </List.Item>
                <List.Item>
                  <List.Item.Meta 
                    title="Средняя длительность" 
                    description={`${regionStats.avgDuration} мин`} 
                  />
                </List.Item>
                <List.Item>
                  <List.Item.Meta 
                    title="Плотность полётов" 
                    description={`${regionStats.flightDensity} на 1000 км²`} 
                  />
                </List.Item>
                <List.Item>
                  <List.Item.Meta 
                    title="Пиковая нагрузка" 
                    description={`${regionStats.peakLoad} полётов/час`} 
                  />
                </List.Item>
              </List>

              {/* Суточная активность */}
              <div style={{ marginTop: '16px' }}>
                <Text strong>Суточная активность:</Text>
                <div style={{ marginTop: '8px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>Утро: {regionStats.dailyActivity.morning}%</span>
                    <span>День: {regionStats.dailyActivity.day}%</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '4px' }}>
                    <span>Вечер: {regionStats.dailyActivity.evening}%</span>
                    <span>Ночь: {regionStats.dailyActivity.night}%</span>
                  </div>
                </div>
              </div>
            </Card>
          ) : (
            <Card title="ℹ️ Информация о регионе">
              <div style={{ textAlign: 'center', padding: '20px' }}>
                <Text type="secondary">Выберите регион на карте для просмотра подробной статистики</Text>
                <div style={{ marginTop: '16px' }}>
                  <Alert 
                    message="Реальная тепловая карта будет отображаться после подключения GeoJSON данных регионов РФ" 
                    type="info" 
                  />
                </div>
              </div>
            </Card>
          )}
        </div>
      </div>

      {/* Легенда тепловой карты */}
      <div style={{ marginTop: '16px', padding: '16px', background: '#f5f5f5', borderRadius: '6px' }}>
        <Text strong>Легенда тепловой карты (количество полётов):</Text>
        <div style={{ display: 'flex', gap: '20px', alignItems: 'center', marginTop: '8px', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <div style={{ width: '20px', height: '20px', background: '#ff4d4f', borderRadius: '2px' }}></div>
            <span>Высокая (1000+)</span>
          </div>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <div style={{ width: '20px', height: '20px', background: '#faad14', borderRadius: '2px' }}></div>
            <span>Средняя (500-1000)</span>
          </div>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <div style={{ width: '20px', height: '20px', background: '#52c41a', borderRadius: '2px' }}></div>
            <span>Низкая (100-500)</span>
          </div>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <div style={{ width: '20px', height: '20px', background: '#d9d9d9', borderRadius: '2px' }}></div>
            <span>Минимальная (0-100)</span>
          </div>
        </div>
      </div>
    </Card>
  );
};

export default RussiaMap;
