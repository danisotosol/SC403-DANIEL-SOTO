package com.ufide.biblioapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import com.ufide.biblioapp.entity.Libro;
import com.ufide.biblioapp.service.LibroService;

@Controller
@RequestMapping("/libros")
public class LibroController {

    @Autowired
    private LibroService libroService;

    // ===== Lectura: cualquier usuario autenticado (BIBLIOTECARIO o LECTOR) =====
    @GetMapping
    public String listar(Model modelo,
                         @RequestParam(name = "categoria", required = false) String categoria,
                         @RequestParam(name = "q", required = false) String q) {
        if (categoria != null && !categoria.isBlank()) {
            modelo.addAttribute("libros", libroService.buscarPorCategoria(categoria));
        } else if (q != null && !q.isBlank()) {
            modelo.addAttribute("libros", libroService.buscar(q));
        } else {
            modelo.addAttribute("libros", libroService.listar());
        }
        modelo.addAttribute("categorias", libroService.listarCategorias());
        modelo.addAttribute("categoriaActiva", categoria);
        modelo.addAttribute("q", q);
        return "libros";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("libro", libroService.buscarPorId(id).orElse(null));
        return "libro";
    }

    // ===== Escritura: solo BIBLIOTECARIO =====
    // hasRole('BIBLIOTECARIO') exige la authority "ROLE_BIBLIOTECARIO"; el
    // prefijo ROLE_ lo agrega Spring solo (CustomUserDetailsService usa
    // .roles(...)). Si entra un LECTOR, AccessDeniedException -> /403.
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping("/nuevo")
    public String mostrarFormNuevo(Model modelo) {
        modelo.addAttribute("libro", new Libro());
        return "libros/form";
    }

    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @PostMapping
    public String crear(@Valid @ModelAttribute("libro") Libro libro,
                        BindingResult result,
                        RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "libros/form";
        }
        libroService.guardar(libro);
        ra.addFlashAttribute("ok", "Libro agregado al catalogo");
        return "redirect:/libros";
    }

    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping("/{id}/editar")
    public String mostrarFormEditar(@PathVariable Long id, Model modelo) {
        Libro libro = libroService.buscarPorId(id).orElse(null);
        if (libro == null) {
            return "redirect:/libros";
        }
        modelo.addAttribute("libro", libro);
        return "libros/form";
    }

    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("libro") Libro libro,
                             BindingResult result,
                             RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "libros/form";
        }
        // El id manda la URL, no el campo oculto del HTML: asi nadie puede
        // editar otro registro alterando el form.
        libro.setId(id);
        libroService.guardar(libro);
        ra.addFlashAttribute("ok", "Libro actualizado");
        return "redirect:/libros";
    }

    // hasAuthority('ROLE_BIBLIOTECARIO') es equivalente a hasRole('BIBLIOTECARIO');
    // se deja la forma explicita aca para comparar las dos sintaxis.
    @PreAuthorize("hasAuthority('ROLE_BIBLIOTECARIO')")
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            libroService.eliminar(id);
            ra.addFlashAttribute("ok", "Libro eliminado del catalogo");
        } catch (Exception e) {
            // Si el libro tiene prestamos, la FK de la tabla prestamos impide
            // borrarlo: se avisa en vez de mostrar el stacktrace.
            ra.addFlashAttribute("error", "No se puede eliminar: el libro tiene prestamos registrados");
        }
        return "redirect:/libros";
    }
}
