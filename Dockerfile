# Gradnja i pokretanje za Render (Docker). Java 17 kao u pom.xml.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN chmod +x mvnw && ./mvnw -q clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render free ima 512 MB RAM-a: heap do 70 % memorije kontejnera i jednostavan GC.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"

# Port dolazi iz env varijable PORT (Render je postavlja), zadano 8080.
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
