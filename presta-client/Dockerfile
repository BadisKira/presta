# Stage : Build Angular
FROM node:22-alpine3.21 AS build

# Dossier de travail
WORKDIR /app

# Copier package.json et package-lock.json pour installer les dépendances
COPY package*.json ./
RUN npm install

# Copier le reste du projet et builder Angular
COPY . .
RUN npm run build -- --configuration=production

# Stage : Nginx
FROM nginx:1.29.1-alpine-perl

RUN rm -rf /usr/share/nginx/html/*

COPY --from=build /app/dist/client-front-end/browser/ /usr/share/nginx/html

RUN chmod -R 755 /usr/share/nginx/html \
    && chown -R nginx:nginx /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exposer le port
EXPOSE 80

# Lancer Nginx en premier plan
CMD ["nginx", "-g", "daemon off;"]
