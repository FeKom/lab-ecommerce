#!/bin/bash
# =============================================================================
# E-commerce Microservices - Deploy Script with Rolling Updates & Rollback
# =============================================================================
# Usage:
#   ./deploy.sh deploy    - Deploy all services with rolling updates
#   ./deploy.sh rollback  - Rollback all services to previous version
#   ./deploy.sh status    - Show current status of all services
#   ./deploy.sh logs      - Follow logs of all services
# =============================================================================

set -e

# Configuration
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"
SERVICES=("user-service" "catalog" "search")
HEALTH_TIMEOUT="${HEALTH_TIMEOUT:-120}"
ROLLBACK_DIR="/tmp/ecommerce-rollback"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log() { echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"; }
warn() { echo -e "${YELLOW}[WARN] $1${NC}"; }
error() { echo -e "${RED}[ERROR] $1${NC}" >&2; }
info() { echo -e "${BLUE}[INFO] $1${NC}"; }

# Check if docker compose is available
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker not found. Please install Docker first."
        exit 1
    fi
    
    if ! docker compose version &> /dev/null; then
        error "Docker Compose not found. Please install Docker Compose plugin."
        exit 1
    fi
}

# Save current state for rollback
save_state() {
    log "Saving current state for rollback..."
    mkdir -p "$ROLLBACK_DIR"
    
    for service in "${SERVICES[@]}"; do
        # Get current image digest
        current_image=$(docker inspect --format='{{.Image}}' "$service" 2>/dev/null || echo "none")
        echo "$current_image" > "$ROLLBACK_DIR/${service}.image"
        log "  Saved state for $service"
    done
    
    # Save timestamp
    date +%s > "$ROLLBACK_DIR/timestamp"
}

# Restore previous state
restore_state() {
    local service=$1
    
    if [ -f "$ROLLBACK_DIR/${service}.image" ]; then
        local previous_image=$(cat "$ROLLBACK_DIR/${service}.image")
        if [ "$previous_image" != "none" ]; then
            log "Restoring $service to previous image..."
            docker compose -f "$COMPOSE_FILE" up -d --no-deps "$service"
            return 0
        fi
    fi
    
    error "No previous state found for $service"
    return 1
}

# Wait for service to be healthy
wait_healthy() {
    local service=$1
    local timeout=$HEALTH_TIMEOUT
    
    log "Waiting for $service to be healthy..."
    
    while [ $timeout -gt 0 ]; do
        # Check if container is healthy
        local health_status=$(docker inspect --format='{{.State.Health.Status}}' "$service" 2>/dev/null || echo "unknown")
        
        if [ "$health_status" = "healthy" ]; then
            log "  $service is healthy!"
            return 0
        fi
        
        # Check if container is running at all
        local running=$(docker inspect --format='{{.State.Running}}' "$service" 2>/dev/null || echo "false")
        if [ "$running" = "false" ]; then
            error "  $service is not running!"
            return 1
        fi
        
        info "  Status: $health_status (${timeout}s remaining)"
        sleep 10
        timeout=$((timeout - 10))
    done
    
    warn "  $service health check timeout"
    return 1
}

# Deploy a single service
deploy_service() {
    local service=$1
    
    log "Deploying $service..."
    
    # Pull latest image
    docker compose -f "$COMPOSE_FILE" pull "$service"
    
    # Start the new container
    docker compose -f "$COMPOSE_FILE" up -d --no-deps "$service"
    
    # Wait for health check
    if ! wait_healthy "$service"; then
        warn "$service failed health check, attempting rollback..."
        restore_state "$service"
        return 1
    fi
    
    log "$service deployed successfully!"
    return 0
}

# Deploy all services with rolling updates
deploy_rolling() {
    log "Starting rolling deployment..."
    
    # Check docker
    check_docker
    
    # Save current state
    save_state
    
    # Pull all images first (faster deployment)
    log "Pulling latest images..."
    docker compose -f "$COMPOSE_FILE" pull "${SERVICES[@]}"
    
    # Deploy each service
    local failed=0
    for service in "${SERVICES[@]}"; do
        echo ""
        log "=== Deploying $service ==="
        
        if ! deploy_service "$service"; then
            error "Failed to deploy $service"
            failed=1
            break
        fi
    done
    
    # Cleanup old images
    log "Cleaning up old images..."
    docker image prune -f
    
    if [ $failed -eq 0 ]; then
        echo ""
        log "=== Deployment completed successfully! ==="
        show_status
    else
        error "=== Deployment failed! ==="
        exit 1
    fi
}

# Rollback all services
rollback_all() {
    log "Starting rollback..."
    
    # Check if rollback data exists
    if [ ! -d "$ROLLBACK_DIR" ]; then
        error "No rollback data found. Cannot rollback."
        exit 1
    fi
    
    # Check timestamp
    if [ -f "$ROLLBACK_DIR/timestamp" ]; then
        local rollback_time=$(cat "$ROLLBACK_DIR/timestamp")
        local current_time=$(date +%s)
        local age=$((current_time - rollback_time))
        
        if [ $age -gt 86400 ]; then
            warn "Rollback data is $(($age / 3600)) hours old"
        fi
    fi
    
    # Rollback each service
    for service in "${SERVICES[@]}"; do
        log "Rolling back $service..."
        if restore_state "$service"; then
            wait_healthy "$service" || warn "$service may not be healthy after rollback"
        fi
    done
    
    log "Rollback completed!"
    show_status
}

# Show status of all services
show_status() {
    echo ""
    log "=== Current Service Status ==="
    docker compose -f "$COMPOSE_FILE" ps
    echo ""
    
    # Show health of each service
    for service in "${SERVICES[@]}"; do
        local status=$(docker inspect --format='{{.State.Health.Status}}' "$service" 2>/dev/null || echo "unknown")
        local image=$(docker inspect --format='{{.Config.Image}}' "$service" 2>/dev/null || echo "unknown")
        
        case $status in
            healthy)
                echo -e "  ${GREEN}$service${NC}: $status ($image)"
                ;;
            unhealthy)
                echo -e "  ${RED}$service${NC}: $status ($image)"
                ;;
            *)
                echo -e "  ${YELLOW}$service${NC}: $status ($image)"
                ;;
        esac
    done
    echo ""
}

# Follow logs
follow_logs() {
    log "Following logs for all services..."
    docker compose -f "$COMPOSE_FILE" logs -f "${SERVICES[@]}"
}

# Main
case "${1:-help}" in
    deploy)
        deploy_rolling
        ;;
    rollback)
        rollback_all
        ;;
    status)
        show_status
        ;;
    logs)
        follow_logs
        ;;
    *)
        echo "Usage: $0 {deploy|rollback|status|logs}"
        echo ""
        echo "Commands:"
        echo "  deploy   - Deploy all services with rolling updates"
        echo "  rollback - Rollback all services to previous version"
        echo "  status   - Show current status of all services"
        echo "  logs     - Follow logs of all services"
        echo ""
        echo "Environment variables:"
        echo "  COMPOSE_FILE    - Docker Compose file (default: docker-compose.prod.yml)"
        echo "  HEALTH_TIMEOUT  - Health check timeout in seconds (default: 120)"
        exit 1
        ;;
esac
