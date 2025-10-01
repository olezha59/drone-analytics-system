import React from 'react';
import { useAuth } from '../context/AuthContext';
import LoginForm from './LoginForm';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { user, isLoading } = useAuth();

  console.log('🛡️ ProtectedRoute - User:', user, 'Loading:', isLoading);

  if (isLoading) {
    return (
      <div className="loading-container">
        <div>Проверка авторизации...</div>
      </div>
    );
  }

  if (!user) {
    console.log('🛡️ No user - showing login form');
    return <LoginForm />;
  }

  console.log('🛡️ User authenticated - showing content');
  return <>{children}</>;
};

export default ProtectedRoute;