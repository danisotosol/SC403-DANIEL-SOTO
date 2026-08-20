# Caso Practico 2: BiblioApp

Proyecto en `biblioapp/`, corre en el puerto 8082 (8080 y 8081 los tengo ocupados con los
otros proyectos del curso).

## La entidad Prestamo

Un prestamo pasa entre un libro y una persona, asi que las dos puntas van con `@ManyToOne`
(muchos prestamos apuntan al mismo libro, muchos al mismo usuario). Eso da dos FK,
`libro_id` y `usuario_id`, las dos `nullable = false` porque un prestamo sin libro o sin
dueño no significa nada.

No puse el lado inverso (`@OneToMany` en Libro y Usuario): no lo necesita ninguna pantalla
y una relacion bidireccional que ademas se serializa a JSON se va en recursion infinita
libro -> prestamos -> libro. Cuando quiero los prestamos de alguien los pido con una
consulta.

El estado no es un booleano `devuelto`. La verdad esta en un campo: `fechaDevolucionReal`.
NULL = el libro esta afuera, con fecha = cerrado. Un booleano aparte es dato duplicado que
se desincroniza. Los helpers `isAtrasado()` y `getDiasAtraso()` van `@Transient`: se
calculan en memoria para los badges y el JSON, no son columnas.

`ejemplaresDisponibles` lo mueve solo `PrestamoService` (resta al prestar, suma al
devolver). Por eso `LibroService.guardar()` lo reescribe con el valor de la base al editar:
si el formulario lo pisara, cambiar un titulo descuadraria el inventario. Prestar y
devolver van `@Transactional` porque guardar el prestamo y tocar el inventario tienen que
pasar juntos, y la devolucion revisa si ya tenia fecha real para que un doble clic no
sume ejemplares de la nada.

## Roles y 403

Los dos roles viven en el enum `Rol`, y `UsuarioService.rolValido()` rechaza cualquier otro
valor antes de llegar a la base. Dentro de `@PreAuthorize` el rol sigue siendo un String
(SpEL es texto), asi que el enum no me salva de un typo ahi, si en el resto del codigo.

Criterio: leer es de todos, gestionar es del bibliotecario.

* Sin `@PreAuthorize`: catalogo, detalle y `/prestamos/mios`.
* Con `hasRole('BIBLIOTECARIO')`: crear/editar/borrar libros, registrar prestamos y
  devoluciones, listado completo y atrasados.
* El borrado de libros lo deje con `hasAuthority('ROLE_BIBLIOTECARIO')`: hace lo mismo que
  `hasRole`, que agrega el prefijo `ROLE_` solo. Lo puse asi para tener las dos sintaxis.

Tres cosas que sostienen esto:

1. `@EnableMethodSecurity`. Sin eso los `@PreAuthorize` se ignoran en silencio, sin error
   en ningun log, y un LECTOR crea libros.
2. La regla de rol vive en los metodos, no en la cadena de filtros. Asi la misma regla
   aplica a la vista y al endpoint REST sin duplicarla.
3. `sec:authorize` es cosmetico. Oculta el boton, no protege: quien escriba la URL a mano
   lo para el `@PreAuthorize`.

`accessDeniedPage("/403")` convierte la `AccessDeniedException` en mi pagina. `/403` tiene
que ser publica o se arma un bucle con `/login`. `/error` tambien: cuando un handler tira
excepcion, Tomcat re-despacha ahi, y si pide sesion el 400 real se vuelve un 302 al login.

## API REST

`POST /api/auth/login` reutiliza el `AuthenticationManager` del login por formulario y
devuelve un JWT en vez de crear sesion. `JwtAuthFilter` lo lee del header y arma el
`Authentication`. A `@PreAuthorize` no le importa si vino de cookie o de token.

Separe la seguridad en dos `SecurityFilterChain` porque con una sola compartida un request
sin token respondia 302 al login y un LECTOR sin permiso 302 a `/403`: sirve para el
navegador, es inutil para Postman. La cadena de `/api/**` va con `@Order(1)`, `STATELESS`
(sin JSESSIONID), CSRF apagado (protege formularios con cookie, aca no hay cookie) y
entry point que responde 401 / 403 seco. La otra se queda con las vistas.

Los prestamos salen por `PrestamoDTO`, no por la entidad, por dos motivos: serializar
`Prestamo` arrastra el `Usuario` completo con el hash del password, y el DTO aplana libro
y usuario y ya entrega `diasAtraso` calculado. Igual con la entrada: `POST /api/prestamos`
recibe `libroId` y `usuarioId`, no entidades que el cliente podria inventar. Las reglas de
negocio que rompe el cliente (sin ejemplares, duplicado, devolucion repetida) suben como
excepcion y el controller las traduce a 409, no a un 500.

## La consulta JPQL de atrasados

```java
@Query("""
       SELECT p FROM Prestamo p
       JOIN FETCH p.libro l
       JOIN FETCH p.usuario u
       WHERE p.fechaDevolucionReal IS NULL
         AND p.fechaDevolucionEsperada < :hoy
       ORDER BY p.fechaDevolucionEsperada ASC
       """)
List<Prestamo> findAtrasados(@Param("hoy") LocalDate hoy);
```

Dos condiciones y ninguna sobra:

* `fechaDevolucionReal IS NULL`: el libro no volvio. Si tiene fecha real el prestamo ya
  esta cerrado; se habra entregado tarde, pero hoy no hay nada que reclamar. Sin esta
  condicion la lista se llena de prestamos viejos ya devueltos.
* `fechaDevolucionEsperada < :hoy`: el plazo ya paso. Uso `<` y no `<=` a proposito: si
  vence hoy, el usuario tiene todo el dia, no es atraso. Ese caso borde lo deje cargado en
  el seed para poder mostrarlo.

La fecha entra como `:hoy` en vez de `CURRENT_DATE` para que "que dia es hoy" lo decida el
service y la consulta se pueda probar con una fecha fija.

`JOIN FETCH` y no `JOIN` normal porque libro y usuario son LAZY: la tabla muestra titulo,
autor, nombre y correo, asi que sin el fetch cada fila dispara dos consultas extra (N+1;
con 50 atrasados, 101 consultas). Con `JOIN FETCH` es un solo SELECT con los dos JOIN. Un
`JOIN` sin `FETCH` filtraria igual pero no poblaria las relaciones, o sea seguiria el N+1.

El `ORDER BY ASC` es negocio, no estetica: primero el atraso mas viejo, que es el que hay
que cobrar antes.

Para el badge no necesito las entidades sino el numero, asi que `contarAtrasados` repite
las mismas dos condiciones con `COUNT(p)` y sin fetch. Y el filtro corre en la base, no
con streams sobre `findAll()`: filtrar en Java obliga a traer la tabla entera a memoria
para descartar casi todo.

**Verificacion.** El seed arma los seis casos con fechas relativas a `CURDATE()`: dos
atrasados, uno en plazo, uno que vence hoy, uno devuelto tarde y uno devuelto en plazo. La
consulta devuelve exactamente los dos primeros (26 y 11 dias), igual en la pantalla y en
`GET /api/prestamos/atrasados`. Los otros cuatro quedan afuera por el motivo correcto: el
que vence hoy por el `<`, los devueltos por el `IS NULL`.

## Otros detalles

* `prestamos/form.html` usa `name="libro"` sin `th:field`: el `DomainClassConverter` que
  registra Spring Data convierte el id del select al `Libro` real.
* El combo solo ofrece libros con ejemplares libres, pero la validacion dura sigue en el
  service: el formulario no es frontera de confianza.
* Un solo modal de Bootstrap por listado, con `data-id`/`data-titulo` y `relatedTarget` en
  `show.bs.modal` para rellenarlo antes de abrir.
* `/prestamos/mios` filtra por el `Principal`, nunca por un parametro: si el username
  viniera en la query, cualquiera leeria los prestamos de otro.
* Passwords del seed con BCrypt cost 10, verificados con `matches()` antes de escribirlos.

## Dos bugs que aparecieron al probar

1. La FK `usuario_id` no se creaba (`errno: 150`). El driver MySQL le reporta a Hibernate
   las tablas de todas las bases del servidor; como tengo otra base del curso con tabla
   `usuarios`, Hibernate creyo que ya existia e intento un `ALTER` en vez del `CREATE`. Se
   arregla con `?nullCatalogMeansCurrent=true` en la URL JDBC.
2. `POST /api/libros` tiraba `Cannot map null into type int`. Jackson tomaba el constructor
   de ocho argumentos de `Libro` como creator, y un JSON sin `ejemplaresDisponibles` le
   llegaba con null a un `int`. Deje la entidad con solo el constructor vacio: usa los
   setters y los campos ausentes conservan su default.
