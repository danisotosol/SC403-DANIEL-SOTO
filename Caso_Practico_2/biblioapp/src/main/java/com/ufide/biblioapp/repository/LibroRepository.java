package com.ufide.biblioapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ufide.biblioapp.entity.Libro;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    // Query methods: Spring Data arma el SQL a partir del nombre del metodo.
    List<Libro> findByCategoriaIgnoreCase(String categoria);

    List<Libro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(String titulo, String autor);

    // Categorias distintas para los botones de filtro del catalogo.
    @Query("SELECT DISTINCT l.categoria FROM Libro l ORDER BY l.categoria")
    List<String> listarCategorias();

    // Solo los que tienen al menos un ejemplar libre.
    @Query("SELECT l FROM Libro l WHERE l.ejemplaresDisponibles > 0 ORDER BY l.titulo")
    List<Libro> findDisponibles();
}
