package com.ufide.cursosapp.security;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

// Genera y valida los JWT que usa la API REST en vez de una sesion. Un JWT
// es, en el fondo, tres partes separadas por puntos (header.payload.signature)
// codificadas en Base64 - la firma es lo que garantiza que nadie modifico el
// contenido sin conocer la clave secreta.
@Component
public class JwtService {

    // Minimo 32 caracteres (256 bits) para el algoritmo HS256 - jjwt tira
    // WeakKeyException si es mas corta. Ya viene configurada en
    // application.properties (app.jwt.secret) con un valor de desarrollo.
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Arma un token con el username como "subject" y el rol como claim
    // extra - asi el filtro puede reconstruir un Authentication completo
    // sin volver a consultar la base de datos en cada request.
    public String generarToken(String username, String rol) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(key())
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public String extraerRol(String token) {
        return extraerClaim(token, claims -> claims.get("rol", String.class));
    }

    // Un token es valido si el username coincide Y todavia no vencio.
    public boolean esValido(String token, String username) {
        try {
            String usernameDelToken = extraerUsername(token);
            return usernameDelToken.equals(username) && !estaExpirado(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean estaExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
