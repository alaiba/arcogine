# syntax=docker/dockerfile:1.7
FROM gradle:9-jdk25 AS build

WORKDIR /app
COPY java/ ./
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :sim-cli:bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine

RUN apk upgrade --no-cache && apk add --no-cache curl

COPY --from=build /app/sim-cli/build/libs/arcogine.jar /app/arcogine.jar
COPY examples/ /app/examples/

WORKDIR /app
EXPOSE 3000

ENTRYPOINT ["java", "-jar", "arcogine.jar"]
CMD ["serve", "--addr", "0.0.0.0:3000"]
