FROM node:22-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk AS backend-build
WORKDIR /backend
COPY backend/main-service/ ./
COPY --from=frontend-build /frontend/dist/ src/main/resources/static/
RUN chmod +x ./gradlew && ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /backend/build/libs/main-service-*.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
