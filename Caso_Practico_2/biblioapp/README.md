# BiblioApp: Caso Práctico 2 (SC-403)

Sistema de préstamo de libros con roles, API REST y detección de préstamos atrasados.

## Qué incluye

- **Entidad `Prestamo`** asociada a `Libro` y `Usuario` con `@ManyToOne` (Semana 9).
- **Roles `BIBLIOTECARIO` / `LECTOR`** con `@PreAuthorize` y página `/403` (Semanas 10–11).
- **API REST** de libros y préstamos, autenticada con JWT (Semana 12).
- **Consulta JPQL propia** de préstamos atrasados (`PrestamoRepository.prestamosAtrasados`).

## Requisitos

- JDK 25 o superior (probado con OpenJDK 26).
- MySQL 8 o MariaDB.
- Maven (o el `mvnw` incluido).

## Arranque

```bash
# 1. Crear la base
mysql -u root -e "CREATE DATABASE IF NOT EXISTS biblioappdb CHARACTER SET utf8mb4;"

# 2. Variables de entorno: la clave JWT es obligatoria y no está en el repo
export JWT_SECRET="una-clave-de-al-menos-32-caracteres-propia"
export DB_PASSWORD=tu_password   # solo si la instancia la pide

# 3. Arrancar (puerto 8082)
./mvnw spring-boot:run

# 4. Cargar el seed (después del primer arranque: Hibernate crea las tablas)
mysql -u root biblioappdb < seed-data.sql
```

App en http://localhost:8082

### Usuarios de prueba

| Usuario  | Password     | Rol           |
|----------|--------------|---------------|
| `biblio` | `biblio123`  | BIBLIOTECARIO |
| `lector` | `lector123`  | LECTOR        |
| `ana`    | `ana123`     | LECTOR        |

## Rutas

### Vistas (Thymeleaf)

| Ruta | Acceso |
|------|--------|
| `GET /` | público |
| `GET /login` | público |
| `GET /403` | público |
| `GET /libros`, `GET /libros/{id}` | autenticado |
| `GET /libros/nuevo`, `POST /libros`, `GET /libros/{id}/editar`, `POST /libros/{id}`, `POST /libros/{id}/eliminar` | BIBLIOTECARIO |
| `GET /prestamos`, `GET /prestamos/nuevo`, `POST /prestamos`, `POST /prestamos/{id}/devolver`, `POST /prestamos/{id}/eliminar` | BIBLIOTECARIO |
| `GET /prestamos/atrasados` | BIBLIOTECARIO |
| `GET /prestamos/mios` | autenticado (solo los propios) |

### API REST

| Endpoint | Acceso |
|----------|--------|
| `POST /api/auth/login` | público → devuelve `{ "token": "..." }` |
| `GET /api/libros`, `GET /api/libros/{id}` | autenticado |
| `POST /api/libros`, `PUT /api/libros/{id}`, `DELETE /api/libros/{id}` | BIBLIOTECARIO |
| `GET /api/prestamos` | BIBLIOTECARIO |
| **`GET /api/prestamos/atrasados`** | BIBLIOTECARIO |
| `GET /api/prestamos/atrasados/resumen` | BIBLIOTECARIO |
| `POST /api/prestamos`, `POST /api/prestamos/{id}/devolver` | BIBLIOTECARIO |
| `GET /api/prestamos/mios` | autenticado |
| `GET /api/roles` | autenticado |

Todos los endpoints de `/api/**` (menos el login) piden el header
`Authorization: Bearer <token>`.

## Postman

Importar `postman-collection.json`. Correr primero **1. Autenticación → Login
BIBLIOTECARIO**: el script de test guarda el JWT en la variable `{{token}}` y el
resto de los requests lo usan solo. Para probar los 403, correr **Login LECTOR** y
después la carpeta **4. Pruebas de autorización**.

## Notas técnicas verificadas

- `nullCatalogMeansCurrent=true` en la URL JDBC: sin ese parámetro, el driver MySQL
  le reporta a Hibernate las tablas de todas las bases del servidor; si en la misma
  instancia hay otra base con tabla `usuarios`, Hibernate intenta un `ALTER` en vez del
  `CREATE` y la FK `usuario_id` de `prestamos` nunca se crea (`errno: 150`).
- La entidad `Libro` tiene solo constructor vacío: con un constructor de varios
  argumentos, Jackson lo toma como *creator* y un JSON sin `ejemplaresDisponibles`
  falla con `Cannot map null into type int`.
- Dos `SecurityFilterChain`: `/api/**` responde 401/403; las vistas redirigen a
  `/login` y `/403`.
- `app.jwt.secret` no tiene valor por defecto: si falta `JWT_SECRET`, la app no arranca.
  Así la clave no queda versionada en el repo.
