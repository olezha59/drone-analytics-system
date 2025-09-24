-- Создаем расширения для работы с координатами
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Таблица полетов с ВСЕМИ необходимыми полями
CREATE TABLE flight_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- Основная информация
    center_code VARCHAR(100) NOT NULL,
    flight_date DATE NOT NULL,
    takeoff_time TIMESTAMP,
    landing_time TIMESTAMP,
    flight_duration INTERVAL GENERATED ALWAYS AS (landing_time - takeoff_time) STORED,
    
    -- Координаты
    takeoff_coords GEOMETRY(POINT, 4326),
    landing_coords GEOMETRY(POINT, 4326),
    takeoff_coords_text VARCHAR(100), -- Исходный формат: 5957N02905E
    landing_coords_text VARCHAR(100),
    
    -- Регионы (определяются автоматически через геопривязку)
    region_takeoff VARCHAR(100),
    region_landing VARCHAR(100),
    
    -- Данные из SHR формата
    flight_number VARCHAR(50),        -- SID/7772187998
    aircraft_type VARCHAR(100),       -- TYP/SHAR
    operator_name VARCHAR(200),       -- OPR/МАЛИНОВСКИЙ НИКИТА АЛЕКСАНДРОВИЧ
    operator_phone VARCHAR(20),       -- +79313215153
    remarks TEXT,                     -- RMK/ОБОЛОЧКА 300 ДЛЯ ЗОНДИРОВАНИЯ АТМОСФЕРЫ
    
    -- Исходные сырые данные
    raw_shr_data JSONB,
    raw_dep_data JSONB,
    raw_arr_data JSONB,
    
    -- Технические поля
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Индексы для быстрого поиска
CREATE INDEX idx_flight_records_center ON flight_records(center_code);
CREATE INDEX idx_flight_records_date ON flight_records(flight_date);
CREATE INDEX idx_flight_records_takeoff_geom ON flight_records USING GIST(takeoff_coords);
CREATE INDEX idx_flight_records_landing_geom ON flight_records USING GIST(landing_coords);

-- Индексы для новых полей
CREATE INDEX idx_flight_records_number ON flight_records(flight_number);
CREATE INDEX idx_flight_records_operator ON flight_records(operator_name);
CREATE INDEX idx_flight_records_type ON flight_records(aircraft_type);
CREATE INDEX idx_flight_records_region_takeoff ON flight_records(region_takeoff);

-- Таблица регионов РФ
CREATE TABLE russian_regions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(10),
    geom GEOMETRY(MULTIPOLYGON, 4326),
    area_km2 NUMERIC
);

CREATE INDEX idx_russian_regions_geom ON russian_regions USING GIST(geom);
