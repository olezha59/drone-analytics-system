import React from 'react';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return <div>Загрузка...</div>;
  }

  if (!user) {
    return <div>Доступ запрещен. Пожалуйста, войдите в систему.</div>;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
