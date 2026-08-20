package com.ufide.biblioapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.ufide.biblioapp.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
// Interruptor general de la autorizacion por metodo. Sin esta anotacion, los
// @PreAuthorize de los controllers se ignoran EN SILENCIO: la app arranca
// igual y un LECTOR podria crear libros.
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // BCrypt: hash distinto cada vez (salt aleatorio) pero matches() siempre
    // valida. Nunca comparar passwords con equals().
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // Se expone el AuthenticationManager interno para poder autenticar a mano
    // en AuthController y emitir un JWT.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ==========================================================
    // CADENA 1 - API REST (/api/**)
    // ==========================================================
    // Va PRIMERO (@Order(1)) y con securityMatcher: solo atiende /api/**.
    // Existe separada de la cadena web por una razon concreta: un cliente de
    // API espera codigos HTTP, no redirecciones a una pantalla de login. Con
    // una sola cadena compartida, un request sin token terminaba en 302 hacia
    // /login y un LECTOR sin permiso en 302 hacia /403 - inutil para Postman.
    // Aca se fuerza 401 (no autenticado) y 403 (autenticado sin rol).
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .cors(cors -> {})
            // CSRF protege formularios con cookie de sesion. La API se
            // autentica con un Bearer token y no usa sesion, asi que no
            // aplica; dejarlo activo rechazaria todo POST/PUT/DELETE.
            .csrf(csrf -> csrf.disable())
            // STATELESS: no se crea JSESSIONID. Cada request se autentica
            // solo con su token.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Hace falta poder pedir un token sin tener uno todavia.
                .requestMatchers("/api/auth/login").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                // Sin token o token invalido -> 401.
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                // Token valido pero rol insuficiente -> 403 con cuerpo vacio.
                .accessDeniedHandler((req, res, e) -> res.sendError(HttpStatus.FORBIDDEN.value()))
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // ==========================================================
    // CADENA 2 - Vistas Thymeleaf (todo lo demas)
    // ==========================================================
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // /error incluido: cuando un handler lanza una excepcion,
                // Tomcat re-despacha el request a /error. Si esa ruta exige
                // sesion, el 400/500 real se convierte en un 302 al login y
                // el cliente nunca ve el codigo verdadero.
                .requestMatchers("/", "/login", "/403", "/error", "/css/**", "/img/**").permitAll()
                // Todo lo demas exige sesion. La restriccion POR ROL vive en
                // los @PreAuthorize de los controllers, no aca: asi la misma
                // regla aplica a las vistas y a la API.
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/libros", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Cuando @PreAuthorize bloquea a un usuario ya logueado, Spring
            // lanza AccessDeniedException; esto la convierte en la pagina
            // propia /403 en vez del error blanco de Tomcat.
            .exceptionHandling(ex -> ex.accessDeniedPage("/403"))
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );
        return http.build();
    }
}
