import React from 'react';
import { getColorByValue } from '../../utils/dataProcessor';

interface HeatMapLegendProps {
  minFlights: number;
  maxFlights: number;
  totalRegions: number;
  regionsWithData: number;
}

const HeatMapLegend: React.FC<HeatMapLegendProps> = ({ 
  minFlights, 
  maxFlights, 
  totalRegions, 
  regionsWithData 
}) => {
  const gradientStops = [0, 0.25, 0.5, 0.75, 1].map(value => ({
    value,
    color: getColorByValue(value),
    label: value === 0 ? '0' : value === 1 ? 'Макс' : `${Math.round(value * 100)}%`
  }));

  return (
    <div style={{
      position: 'absolute',
      bottom: '20px',
      right: '20px',
      background: 'white',
      padding: '15px',
      borderRadius: '8px',
      boxShadow: '0 2px 10px rgba(0,0,0,0.2)',
      zIndex: 1000,
      minWidth: '200px'
    }}>
      <h4 style={{ margin: '0 0 10px 0', fontSize: '14px', color: '#333', textAlign: 'center' }}>
        Тепловая карта полетов БПЛА
      </h4>
      
      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
        {gradientStops.map((stop, index) => (
          <div key={index} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div
              style={{
                width: '20px',
                height: '20px',
                backgroundColor: stop.color,
                borderRadius: '2px',
                border: '1px solid #ddd'
              }}
            />
            <span style={{ fontSize: '12px', color: '#666' }}>{stop.label}</span>
          </div>
        ))}
      </div>
      
      <div style={{
        marginTop: '10px',
        paddingTop: '10px',
        borderTop: '1px solid #eee',
        fontSize: '11px',
        color: '#666'
      }}>
        <p style={{ margin: '2px 0' }}>Диапазон полетов: {minFlights} - {maxFlights}</p>
        <p style={{ margin: '2px 0' }}>Регионы с данными: {regionsWithData}/{totalRegions}</p>
      </div>
    </div>
  );
};

export default HeatMapLegend;
