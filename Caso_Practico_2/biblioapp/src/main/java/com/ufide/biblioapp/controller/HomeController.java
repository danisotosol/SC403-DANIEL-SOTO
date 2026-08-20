package com.ufide.biblioapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ufide.biblioapp.service.LibroService;

@Controller
public class HomeController {

    @Autowired
    private LibroService libroService;

    @GetMapping("/")
    public String home(Model modelo,
                       @RequestParam(name = "nombre", defaultValue = "Estudiante") String nombre) {
        modelo.addAttribute("nombre", nombre);
        modelo.addAttribute("totalLibros", libroService.listar().size());
        return "home";
    }

    // La pagina de acceso denegado. Es publica (ver SecurityConfig): si
    // exigiera autenticacion, el redirect de accessDeniedPage entraria en
    // un bucle con /login.
    @GetMapping("/403")
    public String accesoDenegado() {
        return "403";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
