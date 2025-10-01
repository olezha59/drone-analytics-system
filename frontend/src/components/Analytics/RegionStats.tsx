// frontend/src/components/Analytics/RegionStats.tsx
import React from 'react';
import { Card, List, Typography, Tag, Progress } from 'antd';
import { IRegionStats } from '../../types/region';

const { Text, Title } = Typography;

interface RegionStatsProps {
  stats: IRegionStats;
  regionName: string;
}

const RegionStatsPanel: React.FC<RegionStatsProps> = ({ stats, regionName }) => {
  const getColorByFlights = (flights: number) => {
    if (flights > 1000) return '#ff4d4f';
    if (flights > 500) return '#faad14';
    if (flights > 100) return '#52c41a';
    return '#91d5ff';
  };

  // Получаем самый популярный дрон
  const getPopularDrone = (): string => {
    if (!stats.flightsByAircraftType) return 'N/A';
    const types = Object.entries(stats.flightsByAircraftType);
    if (types.length === 0) return 'N/A';
    return types.sort(([,a], [,b]) => b - a)[0][0];
  };

  // Форматируем суточную активность
  const formatDailyActivity = () => {
    if (!stats.dailyActivity) return null;
    
    const { morning = 0, day = 0, evening = 0, night = 0 } = stats.dailyActivity;
    const total = morning + day + evening + night;
    
    if (total === 0) return null;

    return {
      morning: Math.round((morning / total) * 100),
      day: Math.round((day / total) * 100),
      evening: Math.round((evening / total) * 100),
      night: Math.round((night / total) * 100)
    };
  };

  const dailyActivity = formatDailyActivity();
  const popularDrone = getPopularDrone();

  return (
    <Card 
      title={`📊 ${regionName}`}
      extra={
        <Tag color={getColorByFlights(stats.totalFlights)}>
          {stats.totalFlights > 1000 ? 'Высокая' : 
           stats.totalFlights > 500 ? 'Средняя' : 
           stats.totalFlights > 100 ? 'Низкая' : 'Минимальная'} активность
        </Tag>
      }
      style={{ marginBottom: '20px' }}
    >
      {/* Основные метрики */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px', marginBottom: '20px' }}>
        <div style={{ textAlign: 'center', padding: '12px', background: '#f0f8ff', borderRadius: '8px' }}>
          <Title level={3} style={{ margin: 0, color: '#1890ff' }}>
            {stats.totalFlights?.toLocaleString() || 0}
          </Title>
          <Text type="secondary">Всего полетов</Text>
        </div>
        
        <div style={{ textAlign: 'center', padding: '12px', background: '#f6ffed', borderRadius: '8px' }}>
          <Title level={3} style={{ margin: 0, color: '#52c41a' }}>
            {stats.uniqueOperators?.toLocaleString() || 0}
          </Title>
          <Text type="secondary">Уникальных операторов</Text>
        </div>
        
        <div style={{ textAlign: 'center', padding: '12px', background: '#fff7e6', borderRadius: '8px' }}>
          <Title level={3} style={{ margin: 0, color: '#fa8c16' }}>
            {Math.round(stats.averageFlightDuration || 0)}
          </Title>
          <Text type="secondary">Ср. длительность (мин)</Text>
        </div>
        
        <div style={{ textAlign: 'center', padding: '12px', background: '#fff2f0', borderRadius: '8px' }}>
          <Title level={3} style={{ margin: 0, color: '#ff4d4f' }}>
            {stats.zeroDays || 0}
          </Title>
          <Text type="secondary">Дней без полетов</Text>
        </div>
      </div>

      <List size="small">
        {/* Средние полеты в день */}
        {stats.averageDailyFlights && (
          <List.Item>
            <List.Item.Meta 
              title="Среднее количество полетов в день" 
              description={
                <div>
                  <Text strong>{stats.averageDailyFlights.averageFlightsPerDay?.toFixed(2) || 0}</Text>
                  <br />
                  <Text type="secondary">
                    Дней с полетами: {stats.averageDailyFlights.daysWithFlights} / {stats.averageDailyFlights.totalDaysInPeriod}
                  </Text>
                </div>
              } 
            />
          </List.Item>
        )}

        {/* Самый популярный дрон */}
        <List.Item>
          <List.Item.Meta 
            title="Самый популярный тип БПЛА" 
            description={<Text strong>{popularDrone}</Text>} 
          />
        </List.Item>

        {/* Центры управления */}
        {stats.centerCodes && stats.centerCodes.length > 0 && (
          <List.Item>
            <List.Item.Meta 
              title="Центры управления полетами" 
              description={
                <div>
                  {stats.centerCodes.map((center, index) => (
                    <Tag key={index} color="blue" style={{ marginBottom: '4px' }}>
                      {center}
                    </Tag>
                  ))}
                </div>
              } 
            />
          </List.Item>
        )}

        {/* Период данных */}
        {stats.periodDescription && (
          <List.Item>
            <List.Item.Meta 
              title="Период данных" 
              description={<Text type="secondary">{stats.periodDescription}</Text>} 
            />
          </List.Item>
        )}
      </List>

      {/* Суточная активность */}
      {dailyActivity && (
        <div style={{ marginTop: '20px' }}>
          <Text strong>Суточная активность:</Text>
          <div style={{ marginTop: '12px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
              <Text>Утро (6-12):</Text>
              <Text strong>{dailyActivity.morning}%</Text>
            </div>
            <Progress percent={dailyActivity.morning} strokeColor="#faad14" />
            
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', marginTop: '12px' }}>
              <Text>День (12-18):</Text>
              <Text strong>{dailyActivity.day}%</Text>
            </div>
            <Progress percent={dailyActivity.day} strokeColor="#52c41a" />
            
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', marginTop: '12px' }}>
              <Text>Вечер (18-24):</Text>
              <Text strong>{dailyActivity.evening}%</Text>
            </div>
            <Progress percent={dailyActivity.evening} strokeColor="#fa8c16" />
            
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', marginTop: '12px' }}>
              <Text>Ночь (0-6):</Text>
              <Text strong>{dailyActivity.night}%</Text>
            </div>
            <Progress percent={dailyActivity.night} strokeColor="#722ed1" />
          </div>
        </div>
      )}

      {/* Рост/снижение */}
      {stats.growthDecline && (
        <div style={{ marginTop: '20px', padding: '12px', background: '#f9f9f9', borderRadius: '6px' }}>
          <Text strong>Динамика за период:</Text>
          <div style={{ marginTop: '8px' }}>
            <Text>
              {stats.growthDecline.trend === 'growth' ? '📈 Рост' : '📉 Снижение'} на {Math.abs(stats.growthDecline.changePercentage).toFixed(1)}%
            </Text>
            <br />
            <Text type="secondary">
              С {stats.growthDecline.firstMonthFlights} до {stats.growthDecline.lastMonthFlights} полетов в месяц
            </Text>
          </div>
        </div>
      )}
    </Card>
  );
};

export default RegionStatsPanel;
