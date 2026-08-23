# syntax=docker/dockerfile:1.7
# Runtime-only image: packages the arcogine.jar already built by
# `./arcogine build` (Gradle produces dist/api/arcogine.jar). This
# Dockerfile does not compile Java — build context is dist/api/.
FROM eclipse-temurin:25-jre-alpine

RUN apk upgrade --no-cache && apk add --no-cache curl

COPY arcogine.jar /app/arcogine.jar

WORKDIR /app
EXPOSE 3000

ENTRYPOINT ["java", "-jar", "arcogine.jar"]
CMD ["serve", "--addr", "0.0.0.0:3000"]
