-- Создаем расширения для работы с координатами
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Таблица полетов
CREATE TABLE flight_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    center_code VARCHAR(100) NOT NULL,
    flight_date DATE NOT NULL,
    takeoff_time TIMESTAMP,
    landing_time TIMESTAMP,
    flight_duration INTERVAL GENERATED ALWAYS AS (landing_time - takeoff_time) STORED,
    takeoff_coords GEOMETRY(POINT, 4326),
    landing_coords GEOMETRY(POINT, 4326),
    region_takeoff VARCHAR(100),
    region_landing VARCHAR(100),
    raw_shr_data JSONB,
    raw_dep_data JSONB,
    raw_arr_data JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Индексы
CREATE INDEX idx_flight_records_center ON flight_records(center_code);
CREATE INDEX idx_flight_records_date ON flight_records(flight_date);
CREATE INDEX idx_flight_records_takeoff_geom ON flight_records USING GIST(takeoff_coords);
CREATE INDEX idx_flight_records_landing_geom ON flight_records USING GIST(landing_coords);

-- Таблица регионов РФ
CREATE TABLE russian_regions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(10),
    geom GEOMETRY(MULTIPOLYGON, 4326),
    area_km2 NUMERIC
);

CREATE INDEX idx_russian_regions_geom ON russian_regions USING GIST(geom);

