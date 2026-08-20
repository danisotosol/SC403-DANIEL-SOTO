package com.ufide.biblioapp.security;

// Roles del dominio de BiblioApp como enum, en vez de strings sueltos
// repetidos en @PreAuthorize, seed-data.sql y formularios.
//
// BIBLIOTECARIO: administra el catalogo (crear/editar/eliminar libros),
//                registra prestamos y devoluciones, y ve los atrasados.
// LECTOR:        solo consulta el catalogo y sus propios prestamos.
//
// El enum no evita errores de tipeo dentro de una expresion SpEL
// (@PreAuthorize("hasRole('BIBLIOTECARIO')") sigue siendo un String por
// dentro), pero si es la fuente unica de verdad sobre que roles existen:
// UsuarioService.validarRol() y RolRestController leen de aca.
public enum Rol {
    BIBLIOTECARIO,
    LECTOR
}
