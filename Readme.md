# E-commerce Microservices Platform

Uma plataforma de e-commerce distribuída composta por 4 serviços principais: **User Service** (autenticação/autorização), **Catalog Service** (gerenciamento de produtos), **Search Service** (buscas performáticas) e **Frontend** (interface Angular).

## 📋 Visão Geral da Arquitetura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   User Service  │    │ Catalog Service │
│   (Angular)     │    │   (Bun/TypeScript)│    │   (Spring Boot) │
│                 │    │                 │    │                 │
│  • Interface    │◄──►│  • Auth/Authz   │◄──►│  • Produtos      │
│  • SPA          │    │  • PostgreSQL   │    │  • MongoDB       │
│  • PWA Ready    │    │  • Redis Cache  │    │  • Redis Cache   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 ▼
                    ┌─────────────────┐    ┌─────────────────┐
                    │ Search Service  │    │   Message Bus   │
                    │   (Quarkus)     │    │     Kafka       │
                    │                 │    │                 │
                    │  • Buscas       │◄──►│  • Eventos      │
                    │  • MariaDB      │    │  • CDC          │
                    │  • JOOQ         │    │  • Sincronização │
                    └─────────────────┘    └─────────────────┘
```

## 🚀 DevOps - Infraestrutura e Deploy

### **Containerização**

#### **Dockerfiles Analisados**
- **User Service**: TypeScript/Bun - `multi-stage build` necessário
- **Catalog Service**: Java/Spring Boot - `multi-stage` com Maven
- **Search Service**: Java/Quarkus - múltiplas opções (JVM, Native, Micro)
- **Frontend**: Angular/Node.js - `nginx` para produção

#### **Melhorias Necessárias**
```dockerfile
# User Service - Otimizar camadas
FROM oven/bun:1 AS builder
WORKDIR /app
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile
COPY . .
RUN bun run build

FROM oven/bun:dist AS runtime
WORKDIR /app
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/package.json ./
EXPOSE 3000
CMD ["bun", "run", "dist/index.js"]
```

### **Orquestração - Kubernetes**

#### **Manifestos Necessários**
```yaml
# user-service/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: user-service:v1.0.0
        ports:
        - containerPort: 3000
        env:
        - name: POSTGRES_HOST
          valueFrom:
            configMapKeyRef:
              name: db-config
              key: host
        livenessProbe:
          httpGet:
            path: /health
            port: 3000
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 3000
          initialDelaySeconds: 5
          periodSeconds: 5
```

#### **Serviços Kubernetes**
- **ConfigMaps**: Para configurações não-sensíveis
- **Secrets**: Para credenciais e chaves
- **Ingress**: API Gateway (NGINX Ingress Controller)
- **Service Mesh**: Istio para observabilidade e segurança
- **HorizontalPodAutoscaler**: Baseado em CPU/Memory

### **CI/CD Pipeline**

#### **GitHub Actions Necessário**
```yaml
# .github/workflows/ci-cd.yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

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
    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: '20'
    - name: Setup Bun
      uses: oven-sh/setup-bun@v1

    - name: Test User Service
      run: |
        cd user
        bun install
        bun test
    - name: Test Catalog Service
      run: |
        cd catalog
        ./mvnw test
    - name: Test Search Service
      run: |
        cd search
        ./mvnw test

  build-and-push:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    steps:
    - name: Build and push Docker images
      uses: docker/build-push-action@v5
      with:
        context: .
        push: true
        tags: ${{ github.repository }}:latest
```

### **Monitoramento e Observabilidade**

#### **Ferramentas Necessárias**
- **Prometheus**: Coleta de métricas
- **Grafana**: Dashboards e visualização
- **ELK Stack**: Logs centralizados (Elasticsearch, Logstash, Kibana)
- **Jaeger**: Distributed tracing
- **Kiali**: Service mesh observabilidade (com Istio)

#### **Métricas Essenciais**
```java
// Catalog Service - Métricas Spring Boot
@Configuration
public class MetricsConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "catalog-service")
            .commonTags("version", "1.0.0");
    }
}
```

### **Terraform - Infraestrutura como Código**

#### **Estrutura Terraform**
```
infrastructure/
├── main.tf
├── variables.tf
├── outputs.tf
├── modules/
│   ├── eks/           # EKS Cluster
│   ├── rds/           # Relational Databases
│   ├── elasticache/   # Redis
│   ├── documentdb/    # MongoDB
│   ├── msk/           # Kafka (Managed Streaming)
│   └── networking/    # VPC, Subnets, Security Groups
└── environments/
    ├── dev/
    ├── staging/
    └── prod/
```

#### **Recursos AWS Necessários**
- **EKS**: Kubernetes managed
- **RDS**: PostgreSQL e MariaDB
- **DocumentDB**: MongoDB compatível
- **ElastiCache**: Redis
- **MSK**: Kafka managed
- **CloudWatch**: Logs e métricas
- **X-Ray**: Tracing distribuído

## 🔒 Segurança

### **Autenticação e Autorização**

#### **Melhorias no User Service**
```typescript
// user/src/lib/auth.ts - Melhorar configurações de segurança
export const auth = betterAuth({
  // ... configurações existentes

  // Adicionar rate limiting
  rateLimit: {
    window: 15 * 60 * 1000, // 15 minutes
    max: 100 // requests per window
  },

  // Configurar sessões mais seguras
  session: {
    expiresIn: 60 * 60 * 24 * 7, // 7 days
    updateAge: 60 * 60 * 24,     // 1 day
    cookie: {
      secure: process.env.NODE_ENV === 'production',
      httpOnly: true,
      sameSite: 'strict'
    }
  },

  // Adicionar 2FA
  twoFactor: {
    enabled: true,
    issuer: "E-commerce Platform"
  }
});
```

#### **JWT e Sessões**
- **Access Tokens**: Curta duração (15 min)
- **Refresh Tokens**: Longa duração com rotação
- **Session Cookies**: HttpOnly, Secure, SameSite
- **CSRF Protection**: Tokens anti-falsificação

### **API Security**

#### **Rate Limiting**
```java
// Catalog Service - Rate Limiting
@Configuration
public class RateLimitConfig {
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        return RateLimiterRegistry.of(Map.of(
            "api", RateLimiter.of("api", RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(1)))
        ));
    }
}
```

#### **Input Validation**
```java
// Catalog Service - Bean Validation
public record CreateProductRequest(
    @NotBlank @Size(min = 2, max = 100)
    String name,

    @NotNull @DecimalMin("0.01")
    BigDecimal price,

    @NotNull @Min(0)
    Integer stock,

    @Valid
    List<@NotBlank String> tags,

    @NotBlank
    String category,

    @Size(max = 1000)
    String description
) {}
```

### **Infraestrutura de Segurança**

#### **Network Security**
- **VPC**: Rede isolada
- **Security Groups**: Regras específicas por serviço
- **NACLs**: Network Access Control Lists
- **API Gateway**: Rate limiting, CORS, autenticação

#### **Secrets Management**
```yaml
# Kubernetes - External Secrets Operator
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: database-credentials
spec:
  secretStoreRef:
    name: aws-secretsmanager
    kind: SecretStore
  target:
    name: db-secret
    creationPolicy: Owner
  data:
  - secretKey: password
    remoteRef:
      key: prod/database
      property: password
```

#### **OWASP Top 10 Mitigations**
1. **Injection**: Prepared statements, ORM
2. **Broken Authentication**: JWT seguro, refresh tokens
3. **Sensitive Data Exposure**: TLS 1.3, encryption at rest
4. **XML External Entities**: JSON only APIs
5. **Broken Access Control**: RBAC, authorization middleware
6. **Security Misconfiguration**: Security headers, hardening
7. **Cross-Site Scripting**: CSP, input sanitization
8. **Insecure Deserialization**: Input validation
9. **Vulnerable Components**: Dependency scanning
10. **Insufficient Logging**: Structured logging, audit trails

### **Compliance e Auditoria**

#### **GDPR Compliance**
- **Data Encryption**: At rest and in transit
- **Right to be Forgotten**: Soft delete implementation
- **Data Portability**: Export user data endpoint
- **Consent Management**: Terms acceptance tracking

#### **Audit Logging**
```java
// Catalog Service - Audit Aspect
@Aspect
@Component
public class AuditAspect {
    @AfterReturning("execution(* com.example..*.*(..))")
    public void auditMethod(JoinPoint joinPoint) {
        AuditEvent event = new AuditEvent(
            joinPoint.getSignature().getName(),
            SecurityContextHolder.getContext().getAuthentication().getName(),
            LocalDateTime.now()
        );
        auditService.log(event);
    }
}
```

## ⚡ Performance

### **Database Optimization**

#### **PostgreSQL (User Service)**
```sql
-- Índices necessários
CREATE INDEX CONCURRENTLY idx_users_email ON users(email);
CREATE INDEX CONCURRENTLY idx_sessions_user_id ON sessions(user_id);
CREATE INDEX CONCURRENTLY idx_sessions_expires_at ON sessions(expires_at);

-- Particionamento para sessions
CREATE TABLE sessions_y2024m12 PARTITION OF sessions
    FOR VALUES FROM ('2024-12-01') TO ('2025-01-01');
```

#### **MongoDB (Catalog Service)**
```javascript
// Índices para produtos
db.products.createIndex({ "name": "text", "description": "text" });
db.products.createIndex({ "category": 1 });
db.products.createIndex({ "tags": 1 });
db.products.createIndex({ "price": 1 });
db.products.createIndex({ "createdAt": -1 });

// Sharding strategy
sh.shardCollection("catalog.products", { "_id": 1 });
```

#### **MariaDB (Search Service)**
```sql
-- Índices otimizados para buscas
CREATE FULLTEXT INDEX idx_products_search
ON products (name, description, category);

CREATE INDEX idx_products_category_price
ON products (category, price);

-- Query optimization
EXPLAIN SELECT * FROM products
WHERE MATCH(name, description) AGAINST ('laptop' IN NATURAL LANGUAGE MODE)
ORDER BY price ASC LIMIT 20;
```

### **Caching Strategy**

#### **Redis Cache Layers**
```java
// Catalog Service - Multi-level caching
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

#### **Cache Keys Strategy**
```java
@Service
public class ProductService {

    @Cacheable(value = "products", key = "#id")
    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    @Cacheable(value = "products", key = "'category:' + #category")
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @CacheEvict(value = "products", key = "#product.id")
    public Product update(Product product) {
        return productRepository.save(product);
    }
}
```

### **Message Queue Optimization**

#### **Kafka Topics Configuration**
```yaml
# docker-compose.yml - Otimizado
kafka:
  environment:
    KAFKA_NUM_PARTITIONS: 6
    KAFKA_DEFAULT_REPLICATION_FACTOR: 3
    KAFKA_COMPRESSION_TYPE: gzip
    KAFKA_LOG_SEGMENT_BYTES: 1073741824  # 1GB
    KAFKA_LOG_RETENTION_HOURS: 168       # 7 days
```

#### **Consumer Optimization**
```java
// Catalog Service - Kafka Consumer Config
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "catalog-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // Número de threads
        return factory;
    }
}
```

### **API Performance**

#### **Pagination e Sorting**
```java
// Catalog Service - Paginação otimizada
@RestController
public class ProductController {

    @GetMapping("/api/products")
    public Page<ProductResponse> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(sortDir.equals("desc") ?
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);
        return productService.findAll(pageable);
    }
}
```

#### **HTTP Caching**
```java
// Catalog Service - HTTP Caching Headers
@RestController
public class ProductController {

    @GetMapping("/api/products/{id}")
    @CacheControl(maxAge = 300) // 5 minutes
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
        return productService.findById(id)
            .map(product -> ResponseEntity.ok()
                .eTag("\"" + product.version() + "\"")
                .body(product))
            .orElse(ResponseEntity.notFound().build());
    }
}
```

### **Microservices Communication**

#### **Circuit Breaker Pattern**
```java
// User Service - Circuit Breaker para comunicação entre serviços
@Service
public class AuthUtils {

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackValidateSession")
    public Optional<String> extractUserId(HttpServletRequest request) {
        // Comunicação com user service
        return restTemplate.exchange(...)
            .map(this::extractUserIdFromResponse)
            .orElse(Optional.empty());
    }

    private Optional<String> fallbackValidateSession(HttpServletRequest request, Throwable t) {
        log.warn("User service unavailable, using cached validation");
        // Fallback logic
        return Optional.empty();
    }
}
```

#### **Service Discovery**
```yaml
# Kubernetes - Service Discovery
apiVersion: v1
kind: Service
metadata:
  name: user-service
  labels:
    app: user-service
spec:
  selector:
    app: user-service
  ports:
  - port: 3000
    targetPort: 3000
  type: ClusterIP
---
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-catalog-to-user
spec:
  podSelector:
    matchLabels:
      app: catalog
  policyTypes:
  - Ingress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: catalog
    ports:
    - protocol: TCP
        port: 3000
```

### **Load Testing e Benchmarks**

#### **Ferramentas Recomendadas**
- **Apache JMeter**: Load testing scripts
- **k6**: Modern load testing
- **Artillery**: Scenario-based testing
- **Locust**: Python-based load testing

#### **Benchmarks Necessários**
```javascript
// k6 load test example
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 }, // Ramp up to 100 users
    { duration: '5m', target: 100 }, // Stay at 100 users
    { duration: '2m', target: 200 }, // Ramp up to 200 users
    { duration: '5m', target: 200 }, // Stay at 200 users
    { duration: '2m', target: 0 },   // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(99)<1500'], // 99% of requests must complete below 1.5s
  },
};

export default function () {
  let response = http.get('http://localhost:8080/api/products');
  check(response, { 'status is 200': (r) => r.status === 200 });
  sleep(1);
}
```

### **Performance Monitoring**

#### **APM (Application Performance Monitoring)**
- **Response Times**: < 500ms para APIs críticas
- **Throughput**: > 1000 req/s por serviço
- **Error Rate**: < 0.1%
- **Database Query Time**: < 100ms

#### **Health Checks Avançados**
```java
// Catalog Service - Health Indicators
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Health health() {
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            return Health.up()
                .withDetail("database", "MongoDB")
                .withDetail("connection", "OK")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "MongoDB")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 📊 Conclusão e Próximos Passos

### **Pontuação Atual (1-10)**

| Aspecto | Pontuação | Status |
|---------|-----------|---------|
| **Funcionalidade** | 8/10 | ✅ Core features working |
| **DevOps** | 4/10 | ⚠️ Basic Docker setup |
| **Segurança** | 5/10 | ⚠️ Basic auth, needs hardening |
| **Performance** | 4/10 | ⚠️ No optimization yet |

### **Roadmap Priorizado**

#### **Fase 1: DevOps Fundamentals (2-3 semanas)**
- [ ] Kubernetes manifests para todos os serviços
- [ ] CI/CD pipeline completo
- [ ] Docker multi-stage otimizado
- [ ] ConfigMaps e Secrets

#### **Fase 2: Segurança (2-3 semanas)**
- [ ] JWT seguro com refresh tokens
- [ ] Rate limiting global
- [ ] Security headers (CSP, HSTS)
- [ ] Secrets management (Vault/ASM)

#### **Fase 3: Performance (2-3 semanas)**
- [ ] Database indexing e otimização
- [ ] Redis caching strategy
- [ ] Message queue tuning
- [ ] Load testing suite

#### **Fase 4: Observabilidade (1-2 semanas)**
- [ ] ELK stack para logs
- [ ] Prometheus/Grafana dashboards
- [ ] Distributed tracing
- [ ] Alerting rules

### **Métricas de Sucesso**

- **Performance**: P99 < 500ms, throughput > 1000 req/s
- **Disponibilidade**: 99.9% uptime
- **Segurança**: Zero vulnerabilidades críticas (OWASP Top 10)
- **Manutenibilidade**: < 30 min para deploy de hotfix

Este projeto tem uma base sólida de microserviços, mas precisa de investimentos significativos em DevOps, segurança e performance para estar pronto para produção enterprise.