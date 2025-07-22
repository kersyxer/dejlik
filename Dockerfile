# 1) Build stage: тільки JDK + Maven Wrapper
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Копіюємо Wrapper і конфігурацію
COPY mvnw .
COPY .mvn .mvn

# Копіюємо код
COPY pom.xml .
COPY src src

# Даємо права і запускаємо Wrapper
RUN chmod +x mvnw \
 && ./mvnw clean package -DskipTests

# 2) Runtime stage: мінімальний JRE
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Копіюємо готовий JAR з build-стадії
COPY --from=builder /app/target/dejlik-1.0-SNAPSHOT.jar app.jar

# Порт, який Render задає в $PORT
EXPOSE 8080

# Команда запуску
CMD ["java","-Dserver.port=${PORT}","-jar","app.jar"]
