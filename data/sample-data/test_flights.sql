-- Очистка таблицы перед добавлением тестовых данных
TRUNCATE TABLE flight_records RESTART IDENTITY;

-- Вставка тестовых данных на основе вашего примера
INSERT INTO flight_records (
    center_code,
    flight_date,
    takeoff_time,
    flight_number,
    aircraft_type,
    operator_name,
    operator_phone,
    remarks,
    takeoff_coords_text,
    takeoff_coords,
    raw_shr_data,
    raw_dep_data,
    raw_arr_data
) VALUES (
    'Санкт-Петербургский',
    '2024-01-25', -- DOF/250201 (25.02.2024)
    '2024-01-25 07:05:00', -- ATD 0705
    '7772187998', -- SID/7772187998
    'SHAR', -- TYP/SHAR
    'МАЛИНОВСКИЙ НИКИТА АЛЕКСАНДРОВИЧ', -- OPR/МАЛИНОВСКИЙ...
    '+79313215153', -- телефон
    'ОБОЛОЧКА 300 ДЛЯ ЗОНДИРОВАНИЯ АТМОСФЕРЫ', -- RMK/...
    '5957N02905E', -- DEP/5957N02905E
    parse_coordinates('5957N02905E'), -- автоматическое преобразование координат
    -- SHR данные в JSON
    '{
        "message_type": "SHR",
        "flight_id": "ZZZZ-ZZZZ0705-K0300M3000",
        "departure_coords": "5957N02905E",
        "date_of_flight": "250201",
        "operator": "МАЛИНОВСКИЙ НИКИТА АЛЕКСАНДРОВИЧ",
        "phone": "+79313215153",
        "aircraft_type": "SHAR",
        "remarks": "ОБОЛОЧКА 300 ДЛЯ ЗОНДИРОВАНИЯ АТМОСФЕРЫ",
        "session_id": "7772187998"
    }'::jsonb,
    -- DEP данные в JSON
    '{
        "message_type": "DEP",
        "title": "IDEP",
        "session_id": "7772187998",
        "additional_data": "250201",
        "actual_time_departure": "0705",
        "aerodrome_departure": "ZZZZ",
        "aerodrome_departure_coords": "5957N02905E",
        "planned_altitude_profile": "0"
    }'::jsonb,
    -- ARR данные (пока пустые)
    '{}'::jsonb
);

-- Добавим еще несколько тестовых записей для демонстрации
INSERT INTO flight_records (
    center_code,
    flight_date,
    takeoff_time,
    flight_number,
    aircraft_type,
    operator_name,
    takeoff_coords_text,
    takeoff_coords
) VALUES 
('Московский', '2024-01-26', '2024-01-26 09:30:00', '7772188000', 'UAV', 
 'ИВАНОВ ПЕТР СЕРГЕЕВИЧ', '5545N03737E', parse_coordinates('5545N03737E')),
 
('Новосибирский', '2024-01-27', '2024-01-27 14:20:00', '7772188001', 'UAV',
 'ПЕТРОВ АЛЕКСЕЙ ВИКТОРОВИЧ', '5502N08254E', parse_coordinates('5502N08254E'));
