// frontend/src/components/Map/RussiaMapBasic.tsx
import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Исправление иконки маркера (обязательно для React + Leaflet)
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

const RussiaMapBasic: React.FC = () => {
  const [geoJson, setGeoJson] = useState<any>(null);

  useEffect(() => {
    const loadRegions = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/geo/regions');
        const data = await response.json();
        setGeoJson(data);
      } catch (error) {
        console.error('Ошибка загрузки регионов:', error);
      }
    };

    loadRegions();
  }, []);

  const style = () => ({
    fillColor: '#e0f7fa',
    weight: 1,
    color: '#006064',
    fillOpacity: 0.6,
  });

  return (
    <MapContainer
      center={[65, 90]}
      zoom={3}
      style={{ height: '600px', width: '100%', border: '1px solid #ccc' }}
    >
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; OpenStreetMap contributors'
      />
      {geoJson && <GeoJSON data={geoJson} style={style} />}
    </MapContainer>
  );
};

export default RussiaMapBasic;
