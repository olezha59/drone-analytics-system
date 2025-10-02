import React from 'react';
import { Layout } from 'antd';
import DroneHeatMap from './components/Map/DroneHeatMap';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginForm from './components/LoginForm';
import Header from './components/UI/Header';
import './App.css';

const { Content } = Layout;

// Компонент основного контента (после авторизации)
const AppContent: React.FC = () => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        flexDirection: 'column',
        gap: '20px'
      }}>
        <div>Загрузка системы...</div>
      </div>
    );
  }

  if (!user) {
    return <LoginForm />;
  }

  return (
    <div className="App">
      <Header />
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
