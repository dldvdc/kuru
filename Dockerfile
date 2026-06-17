# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk-noble AS build

# VPS ~2 Go : limiter Gradle + pas de daemon dans le conteneur de build
ENV GRADLE_OPTS="-Xmx768m -XX:+UseParallelGC -Dorg.gradle.daemon=false -Dorg.gradle.parallel=false"

WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle gradle

RUN chmod +x gradlew \
    && ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew bootJar -x test --no-daemon

# ─────────────────────────────────────────────────────────────────────────────

FROM eclipse-temurin:25-jre-noble AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN groupadd --system kuru \
    && useradd --system --gid kuru --home-dir /app --shell /usr/sbin/nologin kuru \
    && mkdir -p /app/data \
    && chown -R kuru:kuru /app

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

USER kuru

ENV KURU_SQLITE_FILE=/app/data/kuru.db \
    KURU_FFPROBE_PATH=ffprobe \
    PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "/app/app.jar"]
