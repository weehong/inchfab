# InchFab API

## Docker Setup

### Development Environment

1. Copy the environment template:
```bash
cp docker/.env.example docker/.env
```

2. Edit the `.env` file with your configuration values

3. Build and start the containers:
```bash
cd docker
docker-compose up -d
```

### Test Environment

1. Create test environment file:
```bash
cp docker/.env.example docker/.env.test
```

2. Edit the `.env.test` file

3. Build test image and start containers:
```bash
cd docker
./build.sh
docker-compose -f docker-compose.yml -f docker-compose.test.yml up -d
```

### Environment Files

- `.env.example`: Template with all required variables
- `.env`: Local development configuration (not committed)
- `.env.test`: Test environment configuration
- `.env.staging`: Staging environment configuration
- `.env.prod`: Production environment configuration

### Docker Commands

```bash
# Build and push test image
cd docker
./build.sh

# Start development environment
docker-compose up -d

# Start test environment
docker-compose -f docker-compose.yml -f docker-compose.test.yml up -d

# View logs
docker-compose logs -f

# Stop all containers
docker-compose down
```