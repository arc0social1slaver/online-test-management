# ---------- Build stage ----------
FROM node:22-alpine AS build

WORKDIR /app

# Copy package metadata first for better layer caching
COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# ---------- Runtime stage ----------
FROM nginx:1.27-alpine

# Angular production build
COPY --from=build /app/dist/online-test-management-frontend/browser /usr/share/nginx/html

# Nginx template. BACKEND_URL is substituted at container startup.
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 80