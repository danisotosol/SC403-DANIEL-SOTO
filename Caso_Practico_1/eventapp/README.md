# EventApp — Caso Práctico 1 (SC-403)

Sistema de gestión de eventos públicos. **Proyecto base** para el Caso Práctico 1.

## Estado actual del proyecto

Lo que **ya funciona** cuando descomprimas y arranques:

- Entidad `Evento` con todos sus campos (sin validaciones).
- `EventoRepository` extiende `JpaRepository` + 2 query methods de referencia.
- `EventoService` con métodos básicos (`listar`, `buscarPorId`, `guardar`, `eliminar`, `buscarPorCategoria`, `buscarProximos`).
- `HomeController` con `GET /`.
- `EventoController` con `GET /eventos` (listar) y `GET /eventos/{id}` (detalle).
- Vistas Thymeleaf: `home.html`, `eventos.html`, `evento.html`, `fragments/header.html`.
- Script `seed-data.sql` con 10 eventos de ejemplo.

Lo que **NO está** y forma parte del examen:

- Validaciones en la entidad `Evento` (`@NotBlank`, `@Future`, etc.).
- CRUD completo: CREATE, UPDATE, DELETE.
- Form `eventos/form.html`.
- Modal de Bootstrap para confirmar borrado.
- Toast / flash messages.
- Endpoint con parámetro (filtrar por categoría o búsqueda).
- Botones de acción en cada card del listado.

## Estructura del proyecto

```
eventapp/
├── pom.xml
├── seed-data.sql
├── README.md
├── .gitignore
└── src/main/
    ├── java/com/ufide/eventapp/
    │   ├── EventappApplication.java
    │   ├── controller/
    │   │   ├── HomeController.java
    │   │   └── EventoController.java
    │   ├── entity/Evento.java
    │   ├── repository/EventoRepository.java
    │   └── service/EventoService.java
    └── resources/
        ├── application.properties
        ├── static/css/styles.css
        └── templates/
            ├── home.html
            ├── eventos.html
            ├── evento.html
            └── fragments/header.html
```

## Cómo arrancar

### Pre-requisitos

- Java 25 (JDK)
- Maven 3.9+ (opcional — el `mvnw` viene incluido)
- MySQL 8 + Workbench

### 1. Crear la base de datos

En MySQL Workbench:

```sql
CREATE DATABASE IF NOT EXISTS eventappdb CHARACTER SET utf8mb4;
```

### 2. Configurar la contraseña

```powershell
setx DB_PASSWORD "tu_password_de_mysql"
```

Cerrar terminal y abrir una nueva para que tome efecto.

### 3. Arrancar la app — dos opciones

**Opción A (recomendada en clase): VS Code**

1. Abrí la carpeta `eventapp/` en VS Code.
2. Esperá a que termine de indexar el proyecto (botón "Java Projects" en la barra lateral).
3. Abrí `src/main/java/com/ufide/eventapp/EventappApplication.java`.
4. Hacé clic en el botón **Run** (▷) que aparece sobre `public static void main`.
5. Esperá a ver en la consola integrada `Started EventappApplication`.

**Opción B: terminal con Maven Wrapper**

```bash
./mvnw spring-boot:run        # Linux / Mac
.\mvnw.cmd spring-boot:run    # Windows
```

Hibernate crea la tabla `eventos` automáticamente al arrancar (gracias a `ddl-auto=update`).

### 4. Cargar el seed

En MySQL Workbench, abrir `seed-data.sql` y ejecutarlo. Debe insertar 10 eventos.

### 5. Verificar

- http://localhost:8080/ — home con cards informativas.
- http://localhost:8080/eventos — listado con los 10 eventos.
- http://localhost:8080/eventos/1 — detalle del primer evento.

## Documentación del examen

Ver `caso_practico_1.md` (en el zip que descargaste) para:

- Los requisitos completos del examen.
- La rúbrica de evaluación.
- Los deliverables (código + capturas + README de lógica).
- Política anti-plagio.

## Notas técnicas

- `application.properties` usa variables de entorno con defaults.
- `application-local.properties` está en `.gitignore` — se carga automáticamente si existe.
- `ddl-auto=update` deja que Hibernate maneje el esquema. NO escribir `CREATE TABLE` a mano.
- `firebase-key.json` está pre-ignorado por si decidís hacer el bonus de imagen.
