# Easy local setup for UI development

*How I Learned to Stop Worrying and Love the Bomb*

## Purpose

To get local UI development environment up and running with minimal hassle

Want:
* Minimal configuration
* Fast feedback loop (e.g. webpack hot reload)
* By-pass any CORS issues

## Prerequisites 

* node + npm
* docker
* docker-compose

## Approach

![deployment](http://www.plantuml.com/plantuml/png/JOr1RiKW34JtdC9YpmMwg7AFgWiALa41cnf8qjj_-0fIDcW6pvlPQhFIUaxAkiO2lQ8enxam8JMWtqZNmv_uKwpRmLRGItiyO507YbOkSVUWnnScBdaYI4SKfgdrvBW4fUOC6CydcSzxvFt2iAlt0tH0scDYqoC8_dMihUexkE1HDvCs9U0MK1x1LMI-lAq1_VVQci0w5k7hNwiDoVUSNW00)

[//]: # (image source: http://www.plantuml.com/plantuml/uml/JOr1RiKW34JtdC9YpmMwg7AFgWiALa41cnf8qjj_-0fIDcW6pvlPQhFIUaxAkiO2lQ8enxam8JMWtqZNmv_uKwpRmLRGItiyO507YbOkSVUWnnScBdaYI4SKfgdrvBW4fUOC6CydcSzxvFt2iAlt0tH0scDYqoC8_dMihUexkE1HDvCs9U0MK1x1LMI-lAq1_VVQci0w5k7hNwiDoVUSNW00)

* UI Access via nginx proxy
* henkilo-ui requests proxied to local webpack dev server (with hot reload support!)
* All other (+ some henkilo-ui) requests proxied to selected developement environment (see [nginx.conf](nginx.conf)) 

### Setup

Components are run in following ports
* nginx: 8080
* webpack: 3000

## Steps

1. Start webpack-dev-server `cd src/main/static && npm start`
2. Start local nginx with `cd nginx && docker-compose up`
3. Access CAS via nginx proxy to login http://localhost:8080/cas
4. Navigate either via menu or directly to http://localhost:8080/henkilo-ui

## Troubleshooting

### There is something wrong with docker

Nginx needs to access services in host machine. Some platforms (mac, win) has made this easy by adding
virtual domain name *host.docker.internal* which resolves to host address.

*host.docker.internal* can be replaced by IP address of the host or by some other clever trick.

### I want to connect to different development environment

Go through [nginx.conf](nginx.conf) and replace all references to development environment with the desired one.
Restart nginx. Delete cookies. Try again.
