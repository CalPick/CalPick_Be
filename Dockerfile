# --- 1단계: 빌드 단계 ---
FROM gradle:8.4.0-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project

# 테스트 제외하고 빌드
RUN gradle build -x test

# --- 2단계: 실행 단계 ---
FROM eclipse-temurin:21-jdk
WORKDIR /app

# 빌드된 jar 복사
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# 앱 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

# Render 기본 포트
EXPOSE 8080
