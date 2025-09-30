// ~/drone-analytics-system/frontend/src/types/region.ts
export interface IRegionStats {
  regionId: number;
  uniqueOperators: number;
  flightsByAircraftType: Record<string, number>;
  flightsByOperator: Record<string, number>;
  totalFlights: number;
  centerCodes: string[];
  averageFlightDuration: number;
}

export interface IRegionFeatureProperties {
  id: number;
  name?: string;
  totalFlights?: number;
  uniqueOperators?: number;
  averageFlightDuration?: number;
  normalizedValue?: number;
  color?: string;
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
