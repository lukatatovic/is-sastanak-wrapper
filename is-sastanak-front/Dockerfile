 FROM node:20-alpine AS build

 WORKDIR /app

 COPY package*.json ./
 RUN npm install

 COPY . . 
 
 ARG VITE_AUTH_SERVICE_URL=http://localhost:8081
 ARG VITE_MEETINGS_SERVICE_URL=http://localhost:8082

 ENV VITE_AUTH_SERVICE_URL=$VITE_AUTH_SERVICE_URL
 ENV VITE_MEETINGS_SERVICE_URL=$VITE_MEETINGS_SERVICE_URL

 RUN npm run build

 FROM nginx:alpine

 COPY --from=build /app/dist /usr/share/nginx/html 
 COPY nginx.conf /etc/nginx/conf.d/default.conf

 EXPOSE 80

 CMD ["nginx", "-g", "daemon off;"]
