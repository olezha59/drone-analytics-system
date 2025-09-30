// src/components/Analytics/RegionStats.tsx
import React from 'react';
import { Card, List, Typography, Tag } from 'antd';
import { RegionStats } from '../../types/region';

const { Text } = Typography;

interface RegionStatsProps {
  stats: RegionStats;
}

const RegionStatsPanel: React.FC<RegionStatsProps> = ({ stats }) => {
  const getColorByFlights = (flights: number) => {
    if (flights > 1000) return '#ff4d4f';
    if (flights > 500) return '#faad14';
    if (flights > 100) return '#52c41a';
    return '#91d5ff';
  };

  return (
    <Card 
      title={`📊 ${stats.name}`}
      extra={
        <Tag color={getColorByFlights(stats.flightCount)}>
          {stats.flightCount > 1000 ? 'Высокая' : 
           stats.flightCount > 500 ? 'Средняя' : 
           stats.flightCount > 100 ? 'Низкая' : 'Минимальная'} активность
        </Tag>
      }
    >
      <List size="small">
        <List.Item>
          <List.Item.Meta 
            title="Количество полётов" 
            description={<Text strong>{stats.flightCount.toLocaleString()}</Text>} 
          />
        </List.Item>
        <List.Item>
          <List.Item.Meta 
            title="Уникальных операторов" 
            description={stats.operatorCount} 
          />
        </List.Item>
        <List.Item>
          <List.Item.Meta 
            title="Средняя длительность" 
            description={`${stats.avgDuration} мин`} 
          />
        </List.Item>
        <List.Item>
          <List.Item.Meta 
            title="Плотность полётов" 
            description={`${stats.flightDensity.toFixed(1)} на 1000 км²`} 
          />
        </List.Item>
        <List.Item>
          <List.Item.Meta 
            title="Пиковая нагрузка" 
            description={`${stats.peakLoad} полётов/час`} 
          />
        </List.Item>
        <List.Item>
          <List.Item.Meta 
            title="Дней без полётов" 
            description={stats.zeroDays} 
          />
        </List.Item>
        <List.Item>
          <List.Item.Meta 
            title="Рост за месяц" 
            description={`${stats.growthRate > 0 ? '+' : ''}${stats.growthRate.toFixed(1)}%`} 
          />
        </List.Item>
        <List.Item>
          <List.Item.Meta 
            title="Прогноз на янв 2026" 
            description={`${stats.predictionJan2026.toLocaleString()} полётов`} 
          />
        </List.Item>
      </List>

      <div style={{ marginTop: '16px' }}>
        <Text strong>Суточная активность:</Text>
        <div style={{ marginTop: '8px' }}>
          <div>Утро (6-12): {stats.dailyActivity.morning}%</div>
          <div>День (12-18): {stats.dailyActivity.day}%</div>
          <div>Вечер (18-24): {stats.dailyActivity.evening}%</div>
          <div>Ночь (0-6): {stats.dailyActivity.night}%</div>
        </div>
      </div>

      <div style={{ marginTop: '16px' }}>
        <Text strong>Самый популярный дрон:</Text>
        <div>{stats.mostPopularDrone}</div>
      </div>
    </Card>
  );
};

export default RegionStatsPanel;
