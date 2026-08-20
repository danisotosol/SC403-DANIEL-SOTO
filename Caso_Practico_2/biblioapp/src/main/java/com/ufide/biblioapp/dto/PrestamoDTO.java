package com.ufide.biblioapp.dto;

import java.time.LocalDate;

import com.ufide.biblioapp.entity.Prestamo;

// DTO de salida para la API REST. Existe por dos razones concretas:
//
// 1. SEGURIDAD: si se serializara la entidad Prestamo tal cual, el JSON
//    arrastraria el objeto Usuario completo, incluido el hash del password.
//    El DTO expone solo username y nombre.
// 2. FORMA DEL JSON: aplana libro y usuario en campos simples y agrega
//    diasAtraso ya calculado, en vez de obligar al cliente a navegar
//    objetos anidados y hacer cuentas de fechas.
public record PrestamoDTO(
        Long id,
        Long libroId,
        String titulo,
        String autor,
        String isbn,
        String usuario,
        String nombreUsuario,
        String email,
        LocalDate fechaPrestamo,
        LocalDate fechaDevolucionEsperada,
        LocalDate fechaDevolucionReal,
        boolean devuelto,
        boolean atrasado,
        long diasAtraso) {

    public static PrestamoDTO de(Prestamo p) {
        return new PrestamoDTO(
                p.getId(),
                p.getLibro().getId(),
                p.getLibro().getTitulo(),
                p.getLibro().getAutor(),
                p.getLibro().getIsbn(),
                p.getUsuario().getUsername(),
                p.getUsuario().getNombreCompleto(),
                p.getUsuario().getEmail(),
                p.getFechaPrestamo(),
                p.getFechaDevolucionEsperada(),
                p.getFechaDevolucionReal(),
                p.isDevuelto(),
                p.isAtrasado(),
                p.getDiasAtraso());
    }
}
