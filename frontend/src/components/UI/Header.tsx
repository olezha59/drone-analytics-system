import React from 'react';
import { Layout, Button, Dropdown, Space, Menu } from 'antd';
import { UserOutlined, LogoutOutlined, LoginOutlined } from '@ant-design/icons';
import { useAuth } from '../../context/AuthContext';

const { Header: AntHeader } = Layout;

const Header: React.FC = () => {
  const { user, logout } = useAuth();

  const userMenu = (
    <Menu>
      <Menu.Item key="user" icon={<UserOutlined />}>
        {user?.username} ({user?.role === 'ADMIN' ? 'Администратор' : 'Аналитик'})
      </Menu.Item>
      <Menu.Divider />
      <Menu.Item key="logout" icon={<LogoutOutlined />} onClick={logout}>
        Выйти
      </Menu.Item>
    </Menu>
  );

  return (
    <AntHeader style={{ 
      background: '#001529', 
      color: 'white',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 20px'
    }}>
      {/* Левая часть - заголовок */}
      <div style={{ display: 'flex', alignItems: 'center' }}>
        <h1 style={{ color: 'white', margin: 0, fontSize: '24px' }}>
          BRZteam - Drone Analytics System
        </h1>
      </div>

      {/* Правая часть - информация о пользователе */}
      <Space style={{ color: 'white' }} size="middle">
        {user ? (
          <Dropdown overlay={userMenu} placement="bottomRight">
            <Button 
              type="text" 
              style={{ color: 'white' }}
              icon={<UserOutlined />}
            >
              {user.username} ({user.role === 'ADMIN' ? 'Админ' : 'Аналитик'})
            </Button>
          </Dropdown>
        ) : (
          <Button 
            type="text" 
            style={{ color: 'white' }}
            icon={<LoginOutlined />}
            onClick={() => window.location.reload()}
          >
            Войти
          </Button>
        )}
      </Space>
    </AntHeader>
  );
};

export default Header;