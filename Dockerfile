# --- 1단계: 빌드 단계 ---
FROM gradle:8.4.0-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project

# 💡 핵심: bootJar 명령으로 fat jar 생성
RUN gradle bootJar -x test
RUN ls -l /home/gradle/project/build/libs/

# --- 2단계: 실행 단계 ---
FROM eclipse-temurin:21-jdk
WORKDIR /app

# build/libs 아래 생성된 bootJar 결과물을 app.jar로 복사
COPY --from=build /home/gradle/project/build/libs/calpick-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# fat jar 실행
ENTRYPOINT ["java", "-jar"]
CMD ["app.jar"]