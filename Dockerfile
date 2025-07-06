# Etapa de construcción
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8081

# Comando de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]
