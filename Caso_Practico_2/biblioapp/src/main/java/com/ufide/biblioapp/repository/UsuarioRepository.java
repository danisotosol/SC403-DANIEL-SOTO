package com.ufide.biblioapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ufide.biblioapp.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Lo usa CustomUserDetailsService para el login (form y JWT).
    Optional<Usuario> findByUsername(String username);
}
