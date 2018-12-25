#!/bin/sh
docker run --rm --name logs-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=rootroot -d mysql:5.7
firewall-cmd --add-port=3306/tcp
docker run --rm --name logs-redis -p 6379:6379 -d redis redis-server --appendonly yes
firewall-cmd --add-port=6379/tcp
docker run --rm -v "$PWD":/home/gradle/project -u root -w /home/gradle/project gradle gradle clean build
docker build -t device-logs .
docker run --rm --name device-logs -p 8080:8080 --link logs-mysql:mysql --link logs-redis:redis -d device-logs 