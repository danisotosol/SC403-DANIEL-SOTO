package com.ufide.biblioapp.controller;

import java.security.Principal;
import java.time.LocalDate;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import com.ufide.biblioapp.entity.Prestamo;
import com.ufide.biblioapp.service.LibroService;
import com.ufide.biblioapp.service.PrestamoService;
import com.ufide.biblioapp.service.UsuarioService;

@Controller
@RequestMapping("/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private LibroService libroService;

    @Autowired
    private UsuarioService usuarioService;

    // Listado completo: solo el BIBLIOTECARIO ve los prestamos de todos.
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping
    public String listar(Model modelo) {
        modelo.addAttribute("prestamos", prestamoService.listar());
        modelo.addAttribute("totalAtrasados", prestamoService.contarAtrasados());
        return "prestamos/lista";
    }

    // "Mis prestamos": cualquier autenticado, pero solo los propios. El
    // username sale del Principal (la sesion o el JWT), nunca de un
    // parametro de la URL, para que nadie pueda espiar los de otro.
    @GetMapping("/mios")
    public String mios(Model modelo, Principal principal) {
        modelo.addAttribute("prestamos", prestamoService.listarDeUsuario(principal.getName()));
        return "prestamos/mios";
    }

    // Pantalla de atrasados (requisito del caso): consume la JPQL propia.
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping("/atrasados")
    public String atrasados(Model modelo) {
        modelo.addAttribute("prestamos", prestamoService.listarAtrasados());
        modelo.addAttribute("hoy", LocalDate.now());
        return "prestamos/atrasados";
    }

    // ===== Registrar prestamo =====
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping("/nuevo")
    public String mostrarFormNuevo(Model modelo) {
        Prestamo prestamo = new Prestamo();
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucionEsperada(LocalDate.now().plusDays(PrestamoService.DIAS_PLAZO));
        modelo.addAttribute("prestamo", prestamo);
        cargarCombos(modelo);
        return "prestamos/form";
    }

    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @PostMapping
    public String registrar(@Valid @ModelAttribute("prestamo") Prestamo prestamo,
                            BindingResult result,
                            Model modelo,
                            RedirectAttributes ra) {
        if (result.hasErrors()) {
            cargarCombos(modelo);
            return "prestamos/form";
        }
        try {
            prestamoService.registrar(prestamo);
            ra.addFlashAttribute("ok", "Prestamo registrado");
        } catch (RuntimeException e) {
            // Las reglas de negocio (sin ejemplares, prestamo duplicado,
            // fechas invertidas) llegan como excepcion desde el service y
            // se muestran como alerta, no como error 500.
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/prestamos";
    }

    // ===== Devolucion =====
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @PostMapping("/{id}/devolver")
    public String devolver(@PathVariable Long id, RedirectAttributes ra) {
        try {
            prestamoService.devolver(id);
            ra.addFlashAttribute("ok", "Devolucion registrada");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/prestamos";
    }

    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            prestamoService.eliminar(id);
            ra.addFlashAttribute("ok", "Prestamo eliminado");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/prestamos";
    }

    // Solo se ofrecen libros con ejemplares libres: la validacion dura vive
    // igual en el service, esto solo evita el error obvio en pantalla.
    private void cargarCombos(Model modelo) {
        modelo.addAttribute("libros", libroService.listarDisponibles());
        modelo.addAttribute("usuarios", usuarioService.listar());
    }
}
