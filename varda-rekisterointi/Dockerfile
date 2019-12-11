# Not in any use yet

FROM openjdk:13-alpine AS MAVEN_BUILDER
COPY pom.xml /tmp/
COPY .mvn /tmp/.mvn
COPY mvnw /tmp/
COPY src /tmp/src/
COPY .git /tmp/.git/

WORKDIR /tmp/
RUN apk add --update nodejs npm
RUN ./mvnw clean install


FROM openjdk:13-alpine
COPY --from=MAVEN_BUILDER /tmp/target/varda-rekisteroint*.jar /varda-rekisterointi.jar

ENTRYPOINT ["java", "-jar", "/varda-rekisterointi.jar"]