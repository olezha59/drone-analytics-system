// frontend/src/pages/MainPage.tsx
import React from 'react';
import RussiaMapBasic from '../components/Map/RussiaMapBasic';

const MainPage: React.FC = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>BRZteam — Аналитика дронов по РФ</h1>
      <RussiaMapBasic />
    </div>
  );
};

export default MainPage;
