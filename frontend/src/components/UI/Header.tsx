import React from 'react';
import { Layout } from 'antd';

const { Header: AntHeader } = Layout;

const Header: React.FC = () => {
  return (
    <AntHeader style={{ 
      background: '#001529', 
      color: 'white',
      display: 'flex',
      alignItems: 'center',
      padding: '0 20px'
    }}>
      <h1 style={{ color: 'white', margin: 0, fontSize: '24px' }}>BRZteam - Drone Analytics System</h1>
    </AntHeader>
  );
};

export default Header;
