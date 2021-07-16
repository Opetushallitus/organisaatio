FROM adoptopenjdk/openjdk11:alpine-slim
COPY build/libs/organisaatio-service.jar /root/
EXPOSE 8180
CMD ["java", "-jar", "/root/organisaatio-service.jar"]
