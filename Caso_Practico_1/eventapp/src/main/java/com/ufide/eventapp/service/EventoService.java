package com.ufide.eventapp.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufide.eventapp.entity.Evento;
import com.ufide.eventapp.repository.EventoRepository;

@Service
public class EventoService {

    @Autowired
    private EventoRepository repo;

    public List<Evento> listar() {
        return repo.findAll();
    }

    public Optional<Evento> buscarPorId(Long id) {
        return repo.findById(id);
    }

    public Evento guardar(Evento e) {
        return repo.save(e);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public List<Evento> buscarPorCategoria(String categoria) {
        return repo.findByCategoria(categoria);
    }

    public List<Evento> buscarProximos() {
        return repo.findByFechaGreaterThanEqualOrderByFechaAsc(LocalDate.now());
    }

    /** Categorias distintas presentes en la BD, para armar el filtro del listado. */
    public List<String> listarCategorias() {
        return repo.findAll().stream()
                .map(Evento::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .toList();
    }
}
