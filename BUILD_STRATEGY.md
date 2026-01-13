# Estratégia de Build - E-commerce Microservices

Este documento detalha a estratégia de build otimizada para a plataforma de e-commerce, cobrindo build local, CI/CD, otimização de performance e melhores práticas.

## 📊 Visão Geral dos Builds

| Serviço | Tecnologia | Build Tool | Artefato | Estratégia |
|---------|------------|------------|----------|------------|
| **User Service** | TypeScript/Bun | Bun | Standalone | Multi-stage Docker |
| **Catalog Service** | Java/Spring Boot | Maven | JAR | Multi-stage Docker |
| **Search Service** | Java/Quarkus | Maven | JAR/Native | Multi-stage com opções |
| **Frontend** | Angular/TypeScript | NPM | Static Files | Multi-stage Nginx |

## 🚀 Estratégias de Build Otimizadas

### **1. Build Paralelo e Cache Inteligente**

#### **Script de Build Principal**
```bash
#!/bin/bash -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Configurações
BUILD_TYPE=${BUILD_TYPE:-"full"}
PARALLEL_BUILDS=${PARALLEL_BUILDS:-true}
SKIP_TESTS=${SKIP_TESTS:-false}
DOCKER_BUILD=${DOCKER_BUILD:-true}

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}"
    exit 1
}

warn() {
    echo -e "${YELLOW}[WARN] $1${NC}"
}

# Verificar dependências
check_dependencies() {
    log "Verificando dependências..."

    command -v docker >/dev/null 2>&1 || error "Docker não encontrado"
    command -v docker-compose >/dev/null 2>&1 || error "Docker Compose não encontrado"

    # Verificar se Docker daemon está rodando
    docker info >/dev/null 2>&1 || error "Docker daemon não está rodando"
}

# Build do User Service
build_user_service() {
    log "Construindo User Service..."

    cd user

    # Instalar dependências com cache
    if [ ! -d "node_modules" ] || [ ! -f ".bun/install-state.json" ]; then
        bun install --frozen-lockfile
    else
        log "Usando cache de dependências do Bun"
    fi

    # Executar testes se não pulados
    if [ "$SKIP_TESTS" != "true" ]; then
        log "Executando testes do User Service..."
        bun test || error "Testes do User Service falharam"
    fi

    # Build para produção
    bun run build || error "Build do User Service falhou"

    cd ..
}

# Build do Catalog Service
build_catalog_service() {
    log "Construindo Catalog Service..."

    cd catalog

    # Cache do Maven local
    MAVEN_OPTS="-Dmaven.repo.local=/tmp/.m2"

    # Build com Maven
    if [ "$SKIP_TESTS" != "true" ]; then
        ./mvnw clean package -DskipTests=false $MAVEN_OPTS || error "Build do Catalog Service falhou"
    else
        ./mvnw clean package -DskipTests=true $MAVEN_OPTS || error "Build do Catalog Service falhou"
    fi

    cd ..
}

# Build do Search Service
build_search_service() {
    log "Construindo Search Service..."

    cd search

    MAVEN_OPTS="-Dmaven.repo.local=/tmp/.m2"

    # Escolher tipo de build baseado na variável de ambiente
    case ${SEARCH_BUILD_TYPE:-jvm} in
        native)
            log "Build nativo do Quarkus (pode ser lento)"
            ./mvnw clean package -Pnative -DskipTests $MAVEN_OPTS || error "Build nativo do Search Service falhou"
            ;;
        jvm)
            ./mvnw clean package -DskipTests $MAVEN_OPTS || error "Build JVM do Search Service falhou"
            ;;
        *)
            error "Tipo de build inválido para Search Service: $SEARCH_BUILD_TYPE"
            ;;
    esac

    cd ..
}

# Build do Frontend
build_frontend() {
    log "Construindo Frontend..."

    cd frontend

    # Instalar dependências
    if [ ! -d "node_modules" ]; then
        npm ci
    else
        log "Usando cache de dependências do NPM"
    fi

    # Build para produção
    npm run build -- --configuration production || error "Build do Frontend falhou"

    cd ..
}

# Build das imagens Docker
build_docker_images() {
    if [ "$DOCKER_BUILD" != "true" ]; then
        log "Pulando build das imagens Docker..."
        return
    fi

    log "Construindo imagens Docker..."

    # Build paralelo das imagens
    if [ "$PARALLEL_BUILDS" = "true" ]; then
        log "Construindo imagens em paralelo..."

        # Build User Service
        TAG=${USER_TAG:-latest} docker compose build --parallel user-service &

        # Build Catalog Service
        TAG=${CATALOG_TAG:-latest} docker compose build --parallel catalog &

        # Build Search Service
        TAG=${SEARCH_TAG:-latest} docker compose build --parallel search &

        # Build Frontend
        TAG=${FRONTEND_TAG:-latest} docker compose build --parallel frontend &

        # Aguardar todos os builds
        wait

        log "Build paralelo concluído"
    else
        log "Construindo imagens sequencialmente..."

        TAG=${USER_TAG:-latest} docker compose build user-service
        TAG=${CATALOG_TAG:-latest} docker compose build catalog
        TAG=${SEARCH_TAG:-latest} docker compose build search
        TAG=${FRONTEND_TAG:-latest} docker compose build frontend
    fi
}

# Função principal
main() {
    log "🚀 Iniciando build da plataforma E-commerce"
    log "Tipo de build: $BUILD_TYPE"
    log "Build paralelo: $PARALLEL_BUILDS"
    log "Pular testes: $SKIP_TESTS"
    log "Build Docker: $DOCKER_BUILD"

    check_dependencies

    case $BUILD_TYPE in
        full)
            log "Executando build completo..."

            if [ "$PARALLEL_BUILDS" = "true" ]; then
                # Build dos serviços em paralelo
                build_user_service &
                build_catalog_service &
                build_search_service &
                build_frontend &

                # Aguardar conclusão
                wait
            else
                # Build sequencial
                build_user_service
                build_catalog_service
                build_search_service
                build_frontend
            fi

            build_docker_images
            ;;

        user-service)
            build_user_service
            if [ "$DOCKER_BUILD" = "true" ]; then
                TAG=${USER_TAG:-latest} docker compose build user-service
            fi
            ;;

        catalog)
            build_catalog_service
            if [ "$DOCKER_BUILD" = "true" ]; then
                TAG=${CATALOG_TAG:-latest} docker compose build catalog
            fi
            ;;

        search)
            build_search_service
            if [ "$DOCKER_BUILD" = "true" ]; then
                TAG=${SEARCH_TAG:-latest} docker compose build search
            fi
            ;;

        frontend)
            build_frontend
            if [ "$DOCKER_BUILD" = "true" ]; then
                TAG=${FRONTEND_TAG:-latest} docker compose build frontend
            fi
            ;;

        docker-only)
            build_docker_images
            ;;

        *)
            error "Tipo de build inválido: $BUILD_TYPE"
            ;;
    esac

    log "✅ Build concluído com sucesso!"
    log "Para executar: docker compose up -d"
}

# Execução
main "$@"
```

### **2. Dockerfiles Otimizados**

#### **User Service - Dockerfile Otimizado**
```dockerfile
# User Service - Multi-stage com Bun
FROM oven/bun:1 AS base
WORKDIR /app

# Instalar dependências
FROM base AS deps
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile --production

# Build da aplicação
FROM base AS builder
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN bun run build

# Imagem de produção
FROM oven/bun:dist AS runner
WORKDIR /app

# Criar usuário não-root
RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nodejs

# Copiar arquivos necessários
COPY --from=builder --chown=nodejs:nodejs /app/dist ./dist
COPY --from=builder --chown=nodejs:nodejs /app/package.json ./
COPY --from=deps --chown=nodejs:nodejs /app/node_modules ./node_modules

USER nodejs

EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:3000/health || exit 1

CMD ["bun", "run", "dist/index.js"]
```

#### **Catalog Service - Dockerfile Otimizado**
```dockerfile
# Catalog Service - Multi-stage com Maven
FROM maven:3.9.4-openjdk-21-slim AS builder

WORKDIR /app

# Cache de dependências Maven
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte e build
COPY src ./src
RUN ./mvnw clean package -DskipTests -Dmaven.test.skip=true

# Imagem de produção
FROM openjdk:21-jre-slim

WORKDIR /app

# Criar usuário não-root
RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 spring spring

# Copiar JAR
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

USER spring

EXPOSE 8080

# JVM tuning para containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### **Search Service - Dockerfile com Múltiplas Opções**
```dockerfile
# Build stage
FROM maven:3.9.4-openjdk-21-slim AS builder

WORKDIR /app

# Cache Maven
COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# JVM Runtime
FROM openjdk:21-jre-slim AS jvm-runtime

WORKDIR /app
COPY --from=builder /app/target/quarkus-app ./quarkus-app

EXPOSE 8081

CMD ["java", "-jar", "quarkus-app/quarkus-run.jar"]

# Native Runtime (opcional)
FROM registry.access.redhat.com/ubi8/ubi-minimal AS native-runtime

WORKDIR /app
COPY --from=builder /app/target/*-runner /app/application

EXPOSE 8081

CMD ["./application"]
```

#### **Frontend - Dockerfile Otimizado**
```dockerfile
# Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Copiar arquivos de dependência primeiro para cache
COPY package*.json ./
RUN npm ci --only=production

# Copiar código fonte
COPY . .

# Build para produção
RUN npm run build -- --configuration production

# Runtime stage
FROM nginx:alpine

# Copiar arquivos buildados
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Copiar configuração customizada do nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/health || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### **3. Estratégia de Cache e Layers**

#### **BuildKit e Cache Mounts**
```bash
# Habilitar BuildKit
export DOCKER_BUILDKIT=1

# Build com cache mounts
docker build \
  --target builder \
  --cache-from=user-service:builder \
  --tag user-service:builder \
  --build-arg BUILDKIT_INLINE_CACHE=1 \
  user/

# Cache para Maven
docker build \
  --cache-from=catalog:builder \
  --tag catalog:latest \
  --build-arg MAVEN_OPTS="-Dmaven.repo.local=/tmp/.m2" \
  catalog/
```

#### **Docker Compose com Cache**
```yaml
version: '3.8'

services:
  user-service:
    build:
      context: user/
      dockerfile: Dockerfile
      cache_from:
        - user-service:latest
      args:
        BUILDKIT_INLINE_CACHE: 1
    image: user-service:${TAG:-latest}

  catalog:
    build:
      context: catalog/
      dockerfile: Dockerfile
      cache_from:
        - catalog:latest
      args:
        MAVEN_OPTS: "-Dmaven.repo.local=/tmp/.m2"
    image: catalog:${TAG:-latest}
```

### **4. CI/CD Pipeline Otimizado**

#### **GitHub Actions - Build Estratégico**
```yaml
name: Build and Deploy

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
      redis:
        image: redis:7
      mongo:
        image: mongo:7

    steps:
    - uses: actions/checkout@v4

    - name: Setup Java
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: maven

    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: '20'
        cache: 'npm'

    - name: Setup Bun
      uses: oven-sh/setup-bun@v1

    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2

    - name: Cache NPM dependencies
      uses: actions/cache@v3
      with:
        path: ~/.npm
        key: ${{ runner.os }}-node-${{ hashFiles('**/package-lock.json') }}

    - name: Test User Service
      run: |
        cd user
        bun install --frozen-lockfile
        bun test

    - name: Test Catalog Service
      run: |
        cd catalog
        ./mvnw test -Dmaven.test.skip=false

    - name: Test Search Service
      run: |
        cd search
        ./mvnw test

    - name: Test Frontend
      run: |
        cd frontend
        npm ci
        npm run test:ci

  build:
    needs: test
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    strategy:
      matrix:
        service: [user-service, catalog, search, frontend]

    steps:
    - name: Checkout repository
      uses: actions/checkout@v4

    - name: Log in to Container Registry
      uses: docker/login-action@v3
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}

    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v5
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/${{ matrix.service }}
        tags: |
          type=ref,event=branch
          type=ref,event=pr
          type=sha,prefix={{branch}}-
          type=raw,value=latest,enable={{is_default_branch}}

    - name: Build and push Docker image
      uses: docker/build-push-action@v5
      with:
        context: ./${{ matrix.service }}
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}
        cache-from: type=gha
        cache-to: type=gha,mode=max

  deploy-staging:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    environment: staging

    steps:
    - name: Deploy to staging
      run: |
        echo "Deploying to staging environment"

  deploy-production:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: production

    steps:
    - name: Deploy to production
      run: |
        echo "Deploying to production environment"
```

### **5. Otimização de Performance de Build**

#### **Build Paralelo Inteligente**
```bash
#!/bin/bash

# Estratégia de build baseada em dependências
build_services() {
  local services=("user-service" "catalog" "search" "frontend")

  # Serviços sem dependências podem ser buildados em paralelo
  build_parallel "user-service" "frontend" &

  # Catalog depende de search (por causa dos eventos)
  wait
  build_parallel "search" "catalog" &

  wait
}

build_parallel() {
  local pids=()

  for service in "$@"; do
    build_service "$service" &
    pids+=($!)
  done

  # Aguardar todos terminarem
  for pid in "${pids[@]}"; do
    wait "$pid"
  done
}
```

#### **Build Incremental**
```bash
#!/bin/bash

# Build incremental baseado em mudanças
get_changed_services() {
  # Usar git diff para identificar serviços alterados
  git diff --name-only HEAD~1 | awk -F'/' '{print $1}' | sort | uniq
}

incremental_build() {
  local changed_services=$(get_changed_services)

  if [ -z "$changed_services" ]; then
    echo "Nenhuma mudança detectada, pulando build"
    exit 0
  fi

  echo "Serviços alterados: $changed_services"

  for service in $changed_services; do
    case $service in
      user|frontend)
        build_service "$service"
        ;;
      catalog|search)
        # Para serviços Java, verificar se pom.xml mudou
        if git diff --name-only HEAD~1 | grep -q "^$service/pom.xml"; then
          build_service "$service"
        else
          echo "Apenas código fonte mudou em $service, rebuild não necessário"
        fi
        ;;
    esac
  done
}
```

### **6. Estratégias de Deployment**

#### **Blue-Green Deployment**
```bash
#!/bin/bash

deploy_blue_green() {
  local service=$1
  local new_version=$2

  # Verificar saúde da versão atual
  if ! check_service_health "$service"; then
    echo "Serviço atual não está saudável"
    exit 1
  fi

  # Deploy da nova versão
  echo "Deploying $service:$new_version"
  kubectl set image deployment/$service $service=$service:$new_version

  # Aguardar rollout
  kubectl rollout status deployment/$service --timeout=300s

  # Verificar saúde da nova versão
  if check_service_health "$service"; then
    echo "✅ Deploy bem-sucedido, switching traffic"

    # Atualizar service para apontar para nova versão
    kubectl patch service $service -p '{"spec":{"selector":{"version":"'"$new_version"'"}}}'

    # Remover versão antiga
    kubectl delete deployment ${service}-old
  else
    echo "❌ Deploy falhou, fazendo rollback"
    kubectl rollout undo deployment/$service
    exit 1
  fi
}
```

#### **Canary Deployment**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: catalog-service
spec:
  selector:
    app: catalog-service
    version: stable  # Traffic vai para stable por padrão
  ports:
  - port: 80
    targetPort: 8080
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: catalog-service-stable
spec:
  replicas: 8  # 80% do traffic
  selector:
    matchLabels:
      app: catalog-service
      version: stable
  template:
    metadata:
      labels:
        app: catalog-service
        version: stable
    spec:
      containers:
      - name: catalog
        image: catalog:v1.0.0
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: catalog-service-canary
spec:
  replicas: 2  # 20% do traffic
  selector:
    matchLabels:
      app: catalog-service
      version: canary
  template:
    metadata:
      labels:
        app: catalog-service
        version: canary
    spec:
      containers:
      - name: catalog
        image: catalog:v1.1.0
```

### **7. Monitoramento e Métricas de Build**

#### **Build Metrics**
```bash
#!/bin/bash

# Coletar métricas de build
collect_build_metrics() {
  local service=$1
  local start_time=$2
  local end_time=$(date +%s)

  local build_time=$((end_time - start_time))
  local image_size=$(docker images $service --format "{{.Size}}" | head -n1)

  # Enviar métricas para monitoring
  curl -X POST http://monitoring:9091/metrics \
    -d "build_time{service=\"$service\"} $build_time" \
    -d "image_size{service=\"$service\"} $image_size"
}
```

#### **Health Checks Pós-Build**
```bash
#!/bin/bash

# Verificar saúde do serviço após deploy
check_service_health() {
  local service=$1
  local max_attempts=30
  local attempt=1

  while [ $attempt -le $max_attempts ]; do
    if curl -f -s "http://$service/health" > /dev/null; then
      echo "✅ $service está saudável"
      return 0
    fi

    echo "⏳ Aguardando $service ficar saudável (tentativa $attempt/$max_attempts)"
    sleep 10
    ((attempt++))
  done

  echo "❌ $service não ficou saudável após $max_attempts tentativas"
  return 1
}
```

### **8. Estratégia de Versionamento**

#### **Semantic Versioning Automático**
```bash
#!/bin/bash

# Gerar versão baseada em conventional commits
generate_version() {
  local latest_tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.0")
  local commits_since_tag=$(git rev-list ${latest_tag}..HEAD --count)

  # Analisar tipo dos commits
  local breaking_changes=$(git log ${latest_tag}..HEAD --oneline | grep -c "BREAKING CHANGE\|!:" || true)
  local features=$(git log ${latest_tag}..HEAD --oneline | grep -c "^feat:" || true)
  local fixes=$(git log ${latest_tag}..HEAD --oneline | grep -c "^fix:" || true)

  # Lógica de versionamento
  IFS='.' read -ra VERSION_PARTS <<< "${latest_tag#v}"

  local major=${VERSION_PARTS[0]}
  local minor=${VERSION_PARTS[1]}
  local patch=${VERSION_PARTS[2]}

  if [ $breaking_changes -gt 0 ]; then
    major=$((major + 1))
    minor=0
    patch=0
  elif [ $features -gt 0 ]; then
    minor=$((minor + 1))
    patch=0
  elif [ $fixes -gt 0 ]; then
    patch=$((patch + 1))
  else
    patch=$((patch + 1))  # Para outros tipos de commit
  fi

  echo "v$major.$minor.$patch"
}
```

### **9. Troubleshooting e Debugging**

#### **Build Debugging**
```bash
#!/bin/bash

# Debug build com verbose output
debug_build() {
  local service=$1

  echo "=== DEBUG BUILD: $service ==="

  # Verificar se diretório existe
  if [ ! -d "$service" ]; then
    echo "❌ Diretório $service não encontrado"
    return 1
  fi

  cd "$service"

  # Verificar arquivos essenciais
  case $service in
    user-service)
      check_file "package.json" "bun.lock"
      ;;
    catalog|search)
      check_file "pom.xml" "src/main/java"
      ;;
    frontend)
      check_file "package.json" "angular.json"
      ;;
  esac

  # Verificar Docker
  if command -v docker &> /dev/null; then
    echo "✅ Docker disponível"
  else
    echo "❌ Docker não encontrado"
  fi

  cd ..
}

check_file() {
  for file in "$@"; do
    if [ -f "$file" ]; then
      echo "✅ $file encontrado"
    else
      echo "❌ $file não encontrado"
    fi
  done
}
```

## 📊 Comparação de Estratégias de Build

| Estratégia | Velocidade | Cache | Complexidade | Recomendado |
|------------|------------|-------|-------------|-------------|
| **Sequencial** | ⚠️ Lento | ✅ Bom | ✅ Simples | Desenvolvimento |
| **Paralelo** | ✅ Rápido | ⚠️ Médio | ⚠️ Médio | CI/CD |
| **Incremental** | ✅ Muito rápido | ✅ Excelente | ⚠️ Complexo | Produção |
| **Monorepo** | ✅ Rápido | ✅ Excelente | ❌ Complexo | Grandes projetos |

## 🚀 Uso Recomendado

### **Desenvolvimento Local**
```bash
# Build completo
./build.sh full

# Build específico
./build.sh user-service

# Build sem testes
SKIP_TESTS=true ./build.sh full

# Build paralelo
PARALLEL_BUILDS=true ./build.sh full
```

### **CI/CD**
```bash
# Build otimizado para CI
export BUILD_TYPE=full
export SKIP_TESTS=false
export PARALLEL_BUILDS=true
export DOCKER_BUILD=true

./build.sh
```

### **Produção**
```bash
# Build com cache e otimizações
export BUILD_TYPE=incremental
export SEARCH_BUILD_TYPE=native
export PARALLEL_BUILDS=true

./build.sh
```

## 📈 Métricas de Performance

### **Tempos de Build Esperados**
- **User Service**: 30-60 segundos
- **Catalog Service**: 2-4 minutos
- **Search Service (JVM)**: 2-4 minutos
- **Search Service (Native)**: 5-10 minutos
- **Frontend**: 1-2 minutos
- **Build Completo**: 5-15 minutos (paralelo)

### **Tamanhos de Imagem**
- **User Service**: ~150MB
- **Catalog Service**: ~200MB
- **Search Service (JVM)**: ~180MB
- **Search Service (Native)**: ~80MB
- **Frontend**: ~50MB

Esta estratégia de build garante builds rápidos, confiáveis e otimizados para diferentes ambientes, desde desenvolvimento até produção.