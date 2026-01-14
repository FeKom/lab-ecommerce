####
# Multi-stage build for Quarkus JVM mode
# Stage 1: Build
####
FROM eclipse-temurin:21-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

WORKDIR /build

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Verify the build
RUN ls -lah target/quarkus-app/ && \
    echo "Quarkus app built successfully!"

####
# Stage 2: Runtime
####
FROM registry.access.redhat.com/ubi9/openjdk-21:1.23

ENV LANGUAGE='en_US:en'

# Copy quarkus app
COPY --chown=185 --from=builder /build/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=builder /build/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=builder /build/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=builder /build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8081
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
