package com.ufide.biblioapp.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ufide.biblioapp.entity.Usuario;
import com.ufide.biblioapp.repository.UsuarioRepository;
import com.ufide.biblioapp.security.Rol;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> listar() {
        return repo.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return repo.findById(id);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return repo.findByUsername(username);
    }

    public List<Rol> rolesDisponibles() {
        return Arrays.asList(Rol.values());
    }

    // Un rol que no este en el enum no entra a la base: si alguien manda
    // "ADMIN" desde un POST hecho a mano, se rechaza aca.
    public boolean rolValido(String rol) {
        if (rol == null) {
            return false;
        }
        return Arrays.stream(Rol.values()).anyMatch(r -> r.name().equals(rol));
    }

    // Hashea el password solo si viene en texto plano (creacion, o cambio
    // explicito). Si el formulario de edicion lo manda vacio, se conserva el
    // hash que ya estaba guardado.
    public Usuario guardar(Usuario usuario) {
        if (!rolValido(usuario.getRol())) {
            throw new IllegalArgumentException("Rol invalido: " + usuario.getRol());
        }
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            Usuario actual = repo.findById(usuario.getId()).orElseThrow();
            usuario.setPassword(actual.getPassword());
        } else if (!usuario.getPassword().startsWith("$2")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return repo.save(usuario);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
