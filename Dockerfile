# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/pokepedia-0.0.1-SNAPSHOT.jar app.jar

EXPOSE ${SERVER_PORT:-5000}
ENTRYPOINT ["java", "-jar", "app.jar"]
