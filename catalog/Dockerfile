FROM eclipse-temurin:21-jdk-alpine AS builder

# Instalar Maven
RUN apk add --no-cache maven

WORKDIR /build

# Copiar pom.xml e baixar dependências (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copiar código fonte e buildar
COPY src ./src
RUN mvn clean package -DskipTests

# Verificar se o JAR foi criado
RUN ls -lah target/ && \
    echo "JAR criado com sucesso!"

# Stage 2: Runtime JRE otimizado
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criar usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar o JAR do builder
COPY --from=builder /build/target/*.jar app.jar

# Expor porta
EXPOSE 8080

# Configurações de JVM otimizadas
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]