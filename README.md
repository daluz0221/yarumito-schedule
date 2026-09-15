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
### Configurar variables de entorno


1. Copia el archivo `.env.example` a `.env`:
   ```bash
   cp .env.example .env
   ```
2. Completa con las credenciales locales

    ```
    
    ```



## Arquitectura

### Backend

```
src/
├── main/
│   ├── java/
│   │   └── co/
│   │       └── edu/
│   │           └── ieruralyarumito/
│   │               └── backend/
│   │                   ├── BackendApplication.java
│   │                   │
│   │                   ├── config/                    # Configuraciones globales
│   │                   │   ├── SecurityConfig.java
│   │                   │   ├── WebConfig.java
│   │                   │   └── SwaggerConfig.java
│   │                   │
│   │                   ├── controller/                # Capa de presentación (endpoints)
│   │                   │   ├── UsuarioController.java
│   │                   │   ├── ProductoController.java
│   │                   │   └── PedidoController.java
│   │                   │
│   │                   ├── service/                   # Capa de negocio
│   │                   │   ├── UsuarioService.java
│   │                   │   ├── ProductoService.java
│   │                   │   └── PedidoService.java
│   │                   │
│   │                   ├── repository/                # Capa de acceso a datos
│   │                   │   ├── UsuarioRepository.java
│   │                   │   ├── ProductoRepository.java
│   │                   │   └── PedidoRepository.java
│   │                   │
│   │                   ├── model/                     # Entidades de base de datos
│   │                   │   ├── Usuario.java
│   │                   │   ├── Producto.java
│   │                   │   └── Pedido.java
│   │                   │
│   │                   ├── dto/                       # Data Transfer Objects
│   │                   │   ├── request/
│   │                   │   │   ├── CrearUsuarioRequest.java
│   │                   │   │   └── ActualizarUsuarioRequest.java
│   │                   │   └── response/
│   │                   │       ├── UsuarioResponse.java
│   │                   │       └── ProductoResponse.java
│   │                   │
│   │                   ├── exception/                 # Manejo de errores
│   │                   │   ├── GlobalExceptionHandler.java
│   │                   │   ├── ResourceNotFoundException.java
│   │                   │   └── BusinessException.java
│   │                   │
│   │                   └── util/                      # Utilidades y helpers
│   │                       ├── DateUtils.java
│   │                       └── ValidationUtils.java
│   │
│   └── resources/
│       ├── application.properties                     # Configuración principal
│       ├── application-dev.properties                 # Configuración desarrollo
│       ├── application-prod.properties                # Configuración producción
│       │
│       ├── db/
│       │   └── migration/                             # Migraciones Flyway/Liquibase
│       │       ├── V1__create_usuarios_table.sql
│       │       └── V2__create_productos_table.sql
│       │
│       └── static/                                    # Archivos estáticos (si los necesitas)
│           └── .gitkeep
│
└── test/
    └── java/
        └── co/
            └── edu/
                └── ieruralyarumito/
                    └── backend/
                        ├── BackendApplicationTests.java
                        ├── controller/
                        │   └── UsuarioControllerTest.java
                        ├── service/
                        │   └── UsuarioServiceTest.java
                        └── repository/
                            └── UsuarioRepositoryTest.java
```