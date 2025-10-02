import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Cell } from 'recharts';

interface YearlyChartProps {
  yearlyDistribution: Record<number, number>;
}

const YearlyChart: React.FC<YearlyChartProps> = ({ yearlyDistribution }) => {
  if (!yearlyDistribution || Object.keys(yearlyDistribution).length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '20px', color: '#999' }}>
        <p>Нет данных о распределении полетов по годам</p>
      </div>
    );
  }

  // Преобразуем объект в массив и сортируем по годам
  const yearlyData = Object.entries(yearlyDistribution)
    .map(([year, count]) => ({
      year: parseInt(year),
      flights: count
    }))
    .sort((a, b) => a.year - b.year);

  // Находим максимальное количество полетов
  const maxFlights = Math.max(...yearlyData.map(item => item.flights));
  const busiestYear = yearlyData.reduce((max, current) => 
    current.flights > max.flights ? current : max, yearlyData[0]
  );

  // Цвета для столбцов
  const getBarColor = (year: number, flights: number) => {
    if (year === busiestYear.year) return '#ff4d4f'; // Красный для самого активного
    const intensity = flights / maxFlights;
    if (intensity > 0.8) return '#ff7c45';
    if (intensity > 0.6) return '#ffa940';
    if (intensity > 0.4) return '#ffc53d';
    if (intensity > 0.2) return '#bae637';
    return '#73d13d';
  };

  return (
    <div className="yearly-chart">
      <h4>📈 Распределение полетов по годам</h4>
      
      {/* Информация о самом насыщенном годе */}
      <div style={{ 
        background: '#f0f8ff', 
        padding: '12px', 
        borderRadius: '6px', 
        marginBottom: '20px',
        textAlign: 'center',
        border: '1px solid #1890ff'
      }}>
        <strong style={{ color: '#1890ff' }}>
          🏆 Самый насыщенный по полетам год: <span style={{color: '#ff4d4f'}}>{busiestYear.year}</span> 
          ({busiestYear.flights.toLocaleString()} полетов)
        </strong>
      </div>

      {/* График */}
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={yearlyData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis 
            dataKey="year" 
            angle={-45}
            textAnchor="end"
            height={60}
            interval={0}
            tick={{ fontSize: 12 }}
          />
          <YAxis 
            label={{ 
              value: 'Количество полетов', 
              angle: -90, 
              position: 'insideLeft',
              offset: -10,
              style: { textAnchor: 'middle' }
            }}
          />
          <Tooltip 
            formatter={(value) => [`${value} полетов`, 'Количество']}
            labelFormatter={(label) => `Год: ${label}`}
          />
          <Legend />
          <Bar 
            dataKey="flights" 
            name="Количество полетов"
            radius={[4, 4, 0, 0]}
          >
            {yearlyData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={getBarColor(entry.year, entry.flights)} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>

      {/* Легенда */}
      <div style={{ 
        marginTop: '15px', 
        padding: '10px', 
        background: '#f5f5f5', 
        borderRadius: '4px',
        fontSize: '12px',
        color: '#666',
        textAlign: 'center'
      }}>
        <p>
          Всего лет: {yearlyData.length} • 
          Диапазон: {yearlyData[0]?.year} - {yearlyData[yearlyData.length - 1]?.year} • 
          Всего полетов: {yearlyData.reduce((sum, item) => sum + item.flights, 0).toLocaleString()}
        </p>
      </div>
    </div>
  );
};

export default YearlyChart;
