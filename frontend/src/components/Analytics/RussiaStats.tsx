import React from 'react';
import { Card, Progress, Typography, Button } from 'antd';

const { Title, Text } = Typography;

interface RussiaStatsProps {
  totalFlights: number;
  totalOperators: number;
  totalRegions: number;
  dailyActivity?: {
    morning: number;
    day: number;
    evening: number;
    night: number;
  };
  onClose: () => void;
}

const RussiaStats: React.FC<RussiaStatsProps> = ({
  totalFlights,
  totalOperators,
  totalRegions,
  dailyActivity,
  onClose
}) => {
  // Расчет процентов для диаграммы
  const calculatePercentages = () => {
    if (!dailyActivity) return null;
    
    const { morning, day, evening, night } = dailyActivity;
    const total = morning + day + evening + night;
    
    if (total === 0) return null;

    return {
      morning: Math.round((morning / total) * 100),
      day: Math.round((day / total) * 100),
      evening: Math.round((evening / total) * 100),
      night: Math.round((night / total) * 100),
      absolute: { morning, day, evening, night }
    };
  };

  const activityPercentages = calculatePercentages();

  return (
    <Card 
      title={
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>📊 Статистика по Российской Федерации</span>
          <Button 
            type="text" 
            onClick={onClose}
            style={{ color: '#ff4d4f' }}
          >
            ✕
          </Button>
        </div>
      }
      style={{ marginBottom: '20px' }}
    >
      {/* Основные метрики РФ */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px', marginBottom: '20px' }}>
        <div style={{ textAlign: 'center', padding: '20px', background: '#f0f8ff', borderRadius: '8px' }}>
          <Title level={2} style={{ margin: 0, color: '#1890ff' }}>
            {totalFlights?.toLocaleString() || 0}
          </Title>
          <Text type="secondary">Всего полетов</Text>
        </div>
        
        <div style={{ textAlign: 'center', padding: '20px', background: '#f6ffed', borderRadius: '8px' }}>
          <Title level={2} style={{ margin: 0, color: '#52c41a' }}>
            {totalOperators?.toLocaleString() || 0}
          </Title>
          <Text type="secondary">Уникальных операторов</Text>
        </div>
        
        <div style={{ textAlign: 'center', padding: '20px', background: '#fff7e6', borderRadius: '8px' }}>
          <Title level={2} style={{ margin: 0, color: '#fa8c16' }}>
            {totalRegions || 0}
          </Title>
          <Text type="secondary">Регионов</Text>
        </div>
      </div>

      {/* Суточная активность РФ */}
      {activityPercentages ? (
        <div style={{ marginTop: '20px' }}>
          <Title level={4}>⏰ Распределение полетов по времени суток</Title>
          
          {/* Абсолютные числа */}
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(4, 1fr)', 
            gap: '12px', 
            marginBottom: '20px' 
          }}>
            <div style={{ textAlign: 'center', padding: '12px', background: '#fff7e6', borderRadius: '6px' }}>
              <Text strong style={{ fontSize: '16px', color: '#fa8c16' }}>
                {activityPercentages.absolute.morning.toLocaleString()}
              </Text>
              <div style={{ fontSize: '12px', color: '#666' }}>Утром (6-12)</div>
            </div>
            <div style={{ textAlign: 'center', padding: '12px', background: '#f6ffed', borderRadius: '6px' }}>
              <Text strong style={{ fontSize: '16px', color: '#52c41a' }}>
                {activityPercentages.absolute.day.toLocaleString()}
              </Text>
              <div style={{ fontSize: '12px', color: '#666' }}>Днем (12-18)</div>
            </div>
            <div style={{ textAlign: 'center', padding: '12px', background: '#fff2f0', borderRadius: '6px' }}>
              <Text strong style={{ fontSize: '16px', color: '#ff4d4f' }}>
                {activityPercentages.absolute.evening.toLocaleString()}
              </Text>
              <div style={{ fontSize: '12px', color: '#666' }}>Вечером (18-24)</div>
            </div>
            <div style={{ textAlign: 'center', padding: '12px', background: '#f9f0ff', borderRadius: '6px' }}>
              <Text strong style={{ fontSize: '16px', color: '#722ed1' }}>
                {activityPercentages.absolute.night.toLocaleString()}
              </Text>
              <div style={{ fontSize: '12px', color: '#666' }}>Ночью (0-6)</div>
            </div>
          </div>

          {/* Прогресс-бары */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                <Text>Утро (6-12):</Text>
                <Text strong>{activityPercentages.morning}%</Text>
              </div>
              <Progress 
                percent={activityPercentages.morning} 
                strokeColor="#faad14"
                showInfo={false}
              />
            </div>
            
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                <Text>День (12-18):</Text>
                <Text strong>{activityPercentages.day}%</Text>
              </div>
              <Progress 
                percent={activityPercentages.day} 
                strokeColor="#52c41a"
                showInfo={false}
              />
            </div>
            
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                <Text>Вечер (18-24):</Text>
                <Text strong>{activityPercentages.evening}%</Text>
              </div>
              <Progress 
                percent={activityPercentages.evening} 
                strokeColor="#fa8c16"
                showInfo={false}
              />
            </div>
            
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                <Text>Ночь (0-6):</Text>
                <Text strong>{activityPercentages.night}%</Text>
              </div>
              <Progress 
                percent={activityPercentages.night} 
                strokeColor="#722ed1"
                showInfo={false}
              />
            </div>
          </div>
        </div>
      ) : (
        <div style={{ textAlign: 'center', padding: '20px', color: '#999' }}>
          <Text>Нет данных о распределении полетов по времени суток</Text>
        </div>
      )}
    </Card>
  );
};

export default RussiaStats;
