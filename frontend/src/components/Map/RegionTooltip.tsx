// frontend/src/components/Map/RegionTooltip.tsx
import React from 'react';
import './RegionTooltip.css';

interface RegionTooltipProps {
  regionName: string;
  totalFlights: number;
  intensityPercentage: number;
  mostPopularDrone: string;
  x: number;
  y: number;
}

const RegionTooltip: React.FC<RegionTooltipProps> = ({
  regionName,
  totalFlights,
  intensityPercentage,
  mostPopularDrone,
  x,
  y
}) => {
  return (
    <div 
      className="region-tooltip"
      style={{
        left: x + 10,
        top: y + 10
      }}
    >
      <div className="tooltip-header">
        <h4>{regionName}</h4>
      </div>
      <div className="tooltip-content">
        <div className="tooltip-row">
          <span className="label">Полетов:</span>
          <span className="value">{totalFlights.toLocaleString()}</span>
        </div>
        <div className="tooltip-row">
          <span className="label">Интенсивность:</span>
          <span className="value">{intensityPercentage}%</span>
        </div>
        <div className="tooltip-row">
          <span className="label">Популярный дрон:</span>
          <span className="value">{mostPopularDrone}</span>
        </div>
      </div>
    </div>
  );
};

export default RegionTooltip;
