# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle gradle

RUN chmod +x gradlew \
    && ./gradlew dependencies --no-daemon --quiet

COPY src src

RUN ./gradlew bootJar -x test --no-daemon --quiet

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
