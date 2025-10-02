import React from 'react';
import './RegionDetails.css';
import YearlyChart from './YearlyChart';

interface IRegionStats {
  regionId: number;
  uniqueOperators?: number;
  flightsByAircraftType?: Record<string, number>;
  flightsByOperator?: Record<string, number>;
  totalFlights?: number;
  centerCodes?: string[];
  averageFlightDuration?: number;
  periodDescription?: string;
  startDate?: string;
  endDate?: string;
  zeroDays?: number;
  dailyActivity?: {
    morning?: number;
    day?: number;
    evening?: number;
    night?: number;
  };
  averageDailyFlights?: {
    daysWithFlights: number;
    averageFlightsPerDay: number;
    totalDaysInPeriod: number;
  };
  yearlyDistribution?: Record<number, number>;
  // 🆕 ДОБАВЛЯЕМ ПОЛЕ ДЛЯ САМОГО АКТИВНОГО ГОДА
  mostActiveYear?: {
    year: number;
    flightsCount: number;
  };
}

interface RegionDetailsProps {
  regionStats: IRegionStats | null;
  regionName: string;
  onClose: () => void;
}

const RegionDetails: React.FC<RegionDetailsProps> = ({ regionStats, regionName, onClose }) => {
  if (!regionStats) {
    return (
      <div className="region-details">
        <div className="loading-message">Загрузка статистики...</div>
      </div>
    );
  }

  const getPopularDrone = (): string => {
    if (!regionStats.flightsByAircraftType) return 'N/A';
    const types = Object.entries(regionStats.flightsByAircraftType);
    if (types.length === 0) return 'N/A';
    return types.sort(([,a], [,b]) => b - a)[0][0];
  };

  const formatDailyActivity = () => {
    if (!regionStats.dailyActivity) return null;
    
    const { morning = 0, day = 0, evening = 0, night = 0 } = regionStats.dailyActivity;
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

  return (
    <div className="region-details">
      <div className="details-header">
        <h3>📊 {regionName}</h3>
        <button className="close-btn" onClick={onClose} title="Закрыть">
          ×
        </button>
      </div>

      <div className="period-info">
        {regionStats.periodDescription || 'За весь период'}
      </div>

      <div className="stats-grid">
        <div className="stat-card main-stat">
          <div className="stat-value">{regionStats.totalFlights?.toLocaleString() || 0}</div>
          <div className="stat-label">Всего полетов</div>
        </div>
        
        <div className="stat-card">
          <div className="stat-value">{regionStats.uniqueOperators?.toLocaleString() || 0}</div>
          <div className="stat-label">Уникальных операторов</div>
        </div>
        
        <div className="stat-card">
          <div className="stat-value">{Math.round(regionStats.averageFlightDuration || 0)}</div>
          <div className="stat-label">Ср. длительность (мин)</div>
        </div>

        {regionStats.dailyActivity && (
          <>
            <div className="stat-card">
              <div className="stat-value">{regionStats.dailyActivity.morning || 0}</div>
              <div className="stat-label">Утром (6-12)</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{regionStats.dailyActivity.day || 0}</div>
              <div className="stat-label">Днем (12-18)</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{regionStats.dailyActivity.evening || 0}</div>
              <div className="stat-label">Вечером (18-24)</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{regionStats.dailyActivity.night || 0}</div>
              <div className="stat-label">Ночью (0-6)</div>
            </div>
          </>
        )}

        {regionStats.zeroDays !== undefined && (
          <div className="stat-card">
            <div className="stat-value">{regionStats.zeroDays}</div>
            <div className="stat-label">Дней без полетов</div>
          </div>
        )}

        {regionStats.averageDailyFlights && (
          <div className="stat-card">
            <div className="stat-value">{regionStats.averageDailyFlights.averageFlightsPerDay?.toFixed(1) || 0}</div>
            <div className="stat-label">Ср. полетов в день</div>
          </div>
        )}

        <div className="stat-card">
          <div className="stat-value">{getPopularDrone()}</div>
          <div className="stat-label">Популярный дрон</div>
        </div>
      </div>

      {dailyActivity && (
        <div className="daily-activity">
          <h4>⏰ Суточная активность (%)</h4>
          <div className="activity-bars">
            <div className="activity-bar">
              <span className="time-label">Утро (6-12)</span>
              <div className="bar-container">
                <div 
                  className="bar-fill morning" 
                  style={{ width: `${dailyActivity.morning}%` }}
                >
                  <span className="bar-value">{dailyActivity.morning}%</span>
                </div>
              </div>
            </div>
            <div className="activity-bar">
              <span className="time-label">День (12-18)</span>
              <div className="bar-container">
                <div 
                  className="bar-fill day" 
                  style={{ width: `${dailyActivity.day}%` }}
                >
                  <span className="bar-value">{dailyActivity.day}%</span>
                </div>
              </div>
            </div>
            <div className="activity-bar">
              <span className="time-label">Вечер (18-24)</span>
              <div className="bar-container">
                <div 
                  className="bar-fill evening" 
                  style={{ width: `${dailyActivity.evening}%` }}
                >
                  <span className="bar-value">{dailyActivity.evening}%</span>
                </div>
              </div>
            </div>
            <div className="activity-bar">
              <span className="time-label">Ночь (0-6)</span>
              <div className="bar-container">
                <div 
                  className="bar-fill night" 
                  style={{ width: `${dailyActivity.night}%` }}
                >
                  <span className="bar-value">{dailyActivity.night}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 🆕 ГРАФИК РАСПРЕДЕЛЕНИЯ ПО ГОДАМ */}
      {regionStats.yearlyDistribution && (
        <div className="yearly-distribution">
          <YearlyChart yearlyDistribution={regionStats.yearlyDistribution} />
        </div>
      )}

      {/* 🆕 САМЫЙ АКТИВНЫЙ ГОД */}
      {regionStats.mostActiveYear && (
        <div className="most-active-year">
          <h4>🎯 Самый насыщенный по полетам год</h4>
          <div className="active-year-card">
            <div className="year-value">{regionStats.mostActiveYear.year}</div>
            <div className="year-flights">{regionStats.mostActiveYear.flightsCount?.toLocaleString()} полетов</div>
          </div>
        </div>
      )}

      {regionStats.centerCodes && regionStats.centerCodes.length > 0 && (
        <div className="centers-info">
          <h4>🏛️ Центры управления</h4>
          <div className="centers-list">
            {regionStats.centerCodes.map((center, index) => (
              <span key={index} className="center-tag">{center}</span>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default RegionDetails;
