FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew gradle.properties settings.gradle.kts build.gradle.kts ./
RUN chmod +x ./gradlew

COPY src/ src/

# Build con más verbose para debugging
RUN ./gradlew buildFatJar --no-daemon --info

EXPOSE 8080

# Comando mejorado con logging
CMD ["sh", "-c", "echo '🚀 Starting container...' && echo 'PORT='$PORT && java -jar build/libs/ktor-sample-all.jar"]