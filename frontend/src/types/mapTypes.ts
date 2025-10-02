export interface IRegionStats {
  regionId: number;
  uniqueOperators?: number;
  flightsByAircraftType?: Record<string, number>;
  flightsByOperator?: Record<string, number>;
  totalFlights?: number;
  centerCodes?: string[];
  averageFlightDuration?: number;
  periodDescription?: string;
  startDate?: string;
  endDate?: string;
  zeroDays?: number;
  dailyActivity?: {
    morning?: number;
    day?: number;
    evening?: number;
    night?: number;
  };
  averageDailyFlights?: {
    daysWithFlights: number;
    averageFlightsPerDay: number;
    totalDaysInPeriod: number;
  };
  growthDecline?: {
    firstMonthFlights: number;
    lastMonthFlights: number;
    trend: string;
    firstMonth: string;
    lastMonth: string;
    monthBetween: number;
    changePercentage: number;
  };
  // 🆕 ДОБАВЛЯЕМ РАСПРЕДЕЛЕНИЕ ПО ГОДАМ
  yearlyDistribution?: Record<number, number>;
}

// ... остальные типы без изменений ...
