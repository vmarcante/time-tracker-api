#!/usr/bin/env bash
set -e

ENVIRONMENT=$1
ONLY_BUILD=$2

if [ -z "$ENVIRONMENT" ]; then
    echo "Usage: ./build.sh <local|dev|prod> [--build-only]"
    exit 1
fi

if [ "$ENVIRONMENT" != "local" ] && [ "$ENVIRONMENT" != "dev" ] && [ "$ENVIRONMENT" != "prod" ]; then
    echo "Invalid environment: $ENVIRONMENT"
    echo "Usage: ./build.sh <local|dev|prod> [--build-only]"
    exit 1
fi

ENV_FILE=".env.$ENVIRONMENT"
COMPOSE_FILE="docker-compose.$ENVIRONMENT.yml"

if [ ! -f "$ENV_FILE" ]; then
    echo "Environment file not found: $ENV_FILE"
    echo "Create it from the example: cp $ENV_FILE.example $ENV_FILE"
    exit 1
fi

if [ ! -f "$COMPOSE_FILE" ]; then
    echo "Compose file not found: $COMPOSE_FILE"
    exit 1
fi

echo "Using environment: $ENVIRONMENT"
echo "Compose file: $COMPOSE_FILE"
echo "Environment file: $ENV_FILE"

if [ "$ONLY_BUILD" == "--build-only" ]; then
    echo "Building Docker image..."
    docker-compose -f "$COMPOSE_FILE" build
else
    echo "Building and starting container..."
    docker-compose -f "$COMPOSE_FILE" up --build
fi
