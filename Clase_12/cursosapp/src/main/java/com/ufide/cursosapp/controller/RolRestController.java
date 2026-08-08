package com.ufide.cursosapp.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufide.cursosapp.security.Rol;

// GET /api/roles -> 200 + JSON con los roles disponibles en el sistema,
// leidos directo del enum Rol (una sola fuente de verdad, en vez de tener
// la lista de roles repetida en varios lugares).
//
// Requiere estar autenticado (cualquier JWT valido alcanza - no hace falta
// ser ADMIN).
@RestController
@RequestMapping("/api/roles")
public class RolRestController {

    @GetMapping
    public List<String> listar() {
        return Arrays.stream(Rol.values())
                .map(Enum::name)
                .toList();
    }
}
