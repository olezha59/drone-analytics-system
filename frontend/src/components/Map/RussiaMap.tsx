import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, GeoJSON, useMap } from 'react-leaflet';
import { Card, Alert, List, Typography, Tag, Spin } from 'antd';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { Region, RegionStats } from '../../types/region';
import { analyticsAPI } from '../../services/api';

const { Title, Text } = Typography;

const CENTER_RUSSIA: [number, number] = [65, 90];
const ZOOM_LEVEL = 3;

// Цвета для тепловой карты на основе реальных данных
const getColorByFlights = (flights: number) => {
  if (flights === 0) return '#f0f0f0'; // нет полетов
  if (flights > 1000) return '#ff4d4f'; // высокая активность
  if (flights > 500) return '#faad14';  // средняя активность
  if (flights > 100) return '#52c41a';  // низкая активность
  return '#91d5ff';                     // минимальная активность
};

const RussiaMap: React.FC = () => {
  const [selectedRegionId, setSelectedRegionId] = useState<number | null>(null);
  const [regionStats, setRegionStats] = useState<RegionStats | null>(null);
  const [heatmapData, setHeatmapData] = useState<HeatmapData[]>([]); 
  const [loading, setLoading] = useState(true);
  const [geoJsonData, setGeoJsonData] = useState<any>(null);

  // Загрузка реальных данных из БД
  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        
        // Параллельная загрузка всех данных
        const [heatmapData, geoData] = await Promise.all([
  	  analyticsAPI.getHeatmapData(),
  	  analyticsAPI.getRegionGeoJSON()
   	]);
	setHeatmapData(heatmapData);
	setGeoJsonData(geoData);
        
      } catch (error) {
        console.error('Ошибка загрузки данных:', error);
        // Fallback на заглушки если бэкенд не готов
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  // Загрузка статистики региона при выборе
  useEffect(() => {
    if (selectedRegion) {
      setRegionStats(null);
      analyticsAPI.getRegionStats(selectedRegion.id)
        .then(setRegionStats)
        .catch(error => {
          console.error('Ошибка загрузки статистики региона:', error);
        });
    }
  }, [selectedRegion]);

  // Получение количества полетов для региона
  const getFlightCountForRegion = (regionId: number): number => {
   const regionData = heatmapData.find(item => item.regionId === regionId);
   return regionData ? regionData.flightCount : 0;
};
  // Стили для GeoJSON на основе реальных данных
  const geoJsonStyle = (feature: any) => {
    const regionId = feature.properties.id;
    const flightCount = getFlightCountForRegion(regionId);
    const isSelected = selectedRegion?.id === regionId;

    return {
      fillColor: getColorByFlights(flightCount),
      weight: isSelected ? 3 : 1,
      color: isSelected ? '#1890ff' : '#333',
      fillOpacity: isSelected ? 0.8 : 0.6,
    };
  };

  // Обработчик клика по региону на карте
  const onEachRegion = (feature: any, layer: any) => {
    const regionId = feature.id; 
    const regionName = feature.properties.name;
    const flightCount = getFlightCountForRegion(regionId);

    layer.bindTooltip(`
      <div style="font-weight: bold;">${regionName}</div>
      <div>Полётов: ${flightCount}</div>
      <div>Кликните для подробностей</div>
    `);

    layer.on('click', () => {
      const region = regions.find(r => r.id === regionId);
      if (region) {
        setSelectedRegion(region);
      }
    });
  };

  if (loading) {
    return (
      <Card title="🗺️ Тепловая карта активности БПЛА по регионам РФ" style={{ marginTop: '24px' }}>
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
          <div style={{ marginTop: '16px' }}>Загрузка данных из базы данных...</div>
        </div>
      </Card>
    );
  }

  return (
    <Card title="🗺️ Тепловая карта активности БПЛА (данные из flight_records)" style={{ marginTop: '24px' }}>
      <div style={{ display: 'flex', gap: '20px', minHeight: '600px' }}>
        {/* Карта с реальными данными */}
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
            
            {/* Реальная тепловая карта из GeoJSON */}
            {geoJsonData && (
              <GeoJSON
                data={geoJsonData}
                style={geoJsonStyle}
                onEachFeature={onEachRegion}
              />
            )}
          </MapContainer>

          {/* Список регионов с реальными данными */}
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
            <Text strong>Регионы РФ (данные из БД):</Text>
            <List
              size="small"
              dataSource={regions}
              renderItem={(region) => {
                const flightCount = getFlightCountForRegion(region.id);
                return (
                  <List.Item 
                    style={{ 
                      cursor: 'pointer',
                      background: selectedRegion?.id === region.id ? '#e6f7ff' : 'transparent',
                      padding: '8px'
                    }}
                    onClick={() => setSelectedRegion(region)}
                  >
                    <List.Item.Meta
                      title={region.name}
                      description={
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <div 
                            style={{ 
                              width: '12px', 
                              height: '12px', 
                              background: getColorByFlights(flightCount),
                              borderRadius: '2px'
                            }} 
                          />
                          <span>{flightCount} полётов</span>
                        </div>
                      }
                    />
                  </List.Item>
                );
              }}
            />
          </div>
        </div>

        {/* Статистика региона */}
        <div style={{ flex: 2, minWidth: '300px' }}>
          {selectedRegion && regionStats ? (
            <Card 
              title={`📊 ${selectedRegion.name}`}
              extra={
                <Tag color={getColorByFlights(regionStats.flightCount)}>
                  {regionStats.flightCount > 1000 ? 'Высокая' : 
                   regionStats.flightCount > 500 ? 'Средняя' : 
                   regionStats.flightCount > 100 ? 'Низкая' : 'Минимальная'} активность
                </Tag>
              }
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
                <List.Item>
                  <List.Item.Meta 
                    title="Дней без полётов" 
                    description={regionStats.zeroDays} 
                  />
                </List.Item>
              </List>

              <div style={{ marginTop: '16px' }}>
                <Text strong>Суточная активность:</Text>
                <div style={{ marginTop: '8px' }}>
                  <div>Утро (6-12): {regionStats.dailyActivity.morning}%</div>
                  <div>День (12-18): {regionStats.dailyActivity.day}%</div>
                  <div>Вечер (18-24): {regionStats.dailyActivity.evening}%</div>
                  <div>Ночь (0-6): {regionStats.dailyActivity.night}%</div>
                </div>
              </div>
            </Card>
          ) : (
            <Card title="ℹ️ Информация о регионе">
              <div style={{ textAlign: 'center', padding: '20px' }}>
                <Text type="secondary">Выберите регион на карте для просмотра статистики</Text>
                <div style={{ marginTop: '16px' }}>
                  <Alert 
                    message="Данные загружаются из базы данных flight_records" 
                    type="info" 
                  />
                </div>
              </div>
            </Card>
          )}
        </div>
      </div>

      {/* Легенда */}
      <div style={{ marginTop: '16px', padding: '16px', background: '#f5f5f5', borderRadius: '6px' }}>
        <Text strong>Легенда тепловой карты (данные из flight_records):</Text>
        <div style={{ display: 'flex', gap: '15px', alignItems: 'center', marginTop: '8px', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <div style={{ width: '20px', height: '20px', background: '#ff4d4f', borderRadius: '2px' }}></div>
            <span>Высокая (1000+ полётов)</span>
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
            <div style={{ width: '20px', height: '20px', background: '#91d5ff', borderRadius: '2px' }}></div>
            <span>Минимальная (1-100)</span>
          </div>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <div style={{ width: '20px', height: '20px', background: '#f0f0f0', borderRadius: '2px' }}></div>
            <span>Нет полётов</span>
          </div>
        </div>
      </div>
    </Card>
  );
};

export default RussiaMap;
