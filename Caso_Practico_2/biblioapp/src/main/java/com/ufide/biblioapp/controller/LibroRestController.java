package com.ufide.biblioapp.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

import com.ufide.biblioapp.entity.Libro;
import com.ufide.biblioapp.service.LibroService;

// @RestController = @Controller + @ResponseBody: cada retorno se serializa
// directo a JSON con Jackson, no se busca una vista Thymeleaf.
@RestController
@RequestMapping("/api/libros")
public class LibroRestController {

    @Autowired
    private LibroService libroService;

    // GET /api/libros            -> catalogo completo
    // GET /api/libros?categoria= -> filtrado por categoria
    // GET /api/libros?disponibles=true -> solo con ejemplares libres
    @GetMapping
    public List<Libro> listar(@RequestParam(required = false) String categoria,
                              @RequestParam(required = false, defaultValue = "false") boolean disponibles) {
        if (categoria != null && !categoria.isBlank()) {
            return libroService.buscarPorCategoria(categoria);
        }
        return disponibles ? libroService.listarDisponibles() : libroService.listar();
    }

    // GET /api/libros/{id} -> 200 + libro, o 404 si no existe.
    @GetMapping("/{id}")
    public ResponseEntity<Libro> detalle(@PathVariable Long id) {
        return libroService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/libros -> 201 Created + header Location + el libro creado.
    // Mismo @PreAuthorize que la vista HTML: la regla de negocio no se
    // duplica ni cambia segun el canal. Al filtro JWT no le importa si el
    // Authentication vino de una cookie de sesion o de un Bearer token.
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @PostMapping
    public ResponseEntity<Libro> crear(@Valid @RequestBody Libro libro) {
        Libro guardado = libroService.guardar(libro);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(guardado.getId())
                .toUri();
        return ResponseEntity.created(location).body(guardado);
    }

    // PUT /api/libros/{id} -> 200 + actualizado, 404 si no existe.
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @PutMapping("/{id}")
    public ResponseEntity<Libro> actualizar(@PathVariable Long id, @Valid @RequestBody Libro libro) {
        if (libroService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        libro.setId(id);
        return ResponseEntity.ok(libroService.guardar(libro));
    }

    // DELETE /api/libros/{id} -> 204 si se borro, 404 si no existia.
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (libroService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
