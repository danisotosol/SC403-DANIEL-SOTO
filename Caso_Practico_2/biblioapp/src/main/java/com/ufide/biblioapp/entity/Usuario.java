package com.ufide.biblioapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.ufide.biblioapp.security.Rol;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El username es obligatorio")
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Nunca en texto plano: siempre pasa por BCryptPasswordEncoder
    // (ver UsuarioService.guardar y seed-data.sql).
    @NotBlank(message = "El password es obligatorio")
    @Column(nullable = false)
    private String password;

    // El rol es el enum Rol, no un String. @Enumerated(EnumType.STRING)
    // guarda el NOMBRE ("BIBLIOTECARIO", "LECTOR") en la columna, no el
    // ordinal: con ORDINAL, agregar o reordenar un valor del enum cambiaria
    // el significado de las filas que ya estan en la base.
    //
    // Al tipar el campo con el enum, un rol invalido no compila y tampoco
    // puede entrar por JPA: la unica frontera que queda por validar es lo
    // que llega de afuera (formulario o JSON), y de eso se ocupa
    // UsuarioService.rolDesdeTexto().
    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100)
    private String nombreCompleto;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato valido")
    private String email;

    public Usuario() {
    }

    public Usuario(Long id, String username, String password, Rol rol,
                   String nombreCompleto, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
