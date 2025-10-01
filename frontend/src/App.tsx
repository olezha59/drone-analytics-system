import React from 'react';
import DroneHeatMap from './components/Map/DroneHeatMap';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginForm from './components/LoginForm';
import './App.css';

// Импортируем логотип
import BrzTeamLogo from './assets/brzteam-logo.svg';

// Компонент основного контента (после авторизации)
const AppContent: React.FC = () => {
  const { user, logout } = useAuth();

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

          {/* Блок пользователя (справа) */}
          {user && (
            <div className="user-section">
              <span className="user-info">
                {user.username} ({user.role === 'ADMIN' ? 'Администратор' : 'Аналитик'})
              </span>
              <button 
                onClick={logout}
                className="logout-button"
                style={{
                  marginLeft: '10px',
                  padding: '5px 10px',
                  background: '#ff4d4f',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Выйти
              </button>
            </div>
          )}
        </div>
      </header>

      <main>
        <ProtectedRoute>
          <DroneHeatMap />
        </ProtectedRoute>
      </main>
    </div>
  );
};

// Главный компонент с провайдером аутентификации
const App: React.FC = () => {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
};

export default App;