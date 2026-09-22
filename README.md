# Yarumito Schedule

Sistema de gestión de docentes y horarios del I.E. Rural Yarumito.

El proyecto incluye:

- **Frontend:** React + Vite (puerto `5173`)
- **Backend:** Spring Boot + PostgreSQL (puerto `8080`)
- **Base de datos:** PostgreSQL 16 (puerto `5432`)

Todo se levanta con Docker Compose. No hace falta instalar Node, Java ni PostgreSQL en la máquina.

---

## Requisitos

Antes de clonar, instala:

1. [Git](https://git-scm.com/)
2. [Docker Desktop](https://www.docker.com/products/docker-desktop/) (incluye Docker Compose)

Comprueba que estén disponibles:

```bash
git --version
docker --version
docker compose version
```

Deja Docker Desktop **en ejecución** antes del siguiente paso.

---

## 1. Clonar el repositorio

```bash
git clone https://github.com/daluz0221/yarumito-schedule.git
cd yarumito-schedule
```

Si ya tienes el repo y quieres actualizarlo:

```bash
git pull
```

---

## 2. Levantar el proyecto

La primera vez (o cuando cambie el backend o el frontend) construye las imágenes:

```bash
docker compose up --build
```

Cuando los tres servicios estén listos:

| Servicio   | URL                         |
|------------|-----------------------------|
| Frontend   | http://localhost:5173       |
| Backend    | http://localhost:8080       |
| PostgreSQL | `localhost:5432` (interno)  |

El frontend espera a que el backend responda. El primer arranque puede tardar 1–2 minutos.

Para dejarlo en segundo plano:

```bash
docker compose up --build -d
```

---

## 3. Iniciar sesión

Abre http://localhost:5173 e ingresa con el rector de desarrollo (se crea solo al arrancar el backend):

| Campo        | Valor                      |
|--------------|----------------------------|
| Correo       | `rector@yarumito.edu.co`   |
| Contraseña   | `Admin123!`                |

---

## 4. (Opcional) Cargar docentes de prueba

El arranque normal **no** crea docentes de demostración. Si quieres ~15 docentes y áreas para explorar el listado y el perfil:

```bash
docker compose --profile seed run --rm seed
```

El comando es idempotente: si los datos ya existen, no los duplica.

Si cambiaste el código del seed y la imagen está cacheada, reconstruye esa imagen:

```bash
docker compose --profile seed build seed
docker compose --profile seed run --rm seed
```

---

## Comandos útiles

```bash
# Ver logs en vivo
docker compose logs -f

# Detener los contenedores (conserva la base de datos)
docker compose down

# Detener y borrar el volumen de PostgreSQL (base limpia)
docker compose down -v

# Volver a levantar sin reconstruir
docker compose up
```

Después de `down -v`, el rector se vuelve a crear al arrancar. Si quieres otra vez los docentes de demo, vuelve a ejecutar el seed.

---

## Puertos ocupados

Si `5173`, `8080` o `5432` ya están en uso, detén el otro proceso o cambia el mapeo en `docker-compose.yml`.
