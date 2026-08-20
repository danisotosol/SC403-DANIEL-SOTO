package com.ufide.biblioapp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufide.biblioapp.security.Rol;

// Expone el enum Rol como JSON: ["BIBLIOTECARIO","LECTOR"]. Sirve para que
// un cliente (o el formulario de usuarios) arme el combo sin quemar los
// valores a mano.
@RestController
@RequestMapping("/api/roles")
public class RolRestController {

    @GetMapping
    public List<Rol> listar() {
        return List.of(Rol.values());
    }
}
