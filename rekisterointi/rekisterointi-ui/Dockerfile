FROM node:18-alpine

WORKDIR /var/app

COPY . .

RUN npm install --global serve
RUN npm ci
RUN npm run build

EXPOSE 8080
CMD [ "serve", "-p", "8080", "-n", "dist/" ]
