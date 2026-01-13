#!/bin/bash

# Setup Observabilidade - E-commerce Platform
# Este script configura a stack completa de observabilidade

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[WARN] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}"
    exit 1
}

info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

# Verificar se Docker está rodando
check_docker() {
    log "Verificando Docker..."
    if ! docker info >/dev/null 2>&1; then
        error "Docker não está rodando. Inicie o Docker primeiro."
    fi
    log "✅ Docker está rodando"
}

# Criar rede de monitoramento
create_network() {
    log "Criando rede de monitoramento..."
    if ! docker network ls | grep -q "monitoring"; then
        docker network create monitoring
        log "✅ Rede 'monitoring' criada"
    else
        log "✅ Rede 'monitoring' já existe"
    fi
}

# Iniciar ELK Stack
start_elk() {
    log "Iniciando ELK Stack..."

    # Elasticsearch
    if ! docker ps | grep -q "elasticsearch"; then
        docker run -d \
            --name elasticsearch \
            --net monitoring \
            -p 9200:9200 \
            -e "discovery.type=single-node" \
            -e "xpack.security.enabled=false" \
            -v elasticsearch_data:/usr/share/elasticsearch/data \
            elasticsearch:8.11.0
        log "✅ Elasticsearch iniciado"
    else
        log "✅ Elasticsearch já está rodando"
    fi

    # Logstash
    if ! docker ps | grep -q "logstash"; then
        docker run -d \
            --name logstash \
            --net monitoring \
            -p 5044:5044 \
            -v $(pwd)/monitoring/logstash.conf:/usr/share/logstash/pipeline/logstash.conf \
            logstash:8.11.0
        log "✅ Logstash iniciado"
    else
        log "✅ Logstash já está rodando"
    fi

    # Kibana
    if ! docker ps | grep -q "kibana"; then
        docker run -d \
            --name kibana \
            --net monitoring \
            -p 5601:5601 \
            --link elasticsearch:elasticsearch \
            kibana:8.11.0
        log "✅ Kibana iniciado"
    else
        log "✅ Kibana já está rodando"
    fi
}

# Iniciar Prometheus e Grafana
start_monitoring() {
    log "Iniciando Prometheus e Grafana..."

    # Prometheus
    if ! docker ps | grep -q "prometheus"; then
        docker run -d \
            --name prometheus \
            --net monitoring \
            -p 9090:9090 \
            -v $(pwd)/monitoring/prometheus.yml:/etc/prometheus/prometheus.yml \
            -v prometheus_data:/prometheus \
            prom/prometheus:latest
        log "✅ Prometheus iniciado"
    else
        log "✅ Prometheus já está rodando"
    fi

    # Grafana
    if ! docker ps | grep -q "grafana"; then
        docker run -d \
            --name grafana \
            --net monitoring \
            -p 3000:3000 \
            -e "GF_SECURITY_ADMIN_PASSWORD=admin" \
            -v grafana_data:/var/lib/grafana \
            grafana/grafana:latest
        log "✅ Grafana iniciado"
    else
        log "✅ Grafana já está rodando"
    fi
}

# Iniciar Jaeger
start_tracing() {
    log "Iniciando Jaeger..."

    if ! docker ps | grep -q "jaeger"; then
        docker run -d \
            --name jaeger \
            --net monitoring \
            -p 16686:16686 \
            -p 14268:14268 \
            -e "COLLECTOR_OTLP_ENABLED=true" \
            jaegertracing/all-in-one:latest
        log "✅ Jaeger iniciado"
    else
        log "✅ Jaeger já está rodando"
    fi
}

# Aguardar serviços ficarem saudáveis
wait_for_services() {
    log "Aguardando serviços ficarem saudáveis..."

    # Aguardar Elasticsearch
    info "Aguardando Elasticsearch..."
    for i in {1..30}; do
        if curl -s http://localhost:9200/_cluster/health | grep -q '"status":"green\|yellow"'; then
            log "✅ Elasticsearch está saudável"
            break
        fi
        sleep 2
    done

    # Aguardar Kibana
    info "Aguardando Kibana..."
    for i in {1..30}; do
        if curl -s http://localhost:5601/api/status | grep -q '"status":"green\|yellow"'; then
            log "✅ Kibana está saudável"
            break
        fi
        sleep 2
    done

    # Aguardar Prometheus
    info "Aguardando Prometheus..."
    for i in {1..30}; do
        if curl -s http://localhost:9090/-/ready | grep -q "Prometheus is ready"; then
            log "✅ Prometheus está saudável"
            break
        fi
        sleep 2
    done

    # Aguardar Grafana
    info "Aguardando Grafana..."
    for i in {1..30}; do
        if curl -s http://localhost:3000/api/health | grep -q '"database":"ok"'; then
            log "✅ Grafana está saudável"
            break
        fi
        sleep 2
    done

    # Aguardar Jaeger
    info "Aguardando Jaeger..."
    for i in {1..30}; do
        if curl -s http://localhost:16686/api/services | grep -q "jaeger"; then
            log "✅ Jaeger está saudável"
            break
        fi
        sleep 2
    done
}

# Mostrar status final
show_status() {
    echo ""
    log "🎉 Setup de observabilidade concluído!"
    echo ""
    info "📊 URLs de acesso:"
    echo "  🔍 Kibana (Logs):     http://localhost:5601"
    echo "  📈 Grafana:           http://localhost:3000 (admin/admin)"
    echo "  📊 Prometheus:        http://localhost:9090"
    echo "  🕵️  Jaeger (Tracing):  http://localhost:16686"
    echo ""
    info "🔧 Comandos úteis:"
    echo "  docker logs -f elasticsearch"
    echo "  docker logs -f logstash"
    echo "  docker logs -f prometheus"
    echo "  docker logs -f grafana"
    echo "  docker logs -f jaeger"
    echo ""
    warn "💡 Lembre-se de conectar sua aplicação aos serviços de observabilidade!"
}

# Função principal
main() {
    log "🚀 Iniciando setup de observabilidade..."

    check_docker
    create_network
    start_elk
    start_monitoring
    start_tracing
    wait_for_services
    show_status

    log "✅ Setup concluído com sucesso!"
}

# Executar apenas se chamado diretamente
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi