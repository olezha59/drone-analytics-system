# Build stage
FROM node:18-alpine as build

# Устанавливаем зависимости для сборки
RUN apk add --no-cache python3 make g++

WORKDIR /app

# Копируем файлы зависимостей для лучшего кэширования
COPY package*.json ./
COPY tsconfig.json ./

# Устанавливаем зависимости
RUN npm ci --silent

# Копируем исходный код
COPY public/ ./public/
COPY src/ ./src/

# Собираем приложение
RUN npm run build

# Production stage
FROM nginx:1.23-alpine

# Удаляем дефолтную конфигурацию nginx
RUN rm -rf /etc/nginx/conf.d/default.conf

# Копируем нашу конфигурацию nginx
COPY nginx.conf /etc/nginx/conf.d

# Копируем собранное приложение из стадии build
COPY --from=build /app/build /usr/share/nginx/html

# Создаем non-root пользователя для безопасности
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nextjs -u 1001
RUN chown -R nextjs:nodejs /usr/share/nginx/html

# Переключаемся на non-root пользователя
USER nextjs

# Открываем порт 80
EXPOSE 80

# Запускаем nginx
CMD ["nginx", "-g", "daemon off;"]
