FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-alpine
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8000
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


