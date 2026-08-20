package com.ufide.biblioapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufide.biblioapp.entity.Libro;
import com.ufide.biblioapp.repository.LibroRepository;

@Service
public class LibroService {

    @Autowired
    private LibroRepository repo;

    public List<Libro> listar() {
        return repo.findAll();
    }

    public List<Libro> listarDisponibles() {
        return repo.findDisponibles();
    }

    public List<String> listarCategorias() {
        return repo.listarCategorias();
    }

    public List<Libro> buscarPorCategoria(String categoria) {
        return repo.findByCategoriaIgnoreCase(categoria);
    }

    public List<Libro> buscar(String texto) {
        return repo.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(texto, texto);
    }

    public Optional<Libro> buscarPorId(Long id) {
        return repo.findById(id);
    }

    // Al crear un libro nuevo, los ejemplares disponibles arrancan igual a
    // los totales. Al editar uno existente NO se toca el disponible: ese
    // numero lo maneja PrestamoService segun prestamos y devoluciones, y
    // sobreescribirlo desde el formulario descuadraria el inventario.
    public Libro guardar(Libro libro) {
        if (libro.getId() == null) {
            libro.setEjemplaresDisponibles(libro.getEjemplaresTotales());
        } else {
            repo.findById(libro.getId())
                .ifPresent(actual -> libro.setEjemplaresDisponibles(actual.getEjemplaresDisponibles()));
        }
        return repo.save(libro);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
