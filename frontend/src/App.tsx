// ~/drone-analytics-system/frontend/src/App.tsx
import React from 'react';
import DroneHeatMap from './components/Map/DroneHeatMap';
import './App.css';

// Импортируем логотип
import BrzTeamLogo from './assets/brzteam-logo.svg';

function App() {
  return (
    <div className="App">
      <header className="app-header">
        <div className="header-content">
          {/* Логотип в левом верхнем углу */}
          <img src={BrzTeamLogo} alt="BRZteam" className="logo" />
          
          {/* Заголовки по центру */}
          <div className="brand-section">
            <div className="titles">
              <h1>Аналитика полетов гражданских БПЛА</h1>
              <p className="subtitle">
                Сервис для анализа количества и длительности полетов гражданских
                беспилотников в регионах Российской Федерации для определения полетной
                активности на основе данных Росавиации (данные за 2025 г.)
              </p>
            </div>
          </div>
        </div>
      </header>
      <main>
        <DroneHeatMap />
      </main>
    </div>
  );
}

export default App;
