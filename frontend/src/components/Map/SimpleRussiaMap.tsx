// frontend/src/components/Map/SimpleRussiaMap.tsx
import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import SimpleRussiaMap from '../components/Map/SimpleRussiaMap';
// Исправление иконки маркера (Leaflet bug в React)
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

const SimpleRussiaMap: React.FC = () => {
  const [geoJsonData, setGeoJsonData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadGeoJson = async () => {
      try {
        // Загружаем GeoJSON с бэкенда
        const response = await fetch('http://localhost:8080/api/geo/regions');
        const data = await response.json();
        setGeoJsonData(data);
      } catch (error) {
        console.error('Ошибка загрузки GeoJSON:', error);
      } finally {
        setLoading(false);
      }
    };

    loadGeoJson();
  }, []);

  // Простой стиль — все регионы серые
  const geoJsonStyle = () => ({
    fillColor: '#d3d3d3',
    weight: 1,
    color: '#666',
    fillOpacity: 0.7,
  });

  if (loading) {
    return <div>Загрузка карты...</div>;
  }

  if (!geoJsonData) {
    return <div>Не удалось загрузить данные карты</div>;
  }

  return (
    <MapContainer
      center={[65, 90]}     // Центр РФ
      zoom={3}
      style={{ height: '600px', width: '100%', border: '1px solid #ccc' }}
      scrollWheelZoom={true}
    >
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      />
      <GeoJSON
        data={geoJsonData}
        style={geoJsonStyle}
      />
    </MapContainer>
  );
};

export default SimpleRussiaMap;
