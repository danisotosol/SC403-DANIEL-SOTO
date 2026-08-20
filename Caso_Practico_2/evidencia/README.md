# Capturas de evidencia: Caso Practico 2

Tomadas con Chrome headless contra la app en http://localhost:8082 con el seed cargado.

| Archivo | Que muestra |
|---------|-------------|
| `01-login.png` | pantalla de login con los usuarios de prueba |
| `02-catalogo-bibliotecario.png` | `/libros` como `biblio`: botones de gestion visibles |
| `03-detalle-libro.png` | detalle de un libro con ejemplares totales y disponibles |
| `04-form-libro.png` | formulario de alta de libro |
| `05-form-prestamo.png` | formulario de prestamo (combo con solo libros disponibles) |
| `06-prestamos.png` | listado con los badges En plazo / Atrasado / Devuelto |
| `07-atrasados.png` | `/prestamos/atrasados`: la consulta JPQL en pantalla |
| `08-catalogo-lector.png` | `/libros` como `lector`: sin botones ni menus de gestion |
| `09-mis-prestamos.png` | `/prestamos/mios` filtrado por el Principal |
| `10-403-acceso-denegado.png` | `lector` entrando a `/libros/nuevo` a mano: 403 |
| `11-403-atrasados.png` | `lector` entrando a `/prestamos/atrasados`: 403 |

Faltan las de Postman (`POST /api/auth/login` con el token, `GET /api/prestamos/atrasados`
con el JSON y un `POST /api/libros` con token de LECTOR devolviendo 403). Esas hay que
sacarlas desde Postman con la coleccion importada.
