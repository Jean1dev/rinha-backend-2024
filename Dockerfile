FROM gradle:8.6.0-jdk21-alpine AS builder

WORKDIR /usr/app/
ENV PORT 8080
ENV SPRING_DATASOURCE_URL jdbc:postgresql://localhost:5432/app
ENV SPRING_DATASOURCE_USERNAME jeanfernandes
ENV SPRING_DATASOURCE_PASSWORD 1234

COPY . .

RUN gradle build -x test

FROM eclipse-temurin:21.0.2_13-jre-alpine

COPY --from=builder /usr/app/build/libs/*.jar /opt/app/application.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

RUN ls /opt/app/
CMD java -jar /opt/app/application.jar