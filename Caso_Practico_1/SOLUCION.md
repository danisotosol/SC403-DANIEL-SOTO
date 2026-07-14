

# Caso Practico 1 Explicacion

## Endpoints agregados

Aparte de los `GET` de lista y detalle que ya venian puse :

* `GET /eventos/categoria/{categoria}`: Filtro del R3. Usa `@PathVariable`, llama al service y devuelve `eventos.html`. Para no escribir la URL a mano, meti botones dinamicos arriba con las categorias que saca `listarCategorias()` de la base
* `GET /eventos/nuevo`: Manda un `Evento` vacio a `eventos/form.html`
* `POST /eventos`: Recibe el form con `@Valid` y `BindingResult`. Si falla, recarga el form; si no, guarda y redirige
* `GET /eventos/{id}/editar`: Busca el id y lo carga en `form.html`. Si no existe, manda a la lista.
* `POST /eventos/{id}`: El update. Usa `@Valid`. Clavo `evento.setId(id)` con el id de la URL por seguridad, para que nadie altere el campo oculto en el HTML
* `POST /eventos/{id}/eliminar`: Borra por id y redirige


## Validaciones de la entidad

Fui campo por campo para evitar datos que no queremos en en la db:


* `@NotBlank` y `@Size` en `nombre`, `lugar`, `categoria` y `organizador`. El size coincide con el de la bd para que no explote al guardar en MySQL
* `descripcion`: Opcional, pero con `@Size(max=500)`
* `fecha`: `@NotNull` y `@Future` (no tiene sentido meter eventos pasados o de hoy mismo si ya no da tiempo de vender)
* `cupoMaximo`: `@Positive` 1 min
* `cuposVendidos` y `precio`: `@PositiveOrZero` (para permitir eventos gratis o que arranquen de cero).
* En el controller, `@Valid` va pegado aNtes del `@ModelAttribute` y el `BindingResult` inmediatamente despues para que Spring no tire excepcion fea por ahi



## Bootstrap

No use el `confirm()` de JavaScript porque pedia Bootstrap y deje un unico modal en `eventos.html` para no duplicar codigo en cada card.
Cada boton de borrar tiene `data-id` y `data-nombre`. Con un script de JS corto al final, se escucha el evento `show.bs.modal`, lee esos datos del boton que disparo el modal (`relatedTarget`), y se rellena el texto y el action del form (`/eventos/{id}/eliminar`) dinamicamente antes de que abra para q no abra vacio



## Otros

El data input con th:field convierte el LocalDate en automatico 
Uso el mismo form.html para crear y editar revisando si el id es null para cambiar la accion
Las categorias del filtro cargan dinamicas de la db y no quedan quemadas en el HTML
Arquitectura limpia xq el controller solo habla con el service y no toca el repo directo