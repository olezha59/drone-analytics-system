// Утилиты для тепловой карты
export const getColorByFlights = (flights: number): string => {
  if (flights === 0) return '#f0f0f0';
  if (flights > 1000) return '#ff4d4f';
  if (flights > 500) return '#faad14';
  if (flights > 100) return '#52c41a';
  return '#91d5ff';
};

export const getActivityLevel = (flights: number): string => {
  if (flights > 1000) return 'Высокая';
  if (flights > 500) return 'Средняя';
  if (flights > 100) return 'Низкая';
  if (flights > 0) return 'Минимальная';
  return 'Нет активности';
};
