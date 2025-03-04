# Alpine est une version légère de Linux, contraites
# de performance vu la patate qui sert de serveur
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

RUN apk add --update nodejs npm

WORKDIR /app

COPY . .
RUN mvn clean install

FROM eclipse-temurin:17-jre-alpine

COPY --from=build /app/backend/target/*.jar app.jar

EXPOSE 8181

ENTRYPOINT ["java", "-jar", "app.jar"]