#!/usr/bin/env python3
import pandas as pd
import re
from sqlalchemy import create_engine
import logging
import os
import sys

# Настройка логирования
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class FlightDataParser:
    def __init__(self, db_connection):
        self.db_engine = create_engine(db_connection)
        
    def parse_shr_data(self, shr_text):
        """Парсинг SHR данных"""
        if pd.isna(shr_text) or not isinstance(shr_text, str):
            return {}
            
        try:
            parsed = {}
            
            # Извлекаем Flight ID (SID)
            sid_match = re.search(r'SID/(\d+)', shr_text)
            if sid_match:
                parsed['flight_id'] = sid_match.group(1)
            
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
                parsed['takeoff_coords'] = dep_coords_match.group(1)
            
            # Извлекаем дату полета (формат DDMMYY)
            dof_match = re.search(r'DOF/(\d+)', shr_text)
            if dof_match:
                parsed['flight_date'] = dof_match.group(1)
            
            # Извлекаем примечания
            rmk_match = re.search(r'RMK/([^SID]+)(?=SID/|$)', shr_text)
            if rmk_match:
                parsed['remarks'] = rmk_match.group(1).strip()
            
            return parsed
            
        except Exception as e:
            logger.error(f"Error parsing SHR: {e}")
            return {}
    
    def parse_dep_arr_data(self, text, data_type):
        """Парсинг DEP/ARR данных"""
        if pd.isna(text) or not isinstance(text, str):
            return {}
            
        try:
            parsed = {}
            lines = text.split('\n')
            
            for line in lines:
                line = line.strip()
                if line.startswith('-'):
                    # Убираем начальный '-' и разбиваем на ключ-значение
                    parts = line[1:].strip().split(' ', 1)
                    if len(parts) == 2:
                        key, value = parts
                        parsed[f"{data_type}_{key.lower()}"] = value.strip()
            
            return parsed
            
        except Exception as e:
            logger.error(f"Error parsing {data_type}: {e}")
            return {}
    
    def parse_excel_file(self, file_path):
        """Парсинг Excel файла"""
        logger.info(f"Начинаю парсинг файла: {file_path}")
        
        try:
            # Читаем Excel файл
            df = pd.read_excel(file_path, header=None)
            
            logger.info(f"Файл загружен. Найдено строк: {len(df)}")
            
            all_flights = []
            
            for index, row in df.iterrows():
                if index % 5000 == 0 and index > 0:
                    logger.info(f"Обработано {index} строк...")
                
                try:
                    # Создаем базовую запись
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
                        dep_parsed = self.parse_dep_arr_data(str(flight_data['raw_dep']), 'dep')
                        flight_data.update(dep_parsed)
                    
                    # Парсим ARR данные
                    if flight_data['raw_arr']:
                        arr_parsed = self.parse_dep_arr_data(str(flight_data['raw_arr']), 'arr')
                        flight_data.update(arr_parsed)
                    
                    all_flights.append(flight_data)
                    
                except Exception as e:
                    logger.error(f"Ошибка в строке {index}: {e}")
                    continue
            
            # Сохраняем в базу данных
            if all_flights:
                self.save_to_db(all_flights)
            
            logger.info(f"Парсинг завершен. Успешно обработано: {len(all_flights)} строк")
            
        except Exception as e:
            logger.error(f"Ошибка при чтении файла: {e}")
    
    def save_to_db(self, flights_data):
        """Сохранение данных в PostgreSQL"""
        try:
            df = pd.DataFrame(flights_data)
            
            # Сохраняем в базу
            df.to_sql(
                'parsed_flight_data', 
                self.db_engine, 
                if_exists='replace',  # Заменяем существующую таблицу
                index=False,
                method='multi'
            )
            
            logger.info(f"Данные сохранены в базу. Записей: {len(flights_data)}")
            
        except Exception as e:
            logger.error(f"Ошибка при сохранении в базу: {e}")

def main():
    # Настройки подключения к БД
    DB_CONNECTION = "postgresql://postgres:password@localhost:5432/drone_analytics"
    
    # Путь к Excel файлу
    EXCEL_FILE = "flight_data.xlsx"
    
    # Проверяем существование файла
    if not os.path.exists(EXCEL_FILE):
        logger.error(f"Файл {EXCEL_FILE} не найден!")
        return
    
    # Создаем парсер
    parser = FlightDataParser(DB_CONNECTION)
    
    # Запускаем парсинг
    parser.parse_excel_file(EXCEL_FILE)
    
    # Выводим итоговое сообщение
    logger.info("✅ Парсинг завершен! Проверь данные в PostgreSQL")

if __name__ == "__main__":
    main()
