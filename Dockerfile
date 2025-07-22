# 1) Build stage: зібрати fat JAR через Maven
FROM maven:3.9.1-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Забираємо залежності й будуємо JAR
RUN mvn clean package -DskipTests

# 2) Runtime stage: запустити з мінімальним Java-runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
# Копіюємо тільки готовий JAR
COPY --from=builder /app/target/dejlik-1.0-SNAPSHOT.jar app.jar

# Expose порт (Render підставить $PORT в CMD)
EXPOSE 8080

# Стартова команда
CMD ["java","-Dserver.port=${PORT}","-jar","app.jar"]
