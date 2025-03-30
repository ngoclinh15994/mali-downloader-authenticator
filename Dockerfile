# Use the official maven/Java image to build the app
# with maven image
FROM jelastic/maven:3.9.5-openjdk-21 AS build
WORKDIR /app
COPY src /app/src
COPY pom.xml /app
RUN mvn clean package -Pproduction -DskipTests

# Start a new stage from scratch
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/authentication-0.0.1.jar /app/authentication-0.0.1.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/authentication-0.0.1.jar"]
