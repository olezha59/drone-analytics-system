#!/bin/bash

echo "🚀 Настройка парсера flight данных..."
echo "=========================================="

# Проверка что мы в правильной папке
if [ ! -f "parser.py" ]; then
    echo "❌ Ошибка: Запусти скрипт из папки data-parser!"
    exit 1
fi

# Проверка установки Python
if ! command -v python3 &> /dev/null; then
    echo "📦 Устанавливаем Python3..."
    sudo apt update
    sudo apt install python3 python3-pip -y
fi

# Проверка установки python3-venv
if ! python3 -c "import venv" &> /dev/null; then
    echo "📦 Устанавливаем python3-venv..."
    sudo apt install python3.12-venv python3-full -y
fi

# Создание виртуального окружения
if [ ! -d "venv" ]; then
    echo "🐍 Создаем виртуальное окружение..."
    python3 -m venv venv
    if [ $? -ne 0 ]; then
        echo "❌ Ошибка при создании venv. Пробуем альтернативный способ..."
        sudo apt install python3-venv -y
        python3 -m venv venv
    fi
fi

# Активация виртуального окружения
echo "🔧 Активируем виртуальное окружение..."
source venv/bin/activate

# Установка/обновление pip
echo "📦 Обновляем pip..."
pip install --upgrade pip

# Установка зависимостей
echo "📦 Устанавливаем зависимости..."
pip install pandas openpyxl sqlalchemy psycopg2-binary

# Создание requirements.txt
echo "📄 Создаем requirements.txt..."
pip freeze > requirements.txt

# Проверка установки
echo "✅ Проверяем установку..."
python3 -c "
try:
    import pandas as pd
    import sqlalchemy
    print('✅ Все пакеты установлены успешно!')
except ImportError as e:
    print(f'❌ Ошибка: {e}')
"

# Создание инструкции
echo ""
echo "🎉 Настройка завершена!"
echo "=========================================="
echo "Для работы выполни:"
echo "1. source venv/bin/activate    - активировать окружение"
echo "2. python parser.py            - запустить парсер"
echo ""
echo "📁 Помести свой Excel файл в эту папку и назови его 'flight_data.xlsx'"
echo "🔌 Убедись что PostgreSQL запущен: docker compose up -d postgres"
echo ""
echo "Для выхода из окружения: deactivate"
