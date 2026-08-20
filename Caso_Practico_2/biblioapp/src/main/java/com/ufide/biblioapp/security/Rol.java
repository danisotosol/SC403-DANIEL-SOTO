package com.ufide.biblioapp.security;

// Roles del dominio de BiblioApp como enum, en vez de strings sueltos
// repetidos en @PreAuthorize, seed-data.sql y formularios.
//
// BIBLIOTECARIO: administra el catalogo (crear/editar/eliminar libros),
//                registra prestamos y devoluciones, y ve los atrasados.
// LECTOR:        solo consulta el catalogo y sus propios prestamos.
//
// Es el tipo del campo Usuario.rol (@Enumerated(EnumType.STRING)), asi que
// un rol invalido no compila y no puede entrar a la base por JPA.
// UsuarioService.rolDesdeTexto() convierte lo que llega de afuera (formulario
// o JSON) y RolRestController publica estos valores como JSON.
//
// Lo unico que el enum NO cubre: dentro de una expresion SpEL
// (@PreAuthorize("hasRole('BIBLIOTECARIO')")) el rol sigue siendo texto.
public enum Rol {
    BIBLIOTECARIO,
    LECTOR
}
