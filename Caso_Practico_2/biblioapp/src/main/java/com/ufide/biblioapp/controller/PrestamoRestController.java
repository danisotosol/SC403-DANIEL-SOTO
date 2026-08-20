package com.ufide.biblioapp.controller;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ufide.biblioapp.dto.PrestamoDTO;
import com.ufide.biblioapp.entity.Libro;
import com.ufide.biblioapp.entity.Prestamo;
import com.ufide.biblioapp.entity.Usuario;
import com.ufide.biblioapp.service.PrestamoService;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoRestController {

    @Autowired
    private PrestamoService prestamoService;

    // GET /api/prestamos -> todos los prestamos (solo BIBLIOTECARIO).
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping
    public List<PrestamoDTO> listar() {
        return prestamoService.listar().stream().map(PrestamoDTO::de).toList();
    }

    // GET /api/prestamos/atrasados -> ENDPOINT PEDIDO POR EL CASO 2.
    // Detras corre la JPQL propia PrestamoRepository.prestamosAtrasados(:hoy):
    // fechaDevolucionReal IS NULL AND fechaDevolucionEsperada < hoy.
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping("/atrasados")
    public List<PrestamoDTO> atrasados() {
        return prestamoService.listarAtrasados().stream().map(PrestamoDTO::de).toList();
    }

    // GET /api/prestamos/atrasados/resumen -> conteo para dashboards.
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping("/atrasados/resumen")
    public Map<String, Object> resumenAtrasados() {
        return Map.of(
                "fecha", LocalDate.now(),
                "totalAtrasados", prestamoService.contarAtrasados());
    }

    // GET /api/prestamos/mios -> los del usuario del token, sea del rol que
    // sea. El username sale del Principal, no del cliente.
    @GetMapping("/mios")
    public List<PrestamoDTO> mios(Principal principal) {
        return prestamoService.listarDeUsuario(principal.getName()).stream().map(PrestamoDTO::de).toList();
    }

    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping("/{id}")
    public ResponseEntity<PrestamoDTO> detalle(@PathVariable Long id) {
        return prestamoService.buscarPorId(id)
                .map(PrestamoDTO::de)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/prestamos
    // Body: { "libroId": 1, "usuarioId": 3, "fechaPrestamo": "2026-08-19",
    //         "fechaDevolucionEsperada": "2026-09-02" }
    // Las dos ultimas son opcionales: si faltan, el service pone hoy y hoy+14.
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody NuevoPrestamoRequest request) {
        if (request.libroId() == null || request.usuarioId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "libroId y usuarioId son obligatorios"));
        }

        Prestamo prestamo = new Prestamo();
        Libro libro = new Libro();
        libro.setId(request.libroId());
        prestamo.setLibro(libro);

        Usuario usuario = new Usuario();
        usuario.setId(request.usuarioId());
        prestamo.setUsuario(usuario);

        prestamo.setFechaPrestamo(request.fechaPrestamo());
        prestamo.setFechaDevolucionEsperada(request.fechaDevolucionEsperada());

        try {
            Prestamo guardado = prestamoService.registrar(prestamo);
            PrestamoDTO dto = prestamoService.buscarPorId(guardado.getId())
                    .map(PrestamoDTO::de)
                    .orElse(null);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(guardado.getId())
                    .toUri();
            return ResponseEntity.created(location).body(dto);
        } catch (RuntimeException e) {
            // Reglas de negocio violadas (sin ejemplares, duplicado, fechas)
            // -> 409 Conflict con el motivo, no un 500.
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/prestamos/{id}/devolver -> cierra el prestamo y devuelve el
    // ejemplar al inventario.
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @PostMapping("/{id}/devolver")
    public ResponseEntity<?> devolver(@PathVariable Long id) {
        try {
            Prestamo devuelto = prestamoService.devolver(id);
            return ResponseEntity.ok(prestamoService.buscarPorId(devuelto.getId())
                    .map(PrestamoDTO::de)
                    .orElse(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    // Request de entrada: se reciben ids sueltos y no entidades completas
    // para que el cliente no pueda mandar un libro/usuario inventado ni
    // pisar campos que no le corresponden.
    public record NuevoPrestamoRequest(Long libroId,
                                       Long usuarioId,
                                       LocalDate fechaPrestamo,
                                       LocalDate fechaDevolucionEsperada) {}
}
