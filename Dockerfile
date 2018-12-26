FROM java:8
LABEL vendor="ranhfun"
COPY build/libs/device-logs.jar device-logs.jar
EXPOSE 8080
ENV mysql.host mysql
ENV redis.host redis
ENTRYPOINT ["java","-jar","device-logs.jar"]