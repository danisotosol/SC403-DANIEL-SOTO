package com.ufide.biblioapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufide.biblioapp.security.JwtService;

// Unico endpoint publico de /api/** (junto con el propio login por
// formulario de las vistas HTML, que no toca esta clase). Recibe
// username+password, reutiliza el AuthenticationManager que Spring Security
// ya usa por debajo para el login de sesion, y si las credenciales son
// validas devuelve un JWT en vez de crear una sesion.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    // POST /api/auth/login -> 200 + { "token": "..." }
    // Si las credenciales son invalidas, authenticationManager.authenticate()
    // lanza una excepcion (BadCredentialsException) que Spring Security
    // convierte automaticamente en un 401 - no hace falta capturarla a mano.
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String rol = auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        String token = jwtService.generarToken(request.username(), rol);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    public record LoginRequest(String username, String password) {}

    public record LoginResponse(String token) {}
}
