FROM gradle:8.6.0-jdk21-alpine AS builder

WORKDIR /usr/app/
ENV PORT 8080
ENV SPRING_DATASOURCE_URL r2dbc:postgresql://jeanfernandes:1234@localhost:5432/app

COPY . .

RUN gradle build -x test

FROM eclipse-temurin:21.0.2_13-jre-alpine

COPY --from=builder /usr/app/build/libs/*.jar /opt/app/application.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

RUN ls /opt/app/
ENV JAVA_OPTS="-Xmx100m"

ENTRYPOINT ["java", "-Xms50m", "-Xmx100m", "-XX:+UseSerialGC","-jar", "/opt/app/application.jar"]
#CMD java -jar /opt/app/application.jar