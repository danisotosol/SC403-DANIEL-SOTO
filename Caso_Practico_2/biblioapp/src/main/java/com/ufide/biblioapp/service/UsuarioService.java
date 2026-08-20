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

    // El campo de la entidad ya es del tipo Rol, asi que un valor invalido no
    // puede llegar por Java. Esto valida lo que entra de AFUERA como texto
    // (un select de formulario, un POST JSON hecho a mano): devuelve el enum
    // correspondiente o vacio si ese rol no existe.
    public Optional<Rol> rolDesdeTexto(String rol) {
        if (rol == null) {
            return Optional.empty();
        }
        return Arrays.stream(Rol.values())
                .filter(r -> r.name().equalsIgnoreCase(rol.trim()))
                .findFirst();
    }

    // Hashea el password solo si viene en texto plano (creacion, o cambio
    // explicito). Si el formulario de edicion lo manda vacio, se conserva el
    // hash que ya estaba guardado.
    public Usuario guardar(Usuario usuario) {
        if (usuario.getRol() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
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
