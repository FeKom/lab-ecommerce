#!/bin/bash -xe

# Verificar argumento
if [ $# -ne 1 ]; then
    echo "Uso: $0 <nome_do_app>"
    exit 1
fi

APP=$1
ROOT=$(pwd)

# Mudar para o diretório do app
cd "$APP" || exit 1

# Etapa 1: Limpar e configurar versão
echo "Cleaning Maven project..."
./mvnw clean

echo "Setting version (removing snapshot)..."
./mvnw versions:set -DremoveSnapshot

echo "Getting app version..."
APP_VERSION=$(./mvnw -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)

echo "Building version ${APP_VERSION}..."
./mvnw package

echo "Restoring snapshot version..."
./mvnw versions:set -DnextSnapshot

# Etapa 2: Commit no Git
echo "Committing version bump..."
git add pom.xml
git commit -m "cicd: bump version ${APP}:${APP_VERSION}"

# Voltar ao diretório raiz
cd "$ROOT" || exit 1

# Etapa 3: Construir imagem Docker
echo "Building Docker image for ${APP} with tag ${APP_VERSION}..."
TAG="${APP_VERSION}" docker compose build --no-cache "${APP}"

# Etapa 4: Verificar imagem
echo "Listing Docker images..."
docker images "fekom/${APP}"

exit 0