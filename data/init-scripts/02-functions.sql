-- Функция для парсинга координат из формата 5957N02905E
CREATE OR REPLACE FUNCTION parse_coordinates(coord_text TEXT)
RETURNS GEOMETRY AS $$
DECLARE
    lat_deg NUMERIC;
    lat_min NUMERIC;
    lon_deg NUMERIC;
    lon_min NUMERIC;
    lat_dir TEXT;
    lon_dir TEXT;
BEGIN
    IF coord_text IS NULL OR length(coord_text) < 11 THEN
        RETURN NULL;
    END IF;
    
    -- Парсинг формата 5957N02905E
    lat_deg := SUBSTRING(coord_text FROM 1 FOR 2)::NUMERIC;
    lat_min := SUBSTRING(coord_text FROM 3 FOR 2)::NUMERIC;
    lat_dir := SUBSTRING(coord_text FROM 5 FOR 1);
    
    lon_deg := SUBSTRING(coord_text FROM 6 FOR 3)::NUMERIC;
    lon_min := SUBSTRING(coord_text FROM 9 FOR 2)::NUMERIC;
    lon_dir := SUBSTRING(coord_text FROM 11 FOR 1);
    
    -- Преобразование в десятичные градусы
    lat_deg := lat_deg + lat_min/60.0;
    IF lat_dir = 'S' THEN lat_deg := -lat_deg; END IF;
    
    lon_deg := lon_deg + lon_min/60.0;
    IF lon_dir = 'W' THEN lon_deg := -lon_deg; END IF;
    
    RETURN ST_SetSRID(ST_MakePoint(lon_deg, lat_deg), 4326);
EXCEPTION WHEN OTHERS THEN
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Функция для автоматического определения региона по координатам
CREATE OR REPLACE FUNCTION assign_region(coords GEOMETRY)
RETURNS VARCHAR AS $$
DECLARE
    region_name VARCHAR;
BEGIN
    SELECT name INTO region_name
    FROM russian_regions 
    WHERE ST_Within(coords, geom)
    LIMIT 1;
    
    RETURN region_name;
EXCEPTION WHEN OTHERS THEN
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Триггерная функция для автоматического обновления регионов
CREATE OR REPLACE FUNCTION update_flight_regions()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.takeoff_coords IS NOT NULL THEN
        NEW.region_takeoff := assign_region(NEW.takeoff_coords);
    END IF;
    
    IF NEW.landing_coords IS NOT NULL THEN
        NEW.region_landing := assign_region(NEW.landing_coords);
    END IF;
    
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Создаем триггер
CREATE TRIGGER trigger_update_flight_regions
    BEFORE INSERT OR UPDATE ON flight_records
    FOR EACH ROW
    EXECUTE FUNCTION update_flight_regions();
