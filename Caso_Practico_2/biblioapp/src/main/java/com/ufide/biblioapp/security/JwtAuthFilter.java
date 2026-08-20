package com.ufide.biblioapp.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Filtro que corre UNA VEZ por request, ANTES de que Spring Security decida
// si autoriza o no. Si viene un header "Authorization: Bearer <token>"
// valido, arma un Authentication "a mano" y lo deja en el SecurityContext -
// es el equivalente, sin sesion, de lo que formLogin() hace con la cookie de
// sesion para las vistas HTML.
//
// Importante: este filtro es ADITIVO. Si no viene header Authorization (o
// no es valido), simplemente deja pasar el request sin tocar nada - no
// rompe el login por formulario de las vistas HTML.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            String username = jwtService.extraerUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.esValido(token, username)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token corrupto, vencido o de un usuario que ya no existe: lo
            // dejamos pasar sin autenticar - si el endpoint requiere estar
            // autenticado, la falta de Authentication resulta en 401/403
            // mas adelante en la cadena.
        }

        filterChain.doFilter(request, response);
    }
}
