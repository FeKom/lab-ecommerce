# 📊 Guia Rápido - Observabilidade E-commerce

## 🚀 Início Rápido

### 1. Configurar Variáveis de Ambiente
```bash
# Copiar arquivo de exemplo
cp env-example.txt .env

# Editar com suas configurações seguras
nano .env
```

### 2. Iniciar Observabilidade
```bash
# Executar script de setup
./setup-observability.sh
```

### 3. Iniciar Plataforma Completa
```bash
# Com observabilidade
docker compose up -d

## 📈 Dashboards e Visualizações

### URLs de Acesso
- **Grafana**: http://localhost:3000 (admin/admin)
- **Kibana**: http://localhost:5601
- **Prometheus**: http://localhost:9090
- **Jaeger**: http://localhost:16686

### Importar Dashboard no Grafana
1. Acesse Grafana
2. Vá em "Dashboards" → "Import"
3. Use o arquivo `monitoring/grafana-dashboard.json`

## 🔍 Logs Estruturados

### Configuração dos Serviços
Cada serviço precisa ser configurado para enviar logs estruturados:

#### Catalog Service (Spring Boot)
```xml
<!-- Adicionar ao pom.xml -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.3</version>
</dependency>
```

#### User Service (Node.js/Bun)
```typescript
// Já implementado no logger.ts
// Configurar LOG_FORMAT=json no .env
```

#### Search Service (Quarkus)
```properties
# Já configurado no application.properties
quarkus.log.console.json=true
```

## 📊 Métricas Implementadas

### HTTP Métricas
- Taxa de requests por endpoint
- Tempo de resposta (P50, P95, P99)
- Taxa de erro por status code

### Business Métricas
- Produtos criados/atualizados/deletados
- Usuários registrados
- Queries de busca executadas

### Infraestrutura
- Uso de CPU/Memória JVM
- Conexões de banco ativas
- Cache hit/miss rate
- Kafka consumer lag

## 🕵️ Tracing Distribuído

### Configuração por Serviço

#### Catalog Service
```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # Em produção: 0.1
  otlp:
    tracing:
      endpoint: http://jaeger:4318/v1/traces
```

#### Search Service
```properties
quarkus.opentelemetry.enabled=true
quarkus.opentelemetry.tracer.exporter.otlp.endpoint=http://jaeger:4318
quarkus.opentelemetry.tracer.sampler.ratio=1.0
```

#### User Service
```typescript
// Já configurado no tracing.ts
// Usar OTEL_EXPORTER_OTLP_TRACES_ENDPOINT no .env
```

## 🚨 Alertas Recomendados

### No Grafana (Alert Rules)
```yaml
# Exemplo de alerta
ALERT ServiceDown
  IF up{job="catalog-service"} == 0
  FOR 5m
  LABELS { severity = "critical" }
  ANNOTATIONS {
    summary = "Service {{ $labels.job }} is down",
    description = "Service {{ $labels.job }} has been down for more than 5 minutes."
  }
```

### Métricas Críticas para Alertar
- Serviço indisponível (> 5min)
- Taxa de erro > 5%
- Latência P95 > 2s
- Uso de memória > 85%
- Consumer lag Kafka > 1000

## 🔧 Troubleshooting

### Verificar Status dos Serviços
```bash
# Todos os containers
docker ps

# Logs de um serviço específico
docker logs -f catalog

# Verificar conectividade
docker exec catalog curl -f http://localhost:8080/actuator/health
```

### Limpar Dados de Observabilidade
```bash
# Parar e remover volumes
docker compose down -v

# Reiniciar observabilidade
./setup-observability.sh
```

## 📚 Próximos Passos

1. **Implementar métricas customizadas** nos serviços
2. **Configurar alertas** no Grafana/Prometheus
3. **Adicionar testes de carga** para validar observabilidade
4. **Implementar log aggregation** com Fluentd/Filebeat
5. **Configurar dashboards** específicos por serviço

## 🆘 Suporte

Para problemas com observabilidade:
1. Verificar logs dos containers de observabilidade
2. Validar configurações no `.env`
3. Consultar documentação completa em `MELHORIAS_OBSERVABILIDADE.md`