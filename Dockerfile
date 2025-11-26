# Multi-stage build: build with Maven, run with slim JRE
FROM maven:3.9.6-eclipse-temurin-24 as builder
WORKDIR /workspace
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
# copy sources
COPY src ./src
RUN mvn -B -DskipTests package --fail-never

# Runtime image
FROM eclipse-temurin:24-jre-jammy
WORKDIR /app
# copy jar produced by builder
RUN mkdir -p /app
COPY --from=builder /workspace/target/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
