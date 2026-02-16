# ============ Stage 1: Build the application ============
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy the pom.xml file first to download dependencies
COPY pom.xml .

# Download dependencies (cached)
RUN mvn -B -q dependency:go-offline

# Copy the rest of the project
COPY src ./src

# Build the project (creates target/*.jar)
RUN mvn -B -q clean package -DskipTests


# ============ Stage 2: Run the application ============
FROM eclipse-temurin:17-jre

WORKDIR /app

# JAR build argument (detect any jar inside target)
ARG JAR_FILE=target/*.jar

# Copy the jar to app.jar
COPY --from=build /app/${JAR_FILE} app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
