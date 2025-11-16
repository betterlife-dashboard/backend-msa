# ============================
# 1단계: Build Stage
# ============================
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

# 의존성 캐싱
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies --no-daemon || true

# 실제 전체 소스 복사 후 빌드
COPY . .
RUN gradle clean build -x test --no-daemon

# ============================
# 2단계: Runtime Stage
# ============================
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# Builder에서 jar 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]