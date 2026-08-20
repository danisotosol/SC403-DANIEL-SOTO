package com.ufide.biblioapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 150, message = "El titulo no puede tener mas de 150 caracteres")
    @Column(nullable = false, length = 150)
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 100, message = "El autor no puede tener mas de 100 caracteres")
    @Column(nullable = false, length = 100)
    private String autor;

    // ISBN unico: no puede haber dos fichas de catalogo para el mismo codigo.
    @NotBlank(message = "El ISBN es obligatorio")
    @Size(max = 20, message = "El ISBN no puede tener mas de 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @NotBlank(message = "La categoria es obligatoria")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String categoria;

    @Min(value = 1450, message = "El anio de publicacion debe ser posterior a 1450")
    @Max(value = 2100, message = "El anio de publicacion no puede ser tan futuro")
    private int anioPublicacion;

    // Cuantos ejemplares tiene la biblioteca en total.
    @Min(value = 1, message = "Debe haber al menos 1 ejemplar")
    private int ejemplaresTotales;

    // Cuantos estan libres para prestar ahora mismo. PrestamoService lo baja
    // en 1 al registrar un prestamo y lo sube en 1 al registrar la devolucion,
    // asi que nunca se edita a mano desde el formulario de libros.
    @PositiveOrZero(message = "Los ejemplares disponibles no pueden ser negativos")
    private int ejemplaresDisponibles;

    // Solo constructor vacio + setters, a proposito. Esta entidad se recibe
    // como @RequestBody en la API REST y Jackson toma un constructor de
    // varios argumentos como "creator": ahi, cualquier campo ausente en el
    // JSON llega como null y explota al asignarlo a un int primitivo
    // ("Cannot map null into type int"). Con solo el constructor vacio,
    // Jackson usa los setters y los campos ausentes conservan su default.
    public Libro() {
    }

    // Helper para las vistas: th:if="${libro.disponible}".
    public boolean isDisponible() {
        return ejemplaresDisponibles > 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getAnioPublicacion() { return anioPublicacion; }
    public void setAnioPublicacion(int anioPublicacion) { this.anioPublicacion = anioPublicacion; }

    public int getEjemplaresTotales() { return ejemplaresTotales; }
    public void setEjemplaresTotales(int ejemplaresTotales) { this.ejemplaresTotales = ejemplaresTotales; }

    public int getEjemplaresDisponibles() { return ejemplaresDisponibles; }
    public void setEjemplaresDisponibles(int ejemplaresDisponibles) { this.ejemplaresDisponibles = ejemplaresDisponibles; }
}
