package com.ufide.eventapp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ufide.eventapp.entity.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    /** Devuelve eventos filtrados por categoria. */
    List<Evento> findByCategoria(String categoria);

    /** Devuelve eventos cuya fecha sea posterior o igual a la fecha dada. */
    List<Evento> findByFechaGreaterThanEqualOrderByFechaAsc(LocalDate fecha);
}
