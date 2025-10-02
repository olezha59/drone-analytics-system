import React, { createContext, useState, useContext, useEffect, type ReactNode } from 'react';
import apiClient from '../api/apiClient';

interface User {
  username: string;
  role: 'ADMIN' | 'ANALYST';
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (username: string, password: string) => Promise<boolean>;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Восстановление сессии при загрузке
  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    
    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string): Promise<boolean> => {
    try {
      console.log('🔄 Login attempt...');
      
      const response = await apiClient.post('/auth/login', {
        username,
        password
      });

      console.log('✅ Login success:', response.data);
      
      setToken(response.data.token);
      setUser({ username: response.data.username, role: response.data.role });
      
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify({ 
        username: response.data.username, 
        role: response.data.role 
      }));
      
      return true;
    } catch (error: any) {
      console.error('❌ Login error:', error);
      console.error('Error response:', error.response?.data);
      return false;
    }
  };

  const logout = () => {
    console.log('🚪 Logging out user:', user?.username);
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    // Принудительно перезагружаем страницу чтобы показать форму логина
    window.location.reload();
  };

  const value = {
    user,
    token,
    login,
    logout,
    isLoading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
