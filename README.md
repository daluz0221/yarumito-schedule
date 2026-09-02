# yarumito-schedule

Proyecto desarrollado.

Actualmente el proyecto cuenta únicamente con un frontend básico desarrollado con **React + Vite**, ejecutándose mediante **Docker Compose**.

## Requisitos

Antes de comenzar, asegúrate de tener instalado:

- [Docker](https://www.docker.com/)
- Docker Compose (incluido actualmente en Docker Desktop)

Puedes comprobar que Docker está instalado ejecutando:

```bash
docker --version
```

# Cómo levantar el proyecto

## Requisitos

- Docker
- Docker Compose

Puedes verificar que estén instalados con:

### 1. Clonar el repositorio

```bash
git clone https://github.com/daluz0221/yarumito-schedule.git
cd yarumito-schedule
```

### 2. Levantar el proyecto
```bash
docker compose up --build
```


#### Comandos útiles

```bash
# Detener el proyecto
docker compose down

# Levantar normalmente
docker compose up

# Levantar y construir
docker compose up --build

# Levantar en segundo plano
docker compose up -d


```