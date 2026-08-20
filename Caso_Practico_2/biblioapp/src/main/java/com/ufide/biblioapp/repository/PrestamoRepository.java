package com.ufide.biblioapp.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ufide.biblioapp.entity.Prestamo;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    // ==========================================================
    // CONSULTA JPQL PROPIA DEL CASO 2: prestamos ATRASADOS
    // ==========================================================
    // Un prestamo esta atrasado cuando se cumplen LAS DOS condiciones:
    //   1. fechaDevolucionReal IS NULL  -> el libro todavia no volvio.
    //      Si tuviera fecha real, el prestamo ya esta cerrado y no importa
    //      que se haya entregado tarde: hoy no hay nada que reclamar.
    //   2. fechaDevolucionEsperada < :hoy -> la fecha limite ya paso.
    //      Se usa "<" y no "<=" a proposito: si vence HOY todavia esta en
    //      plazo, el usuario tiene todo el dia para devolverlo.
    //
    // La fecha se pasa como parametro (:hoy) en vez de usar CURRENT_DATE
    // para que la consulta sea testeable con una fecha fija y para que la
    // decision de "que dia es hoy" viva en el service, no en la base.
    //
    // JOIN FETCH l y u: libro y usuario son LAZY. Sin el fetch, la vista y
    // el JSON dispararian una consulta extra por cada prestamo (problema
    // N+1) o directamente fallarian al serializar fuera de la transaccion.
    // Con el fetch, todo viene en UN solo SELECT con dos JOIN.
    //
    // ORDER BY ascendente: primero el atraso mas viejo, que es el que la
    // biblioteca tiene que cobrar primero.
    @Query("""
           SELECT p FROM Prestamo p
           JOIN FETCH p.libro l
           JOIN FETCH p.usuario u
           WHERE p.fechaDevolucionReal IS NULL
             AND p.fechaDevolucionEsperada < :hoy
           ORDER BY p.fechaDevolucionEsperada ASC
           """)
    List<Prestamo> prestamosAtrasados(@Param("hoy") LocalDate hoy);

    // Variante de conteo de la misma consulta, para el badge del listado y el
    // resumen de la API. Mismas dos condiciones, sin JOIN FETCH: un COUNT no
    // necesita materializar las entidades relacionadas.
    @Query("""
           SELECT COUNT(p) FROM Prestamo p
           WHERE p.fechaDevolucionReal IS NULL
             AND p.fechaDevolucionEsperada < :hoy
           """)
    long contarAtrasados(@Param("hoy") LocalDate hoy);

    // Listado general con las dos relaciones resueltas (evita N+1 en la tabla).
    @Query("SELECT p FROM Prestamo p JOIN FETCH p.libro JOIN FETCH p.usuario ORDER BY p.fechaPrestamo DESC")
    List<Prestamo> findAllConLibroYUsuario();

    // Un prestamo puntual con sus relaciones ya cargadas.
    @Query("SELECT p FROM Prestamo p JOIN FETCH p.libro JOIN FETCH p.usuario WHERE p.id = :id")
    Optional<Prestamo> findByIdConRelaciones(@Param("id") Long id);

    // Prestamos activos (todavia afuera), sin importar si estan atrasados.
    @Query("""
           SELECT p FROM Prestamo p
           JOIN FETCH p.libro JOIN FETCH p.usuario
           WHERE p.fechaDevolucionReal IS NULL
           ORDER BY p.fechaDevolucionEsperada ASC
           """)
    List<Prestamo> findActivos();

    // "Mis prestamos": lo que ve un LECTOR de si mismo.
    @Query("""
           SELECT p FROM Prestamo p
           JOIN FETCH p.libro JOIN FETCH p.usuario u
           WHERE u.username = :username
           ORDER BY p.fechaPrestamo DESC
           """)
    List<Prestamo> findByUsername(@Param("username") String username);

    // Regla de negocio: un mismo usuario no puede tener dos veces afuera el
    // mismo titulo. Se resuelve con COUNT en la base, no cargando listas.
    @Query("""
           SELECT COUNT(p) FROM Prestamo p
           WHERE p.usuario.id = :usuarioId
             AND p.libro.id = :libroId
             AND p.fechaDevolucionReal IS NULL
           """)
    long contarActivosDe(@Param("usuarioId") Long usuarioId, @Param("libroId") Long libroId);
}
