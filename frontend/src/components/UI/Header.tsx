import React from 'react';
import { Layout, Button, Dropdown, Space, Menu, Upload, message } from 'antd';
import { UserOutlined, LogoutOutlined, LoginOutlined, UploadOutlined } from '@ant-design/icons';
import { useAuth } from '../../context/AuthContext';
import type { UploadProps } from 'antd';

const { Header: AntHeader } = Layout;

const Header: React.FC = () => {
  const { user, logout } = useAuth();

  // Конфигурация загрузки файла
  const uploadProps: UploadProps = {
    name: 'file',
    action: 'http://localhost:8080/api/admin/upload-excel',
    headers: {
      authorization: `Bearer ${localStorage.getItem('token')}`,
    },
    accept: '.xlsx,.xls',
    showUploadList: false,
    beforeUpload: (file) => {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                     file.type === 'application/vnd.ms-excel';
      if (!isExcel) {
        message.error('Можно загружать только Excel файлы!');
        return false;
      }
      return true;
    },
    onChange: (info) => {
      if (info.file.status === 'done') {
        message.success(`${info.file.name} успешно загружен и обработан`);
        // Автоматическое обновление данных через 3 секунды
        setTimeout(() => {
          window.location.reload();
        }, 3000);
      } else if (info.file.status === 'error') {
        message.error(`Ошибка загрузки: ${info.file.response?.message || 'Неизвестная ошибка'}`);
      }
    },
  };

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

      {/* Правая часть - информация о пользователе и кнопка загрузки */}
      <Space style={{ color: 'white' }} size="middle">
        {/* Кнопка загрузки файла (только для ADMIN) */}
        {user?.role === 'ADMIN' && (
          <Upload {...uploadProps}>
            <Button 
              type="primary" 
              icon={<UploadOutlined />}
              style={{ background: '#52c41a', borderColor: '#52c41a' }}
            >
              Загрузить Excel
            </Button>
          </Upload>
        )}

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
