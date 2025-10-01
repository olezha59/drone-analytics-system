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
}

export interface IRegionFeatureProperties {
  id: number;
  name?: string;
  totalFlights?: number;
  uniqueOperators?: number;
  averageFlightDuration?: number;
  normalizedValue?: number;
  color?: string;
  flightsByAircraftType?: Record<string, number>;
  centerCodes?: string[];
}

export interface IRegionFeature {
  type: "Feature";
  id: number;
  geometry: {
    type: "MultiPolygon";
    coordinates: number[][][][];
  };
  properties: IRegionFeatureProperties;
}

export interface IRegionsGeoJSON {
  type: "FeatureCollection";
  features: IRegionFeature[];
}

export type Region = {
  id: number;
  name: string;
};
