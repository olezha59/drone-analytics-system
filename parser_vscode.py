#!/usr/bin/env python3
import json
import pandas as pd
import re
from sqlalchemy import create_engine, text
import logging
import os
import sys
from datetime import datetime

# Настройка логирования
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class FlightDataParser:
    def __init__(self, db_connection):
        try:
            self.db_engine = create_engine(db_connection)
            # Проверяем подключение
            with self.db_engine.connect() as conn:
                conn.execute(text("SELECT 1"))
            logger.info("✅ Подключение к БД установлено")
        except Exception as e:
            logger.error(f"❌ Ошибка подключения к БД: {e}")
            raise
    
    def create_table(self):
        """Создает таблицу в формате старой таблицы flight_records"""
        try:
            with self.db_engine.connect() as conn:
                # Удаляем старую таблицу если есть
                conn.execute(text("DROP TABLE IF EXISTS flight_records CASCADE"))
                
                # Создаем таблицу с правильной структурой
                conn.execute(text("""
                    CREATE TABLE flight_records (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        center_code VARCHAR(500) NOT NULL,
                        flight_date DATE,
                        takeoff_time TIMESTAMP,
                        landing_time TIMESTAMP,
                        flight_duration INTERVAL,
                        takeoff_coords GEOMETRY(Point,4326),
                        landing_coords GEOMETRY(Point,4326),
                        takeoff_coords_text VARCHAR(500),
                        landing_coords_text VARCHAR(500),
                        region_takeoff VARCHAR(500),
                        region_landing VARCHAR(500),
                        flight_number VARCHAR(500),
                        aircraft_type VARCHAR(500),
                        operator_name TEXT,
                        operator_phone VARCHAR(50),
                        remarks TEXT,
                        raw_shr_data JSONB,
                        raw_dep_data JSONB,
                        raw_arr_data JSONB,
                        created_at TIMESTAMP DEFAULT NOW(),
                        updated_at TIMESTAMP DEFAULT NOW(),
                        flight_duration_minutes INTEGER,
                        row_number INTEGER
                    )
                """))
                
                # Создаем индексы как в старой таблице
                conn.execute(text("""
                    CREATE INDEX idx_parsed_center ON flight_records (center_code);
                    CREATE INDEX idx_parsed_date ON flight_records (flight_date);
                    CREATE INDEX idx_parsed_number ON flight_records (flight_number);
                    CREATE INDEX idx_parsed_operator ON flight_records (operator_name);
                    CREATE INDEX idx_parsed_type ON flight_records (aircraft_type);
                    CREATE INDEX idx_parsed_region_takeoff ON flight_records (region_takeoff);
                """))
                
                conn.commit()
            logger.info("✅ Таблица создана в формате старой таблицы")
        except Exception as e:
            logger.error(f"❌ Ошибка создания таблицы: {e}")
            raise
    
    def convert_date_format(self, date_text):
        """Преобразует текст в DATE"""
        if pd.isna(date_text) or not isinstance(date_text, str):
            return None
        try:
            # Формат DDMMYY -> YYYY-MM-DD
            if re.match(r'^\d{6}$', date_text):
                day, month, year = date_text[:2], date_text[2:4], "20" + date_text[4:]
                return f"{year}-{month}-{day}"
            # Если уже в формате YYYY-MM-DD
            elif re.match(r'^\d{4}-\d{2}-\d{2}$', date_text):
                return date_text
        except Exception as e:
            logger.error(f"Ошибка преобразования даты {date_text}: {e}")
        return None
    
    def convert_to_geometry(self, coords_text):
        """Преобразует координаты в GEOMETRY Point"""
        if pd.isna(coords_text) or not isinstance(coords_text, str):
            return None
        try:
            # Пример: "5957N02905E" -> POINT(29.0833 59.95)
            if re.match(r'^\d{4}[NS]\d{5}[EW]$', coords_text):
                # Широта: 5957N -> 59°57'
                lat_deg = int(coords_text[0:2])
                lat_min = int(coords_text[2:4])
                lat_dir = coords_text[4]
                
                # Долгота: 02905E -> 29°05'
                lon_deg = int(coords_text[5:8])
                lon_min = int(coords_text[8:10])
                lon_dir = coords_text[10]
                
                # Преобразуем в десятичные градусы
                lat = lat_deg + lat_min / 60.0
                lon = lon_deg + lon_min / 60.0
                
                # Учитываем направление
                if lat_dir == 'S': lat = -lat
                if lon_dir == 'W': lon = -lon
                
                return f"POINT({lon} {lat})"
        except Exception as e:
            logger.error(f"Ошибка преобразования координат {coords_text}: {e}")
        return None
    
    def convert_time_format(self, time_text):
        """Преобразует время в TIMESTAMP"""
        if pd.isna(time_text) or not isinstance(time_text, str):
            return None
        try:
            # Формат HHMM -> 2024-01-01 HH:MM:00 (дата фиктивная)
            if re.match(r'^\d{4}$', time_text):
                hours, minutes = time_text[:2], time_text[2:4]
                return f"2024-01-01 {hours}:{minutes}:00"
            # Если уже в формате HH:MM:SS
            elif re.match(r'^\d{2}:\d{2}:\d{2}$', time_text):
                return f"2024-01-01 {time_text}"
        except Exception as e:
            logger.error(f"Ошибка преобразования времени {time_text}: {e}")
        return None
    
    def calculate_flight_duration(self, takeoff_time, landing_time):
        """Вычисляет продолжительность полета в минутах"""
        if not takeoff_time or not landing_time:
            return None
        
        try:
            # Извлекаем время из timestamp
            takeoff = datetime.strptime(takeoff_time.split(' ')[1], '%H:%M:%S')
            landing = datetime.strptime(landing_time.split(' ')[1], '%H:%M:%S')
            
            # Если время посадки меньше времени вылета, значит посадка на следующий день
            if landing < takeoff:
                landing = landing.replace(day=landing.day + 1)
            
            duration = landing - takeoff
            return int(duration.total_seconds() / 60)  # в минутах
            
        except Exception as e:
            logger.error(f"Ошибка вычисления длительности полета: {e}")
            return None
    
    def parse_shr_data(self, shr_text):
        """Парсинг SHR данных"""
        if pd.isna(shr_text) or not isinstance(shr_text, str):
            return {}
            
        try:
            parsed = {}
            
            # Извлекаем Flight ID (SID)
            sid_match = re.search(r'SID/(\d+)', shr_text)
            if sid_match:
                parsed['flight_number'] = sid_match.group(1)
            
            # Извлекаем оператора
            opr_match = re.search(r'OPR/([^+]+?)(?=\s*\+\d|$)', shr_text)
            if opr_match:
                parsed['operator_name'] = opr_match.group(1).strip()
            
            # Извлекаем телефон
            phone_match = re.search(r'(\+7\d{10})', shr_text)
            if phone_match:
                parsed['operator_phone'] = phone_match.group(1)
            
            # Извлекаем тип воздушного судна
            typ_match = re.search(r'TYP/([A-Z]+)', shr_text)
            if typ_match:
                parsed['aircraft_type'] = typ_match.group(1)
            
            # Извлекаем координаты вылета
            dep_coords_match = re.search(r'DEP/([A-Z0-9]+)', shr_text)
            if dep_coords_match:
                coords = dep_coords_match.group(1)
                parsed['takeoff_coords_text'] = coords
                parsed['takeoff_coords'] = self.convert_to_geometry(coords)
            
            # Извлекаем дату полета (формат DDMMYY)
            dof_match = re.search(r'DOF/(\d+)', shr_text)
            if dof_match:
                date_str = dof_match.group(1)
                parsed['flight_date'] = self.convert_date_format(date_str)
            
            # Извлекаем примечания
            rmk_match = re.search(r'RMK/([^SID]+)(?=SID/|$)', shr_text)
            if rmk_match:
                parsed['remarks'] = rmk_match.group(1).strip()
            
            # Извлекаем время вылета из формата -ZZZZ0705
            takeoff_time_match = re.search(r'-([A-Z]{4})(\d{4})', shr_text)
            if takeoff_time_match:
                time_str = takeoff_time_match.group(2)  # 0705
                parsed['takeoff_time'] = self.convert_time_format(time_str)
            
            return parsed
            
        except Exception as e:
            logger.error(f"Ошибка парсинга SHR: {e}")
            return {}
    
    def parse_dep_data(self, dep_text):
        """Парсинг DEP данных"""
        if pd.isna(dep_text) or not isinstance(dep_text, str):
            return {}
            
        try:
            parsed = {}
            lines = dep_text.split('\n')
            
            for line in lines:
                line = line.strip()
                if line.startswith('-'):
                    parts = line[1:].strip().split(' ', 1)
                    if len(parts) == 2:
                        key, value = parts
                        key = key.lower()
                        
                        if key == 'atd':  # Actual Time of Departure
                            parsed['takeoff_time'] = self.convert_time_format(value)
                        elif key == 'adeptz':  # Coordinates
                            parsed['takeoff_coords_text'] = value
                            parsed['takeoff_coords'] = self.convert_to_geometry(value)
            
            return parsed
            
        except Exception as e:
            logger.error(f"Ошибка парсинга DEP: {e}")
            return {}
    
    def parse_arr_data(self, arr_text):
        """Парсинг ARR данных"""
        if pd.isna(arr_text) or not isinstance(arr_text, str):
            return {}
            
        try:
            parsed = {}
            lines = arr_text.split('\n')
            
            for line in lines:
                line = line.strip()
                if line.startswith('-'):
                    parts = line[1:].strip().split(' ', 1)
                    if len(parts) == 2:
                        key, value = parts
                        key = key.lower()
                        
                        if key == 'ata':  # Actual Time of Arrival
                            parsed['landing_time'] = self.convert_time_format(value)
                        elif key == 'adarrz':  # Coordinates
                            parsed['landing_coords_text'] = value
                            parsed['landing_coords'] = self.convert_to_geometry(value)
            
            return parsed
            
        except Exception as e:
            logger.error(f"Ошибка парсинга ARR: {e}")
            return {}
    
    def determine_region(self, coords):
        """Определение региона по координатам"""
        if not coords:
            return None
            
        try:
            # Простая логика определения региона
            if 'POINT(37' in coords and '55' in coords:
                return "Центральный федеральный округ"
            elif 'POINT(29' in coords and '59' in coords:
                return "Северо-Западный федеральный округ"
            elif 'POINT(43' in coords and '44' in coords:
                return "Южный федеральный округ"
            
            return "Не определен"
            
        except Exception as e:
            logger.error(f"Ошибка определения региона: {e}")
            return "Ошибка определения"
    
    def parse_excel_file(self, file_path):
        """Парсинг Excel файла"""
        logger.info(f"Начинаю парсинг файла: {file_path}")
    
        try:
            # Читаем Excel файл
            df = pd.read_excel(file_path, header=None)
            logger.info(f"✅ Файл загружен. Найдено строк: {len(df)}")
        
            all_flights = []
            skipped_empty = 0
        
            for index, row in df.iterrows():
                if index % 5000 == 0 and index > 0:
                    logger.info(f"Обработано {index} строк...")
            
                try:
                    # Базовые данные
                    flight_data = {
                        'row_number': index + 1,
                        'center_code': row[0] if len(row) > 0 and pd.notna(row[0]) else None,
                        'raw_shr': row[1] if len(row) > 1 and pd.notna(row[1]) else None,
                        'raw_dep': row[2] if len(row) > 2 and pd.notna(row[2]) else None,
                        'raw_arr': row[3] if len(row) > 3 and pd.notna(row[3]) else None
                    }
                
                    # Парсим SHR данные
                    if flight_data['raw_shr']:
                        shr_parsed = self.parse_shr_data(str(flight_data['raw_shr']))
                        flight_data.update(shr_parsed)
                
                    # Парсим DEP данные
                    if flight_data['raw_dep']:
                        dep_parsed = self.parse_dep_data(str(flight_data['raw_dep']))
                        flight_data.update(dep_parsed)
                
                    # Парсим ARR данные
                    if flight_data['raw_arr']:
                        arr_parsed = self.parse_arr_data(str(flight_data['raw_arr']))
                        flight_data.update(arr_parsed)
                
                    # Вычисляем продолжительность полета
                    flight_duration = self.calculate_flight_duration(
                        flight_data.get('takeoff_time'),
                        flight_data.get('landing_time')
                    )
                    flight_data['flight_duration_minutes'] = flight_duration
                
                    # Определяем регионы
                    flight_data['region_takeoff'] = self.determine_region(flight_data.get('takeoff_coords'))
                    flight_data['region_landing'] = self.determine_region(flight_data.get('landing_coords'))
                
                    # Создаем JSON данные как строки
                    flight_data['raw_shr_data'] = json.dumps({
                        'original_text': flight_data.get('raw_shr'),
                        'flight_number': flight_data.get('flight_number'),
                        'operator_name': flight_data.get('operator_name'),
                        'aircraft_type': flight_data.get('aircraft_type')
                    }, ensure_ascii=False)
                
                    flight_data['raw_dep_data'] = json.dumps({
                        'original_text': flight_data.get('raw_dep')
                    }, ensure_ascii=False)
                
                    flight_data['raw_arr_data'] = json.dumps({
                        'original_text': flight_data.get('raw_arr')
                    }, ensure_ascii=False)
                
                    # Удаляем временные поля
                    for field in ['raw_shr', 'raw_dep', 'raw_arr']:
                        flight_data.pop(field, None)
                
                    # Проверяем что есть хотя бы некоторые данные
                    has_data = any([
                        flight_data.get('center_code'),
                        flight_data.get('flight_number'), 
                        flight_data.get('operator_name'),
                        flight_data.get('aircraft_type')
                    ])
                
                    if has_data:
                        all_flights.append(flight_data)
                    else:
                        skipped_empty += 1
                        if skipped_empty <= 5:  # Логируем только первые 5 пустых строк
                            logger.debug(f"Пропущена пустая строка {index}")
                
                except Exception as e:
                    logger.error(f"Ошибка в строке {index}: {e}")
                    continue
        
            logger.info(f"✅ Обработка завершена. Успешно: {len(all_flights)} строк, Пропущено пустых: {skipped_empty}")
        
            # Сохраняем в базу данных
            if all_flights:
                self.save_to_db(all_flights)
            else:
                logger.warning("⚠️ Нет данных для сохранения")
        
        except Exception as e:
            logger.error(f"❌ Ошибка при чтении файла: {e}")
            return False
    
    def save_to_db(self, flights_data):
        """Сохранение данных в PostgreSQL с правильной обработкой JSON"""
        try:
            with self.db_engine.connect() as conn:
                for flight in flights_data:
                    # Преобразуем dict в JSON строку
                    raw_shr_json = json.dumps(flight.get('raw_shr_data', {}), ensure_ascii=False)
                    raw_dep_json = json.dumps(flight.get('raw_dep_data', {}), ensure_ascii=False) 
                    raw_arr_json = json.dumps(flight.get('raw_arr_data', {}), ensure_ascii=False)
                
                    conn.execute(text("""
                        INSERT INTO flight_records (
                            center_code, flight_date, takeoff_time, landing_time,
                            takeoff_coords, landing_coords, takeoff_coords_text, landing_coords_text,
                            region_takeoff, region_landing, flight_number, aircraft_type,
                            operator_name, operator_phone, remarks, raw_shr_data,
                            raw_dep_data, raw_arr_data, flight_duration_minutes, row_number
                        ) VALUES (
                            :center_code, :flight_date, :takeoff_time, :landing_time,
                            ST_GeomFromText(:takeoff_coords, 4326), ST_GeomFromText(:landing_coords, 4326),
                            :takeoff_coords_text, :landing_coords_text, :region_takeoff, :region_landing,
                            :flight_number, :aircraft_type, :operator_name, :operator_phone, :remarks,
                            :raw_shr_data, :raw_dep_data, :raw_arr_data, :flight_duration_minutes, :row_number
                        )
                    """), {
                        'center_code': flight.get('center_code'),
                        'flight_date': flight.get('flight_date'),
                        'takeoff_time': flight.get('takeoff_time'),
                        'landing_time': flight.get('landing_time'),
                        'takeoff_coords': flight.get('takeoff_coords'),
                        'landing_coords': flight.get('landing_coords'),
                        'takeoff_coords_text': flight.get('takeoff_coords_text'),
                        'landing_coords_text': flight.get('landing_coords_text'),
                        'region_takeoff': flight.get('region_takeoff'),
                        'region_landing': flight.get('region_landing'),
                        'flight_number': flight.get('flight_number'),
                        'aircraft_type': flight.get('aircraft_type'),
                        'operator_name': flight.get('operator_name'),
                        'operator_phone': flight.get('operator_phone'),
                        'remarks': flight.get('remarks'),
                        'raw_shr_data': raw_shr_json,  # Теперь это JSON строка
                        'raw_dep_data': raw_dep_json,  # Теперь это JSON строка
                        'raw_arr_data': raw_arr_json,  # Теперь это JSON строка
                        'flight_duration_minutes': flight.get('flight_duration_minutes'),
                        'row_number': flight.get('row_number')
                    })
            
                conn.commit()
        
            logger.info(f"✅ Данные сохранены в базу. Записей: {len(flights_data)}")
        
            # Проверяем что данные действительно сохранились
            with self.db_engine.connect() as conn:
                count = conn.execute(text("SELECT COUNT(*) FROM flight_records")).scalar()
                logger.info(f"✅ Проверка БД: в таблице {count} записей")
            
        except Exception as e:
            logger.error(f"❌ Ошибка при сохранении в базу: {e}")

def main():
    print("🚀 Запуск исправленного парсера (старый формат таблицы)")
    print("=" * 60)
    
    # Настройки подключения к БД
    DB_CONNECTION = "postgresql://postgres:password123@localhost:5432/drone_analytics"
    
    # Путь к Excel файлу
    EXCEL_FILE = "data-parser/flight_data.xlsx"
    
    # Проверяем существование файла
    if not os.path.exists(EXCEL_FILE):
        logger.error(f"❌ Файл {EXCEL_FILE} не найден!")
        print("📁 Помести файл flight_data.xlsx в папку data-parser/")
        return
    
    try:
        # Создаем парсер
        parser = FlightDataParser(DB_CONNECTION)
        
        # Создаем таблицу
        parser.create_table()
        
        # Запускаем парсинг
        parser.parse_excel_file(EXCEL_FILE)
        
        print("🎉 Парсинг завершен!")
        print("📊 Проверь данные командой: psql -h localhost -U postgres drone_analytics -c \"SELECT * FROM flight_records LIMIT 3;\"")
        
    except Exception as e:
        logger.error(f"❌ Критическая ошибка: {e}")
        print("💡 Проверь что PostgreSQL запущен: docker compose up -d postgres")

if __name__ == "__main__":
    main()
