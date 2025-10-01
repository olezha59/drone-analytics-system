// frontend/src/components/Map/RegionsSidebar.tsx
import React, { useState } from 'react';
import './RegionsSidebar.css';

interface RegionItem {
  id: number;
  name: string;
  totalFlights: number;
  normalizedValue: number;
  color: string;
}

interface RegionsSidebarProps {
  regions: RegionItem[];
  selectedRegionId: number | null;
  onRegionSelect: (regionId: number) => void;
  onRegionHover: (regionId: number) => void;
  onRegionLeave: () => void;
}

const RegionsSidebar: React.FC<RegionsSidebarProps> = ({
  regions,
  selectedRegionId,
  onRegionSelect,
  onRegionHover,
  onRegionLeave
}) => {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  // Сортируем регионы по количеству полетов (по убыванию)
  const sortedRegions = [...regions].sort((a, b) => b.totalFlights - a.totalFlights);

  // Фильтруем регионы по поисковому запросу
  const filteredRegions = sortedRegions.filter(region =>
    region.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (isCollapsed) {
    return (
      <div className="regions-sidebar collapsed">
        <button 
          className="toggle-btn"
          onClick={() => setIsCollapsed(false)}
          title="Развернуть список регионов"
        >
          📋
        </button>
      </div>
    );
  }

  return (
    <div className="regions-sidebar">
      {/* Заголовок с кнопкой свернуть */}
      <div className="sidebar-header">
        <h3>📍 Регионы РФ</h3>
        <button 
          className="toggle-btn"
          onClick={() => setIsCollapsed(true)}
          title="Свернуть список"
        >
          ✕
        </button>
      </div>

      {/* Поиск */}
      <div className="search-container">
        <input
          type="text"
          placeholder="Поиск региона..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="search-input"
        />
      </div>

      {/* Статистика по РФ */}
      <div className="russia-stats">
        <h4>📊 Статистика по РФ</h4>
        <div className="russia-stats-grid">
          <div className="russia-stat">
            <div className="russia-stat-value">{regions.length}</div>
            <div className="russia-stat-label">Регионов</div>
          </div>
          <div className="russia-stat">
            <div className="russia-stat-value">
              {regions.reduce((sum, region) => sum + region.totalFlights, 0).toLocaleString()}
            </div>
            <div className="russia-stat-label">Всего полетов</div>
          </div>
        </div>
      </div>

      {/* Список регионов */}
      <div className="regions-list">
        <div className="list-header">
          <span>#</span>
          <span>Регион</span>
          <span>Полетов</span>
        </div>
        <div className="regions-scrollable">
          {filteredRegions.map((region, index) => (
            <div
              key={region.id}
              className={`region-item ${
                selectedRegionId === region.id ? 'selected' : ''
              } ${region.totalFlights === 0 ? 'no-data' : ''}`}
              onClick={() => onRegionSelect(region.id)}
              onMouseEnter={() => onRegionHover(region.id)}
              onMouseLeave={onRegionLeave}
            >
              <div className="region-number">{index + 1}</div>
              <div className="region-info">
                <div 
                  className="region-color"
                  style={{ backgroundColor: region.color }}
                />
                <span className="region-name">{region.name}</span>
              </div>
              <div className="region-flights">
                {region.totalFlights > 0 ? region.totalFlights.toLocaleString() : '—'}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Подсказка */}
      <div className="sidebar-hint">
        🖱️ Наводите на регионы для подсветки на карте
      </div>
    </div>
  );
};

export default RegionsSidebar;
