package com.ufide.biblioapp.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import jakarta.validation.constraints.NotNull;

// Entidad central del Caso Practico 2. Un Prestamo es la relacion entre UN
// libro y UN usuario en una fecha dada, asi que las dos puntas se modelan
// con @ManyToOne (muchos prestamos apuntan al mismo libro; muchos prestamos
// apuntan al mismo usuario). En la base esto se traduce en dos columnas FK:
// libro_id y usuario_id.
//
// No se modela el lado inverso (@OneToMany en Libro/Usuario) a proposito:
// no hace falta para ninguna pantalla de la app y una coleccion bidireccional
// invita a problemas de serializacion en la API REST (recursion infinita
// libro -> prestamos -> libro -> ...).
@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El libro es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull(message = "La fecha de prestamo es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaPrestamo;

    // Fecha en la que el libro DEBERIA volver. Es la que se compara contra
    // hoy en la consulta JPQL de atrasados.
    @NotNull(message = "La fecha de devolucion esperada es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaDevolucionEsperada;

    // NULL = el libro todavia esta afuera. Cuando el bibliotecario registra
    // la devolucion se le pone la fecha real. Este campo es el que distingue
    // un prestamo "activo" de uno "cerrado" - no hay booleano redundante.
    private LocalDate fechaDevolucionReal;

    public Prestamo() {
    }

    public Prestamo(Long id, Libro libro, Usuario usuario, LocalDate fechaPrestamo,
                    LocalDate fechaDevolucionEsperada, LocalDate fechaDevolucionReal) {
        this.id = id;
        this.libro = libro;
        this.usuario = usuario;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    // ===== Estado derivado =====
    // @Transient: se calcula en memoria, NO es una columna. Sirve para las
    // vistas y para el JSON de la API, pero la deteccion de atrasados de
    // verdad se hace en JPQL (PrestamoRepository.findAtrasados) para no
    // traer toda la tabla a Java y filtrarla aca.

    @Transient
    public boolean isDevuelto() {
        return fechaDevolucionReal != null;
    }

    @Transient
    public boolean isAtrasado() {
        return fechaDevolucionReal == null
                && fechaDevolucionEsperada != null
                && fechaDevolucionEsperada.isBefore(LocalDate.now());
    }

    // Dias de atraso a hoy; 0 si no esta atrasado. Se usa en la tabla de
    // atrasados y en el JSON de /api/prestamos/atrasados.
    @Transient
    public long getDiasAtraso() {
        if (!isAtrasado()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(fechaDevolucionEsperada, LocalDate.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaDevolucionEsperada() { return fechaDevolucionEsperada; }
    public void setFechaDevolucionEsperada(LocalDate fechaDevolucionEsperada) { this.fechaDevolucionEsperada = fechaDevolucionEsperada; }

    public LocalDate getFechaDevolucionReal() { return fechaDevolucionReal; }
    public void setFechaDevolucionReal(LocalDate fechaDevolucionReal) { this.fechaDevolucionReal = fechaDevolucionReal; }
}
