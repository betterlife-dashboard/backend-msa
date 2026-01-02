# better-life backend/msa

Multi-module Spring Boot backend for better-life. This repo hosts the core services and deployment artifacts used by local Docker Compose and the home server deployment.

## Services
- gateway (8080)
- auth (8081)
- todo (8082)
- notify (8083)
- focus (8084)

## Repository layout
- `auth/`, `todo/`, `notify/`, `focus/`, `gateway/`: Spring Boot services (Gradle)
- `docker-compose.yml`: local dev stack
- `deploy/`: deployment compose files and env files
- `.github/workflows/`: CI/CD for building and deploying services

## Local development (Docker Compose)
Start infra + services:
```bash
docker compose up -d
```

Ports are mapped in `docker-compose.yml`.

## Deployment (home server)
Two compose files are used on the server:
- `deploy/docker-compose.infra.yml`: mysql/redis/rabbit + frontend
- `deploy/docker-compose.service.yml`: service containers using env files

Env files:
- `deploy/auth.env`
- `deploy/todo.env`
- `deploy/notify.env`
- `deploy/focus.env`
- `deploy/gateway.env`

### Firebase key (notify)
The notify service expects `GOOGLE_APPLICATION_CREDENTIALS` to point to the service account JSON file. In deployment, the key is written on the server and mounted into the container.

## Notes
- Production profile enables Flyway validation (`application-prod.yml` in each service).
- If a migration fails, repair the Flyway history before restarting the service.
