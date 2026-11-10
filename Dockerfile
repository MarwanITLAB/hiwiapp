# 1. Schritt: Build mit Maven (Java 21 – passend zur pom.xml)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Erst nur die pom.xml kopieren -> Abhängigkeiten werden gecacht und nicht bei jeder Code-Änderung neu geladen
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# 2. Schritt: App ausführen (schlankes Image, nur Java-Laufzeit)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Nicht als root laufen (Sicherheit auf dem Server)
RUN addgroup -S hiwi && adduser -S hiwi -G hiwi
COPY --from=build /app/target/*.jar app.jar
USER hiwi
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
