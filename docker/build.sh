#!/bin/bash

# Convert CRLF to LF if needed
sed -i 's/\r$//' ./docker/.env

# Source the environment variables
export $(cat ./docker/.env | grep -v '^#' | xargs)

# Set variables
IMAGE_NAME="inchfab-api"
GIT_HASH=$(git rev-parse --short HEAD)
TIMESTAMP=$(date +%Y%m%d%H%M%S)

# Check Docker credentials
if [ -z "$DOCKER_USERNAME" ] || [ -z "$DOCKER_PASSWORD" ]; then
    echo "Error: Docker credentials not found in environment variables"
    echo "Please ensure DOCKER_USERNAME and DOCKER_PASSWORD are set in .env file"
    exit 1
fi

# Docker login - using a more robust method
echo "Logging in to Docker registry..."
docker logout >/dev/null 2>&1 || true
echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin
if [ $? -ne 0 ]; then
    echo "Error: Docker login failed"
    exit 1
fi

# Build the project
echo "Building Maven project..."
./mvnw clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "Error: Maven build failed"
    exit 1
fi

# Build and push the image
echo "Building and pushing Docker image..."
docker build \
  --no-cache \
  --build-arg JAR_FILE=target/*.jar \
  -f docker/Dockerfile \
  -t ${DOCKER_USERNAME}/${IMAGE_NAME}:${APP_VERSION} \
  .

if [ $? -ne 0 ]; then
    echo "Error: Docker build failed"
    exit 1
fi

docker push ${DOCKER_USERNAME}/${IMAGE_NAME}:${APP_VERSION}

# Logout
docker logout

echo "Build and push completed successfully!"
