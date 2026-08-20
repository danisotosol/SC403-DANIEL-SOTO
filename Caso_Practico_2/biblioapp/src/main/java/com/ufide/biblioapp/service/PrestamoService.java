package com.ufide.biblioapp.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ufide.biblioapp.entity.Libro;
import com.ufide.biblioapp.entity.Prestamo;
import com.ufide.biblioapp.repository.LibroRepository;
import com.ufide.biblioapp.repository.PrestamoRepository;

@Service
public class PrestamoService {

    // Plazo estandar de la biblioteca cuando el formulario no manda una
    // fecha de devolucion esperada.
    public static final int DIAS_PLAZO = 14;

    @Autowired
    private PrestamoRepository repo;

    @Autowired
    private LibroRepository libroRepo;

    public List<Prestamo> listar() {
        return repo.findAllConLibroYUsuario();
    }

    public List<Prestamo> listarActivos() {
        return repo.findActivos();
    }

    public List<Prestamo> listarDeUsuario(String username) {
        return repo.findByUsername(username);
    }

    public Optional<Prestamo> buscarPorId(Long id) {
        return repo.findByIdConRelaciones(id);
    }

    // Los atrasados se calculan SIEMPRE contra la fecha de hoy, y el filtro
    // corre en la base de datos via JPQL (ver PrestamoRepository.prestamosAtrasados).
    public List<Prestamo> listarAtrasados() {
        return repo.prestamosAtrasados(LocalDate.now());
    }

    public long contarAtrasados() {
        return repo.contarAtrasados(LocalDate.now());
    }

    // ===== Registrar un prestamo =====
    // @Transactional: guardar el prestamo y descontar el ejemplar tienen que
    // pasar juntos o no pasar. Sin la transaccion, un error en el segundo
    // paso dejaria el prestamo creado con el inventario descuadrado.
    @Transactional
    public Prestamo registrar(Prestamo prestamo) {
        Libro libro = libroRepo.findById(prestamo.getLibro().getId())
                .orElseThrow(() -> new IllegalArgumentException("El libro no existe"));

        if (libro.getEjemplaresDisponibles() <= 0) {
            throw new IllegalStateException("No hay ejemplares disponibles de \"" + libro.getTitulo() + "\"");
        }

        if (repo.contarActivosDe(prestamo.getUsuario().getId(), libro.getId()) > 0) {
            throw new IllegalStateException("Ese usuario ya tiene un prestamo activo de este libro");
        }

        if (prestamo.getFechaPrestamo() == null) {
            prestamo.setFechaPrestamo(LocalDate.now());
        }
        if (prestamo.getFechaDevolucionEsperada() == null) {
            prestamo.setFechaDevolucionEsperada(prestamo.getFechaPrestamo().plusDays(DIAS_PLAZO));
        }
        if (prestamo.getFechaDevolucionEsperada().isBefore(prestamo.getFechaPrestamo())) {
            throw new IllegalArgumentException("La fecha de devolucion no puede ser anterior a la del prestamo");
        }

        // Un prestamo recien creado nunca nace devuelto.
        prestamo.setFechaDevolucionReal(null);
        prestamo.setLibro(libro);

        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() - 1);
        libroRepo.save(libro);

        return repo.save(prestamo);
    }

    // ===== Registrar la devolucion =====
    // Idempotente: si el prestamo ya estaba devuelto, no vuelve a sumar un
    // ejemplar (si no, un doble clic infla el inventario).
    @Transactional
    public Prestamo devolver(Long prestamoId) {
        Prestamo prestamo = repo.findById(prestamoId)
                .orElseThrow(() -> new IllegalArgumentException("El prestamo no existe"));

        if (prestamo.getFechaDevolucionReal() != null) {
            throw new IllegalStateException("Ese prestamo ya fue devuelto el " + prestamo.getFechaDevolucionReal());
        }

        prestamo.setFechaDevolucionReal(LocalDate.now());

        Libro libro = prestamo.getLibro();
        int nuevoDisponible = Math.min(libro.getEjemplaresDisponibles() + 1, libro.getEjemplaresTotales());
        libro.setEjemplaresDisponibles(nuevoDisponible);
        libroRepo.save(libro);

        return repo.save(prestamo);
    }

    // Al borrar un prestamo activo, el ejemplar vuelve al inventario.
    @Transactional
    public void eliminar(Long id) {
        Prestamo prestamo = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El prestamo no existe"));

        if (prestamo.getFechaDevolucionReal() == null) {
            Libro libro = prestamo.getLibro();
            libro.setEjemplaresDisponibles(
                    Math.min(libro.getEjemplaresDisponibles() + 1, libro.getEjemplaresTotales()));
            libroRepo.save(libro);
        }
        repo.delete(prestamo);
    }
}
