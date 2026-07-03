package com.ufide.tiendaapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Entidad Producto - representa la tabla productos en MySQL.
 *
 * Anotaciones JPA:
 *   @Entity        -> marca la clase como tabla
 *   @Table         -> nombre custom de la tabla
 *   @Id            -> llave primaria
 *   @GeneratedValue -> auto-incremento
 *   @Column        -> configuracion de la columna
 */
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 500)
    @Column(length = 500)
    private String descripcion;

    @Positive(message = "El precio debe ser mayor a 0")
    @Column(nullable = false)
    private double precio;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private int stock;

    @Size(max = 50)
    @Column(length = 50)
    private String categoria;

    /** Constructor vacio - obligatorio para JPA. */
    public Producto() {}

    public Producto(String nombre, String descripcion, double precio, int stock, String categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    /** Util: marca si hay poco stock. */
    public boolean isBajoStock() {
        return stock > 0 && stock < 5;
    }

    /** Util: marca si esta agotado. */
    public boolean isAgotado() {
        return stock == 0;
    }
}
