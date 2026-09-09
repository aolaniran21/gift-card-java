FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY shared/pom.xml ./shared/pom.xml
COPY auth/pom.xml ./auth/pom.xml
COPY orders/pom.xml ./orders/pom.xml
COPY app/pom.xml ./app/pom.xml
COPY shared/src ./shared/src
COPY auth/src ./auth/src
COPY orders/src ./orders/src
COPY app/src ./app/src
RUN mvn -q -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends wget && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/app/target/app-1.0.0.jar /app/app.jar
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=10s --start-period=30s --retries=10 CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-jar","/app/app.jar"]
