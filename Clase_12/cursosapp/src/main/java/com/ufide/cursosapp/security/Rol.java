package com.ufide.cursosapp.security;

// Constantes de rol como enum, en vez de strings sueltos ("ADMIN", "USER")
// repetidos a mano en @PreAuthorize, seed-data.sql, formularios, etc.
//
// Un enum no evita errores de tipeo dentro de una expresion SpEL
// (@PreAuthorize("hasRole('ADMIN')") sigue siendo, por dentro, un String) -
// pero sí evita errores de tipeo en cualquier lugar de Java que use
// Rol.ADMIN en vez de escribir "ADMIN" a mano, y sirve como fuente unica de
// verdad sobre que roles existen en el sistema. Ver RolRestController
// (expone estos valores via API) y UsuarioService.validarRol() (los usa
// para validar datos que entran desde un formulario o un POST JSON).
public enum Rol {
    ADMIN,
    USER
}
