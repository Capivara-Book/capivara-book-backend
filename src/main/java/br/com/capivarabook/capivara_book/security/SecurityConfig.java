package br.com.capivarabook.capivara_book.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// ── SecurityConfig — RBAC por Cargo ───────────────────────────
//
//  ROLE_ADMIN      → acesso total
//  ROLE_GERENTE    → acesso total + relatórios
//  ROLE_COLABORADOR→ consultas, pré-reservas (sem registrar empréstimo)
//  ROLE_CLIENTE    → catálogo, pré-reserva, meus empréstimos, renovação
//
// Autenticação: email + senha → JWT → Bearer token
// Sessão: STATELESS (sem HttpSession)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // habilita @PreAuthorize nos controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter       jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Endpoints públicos ─────────────────────────
                .requestMatchers("/api/v1/auth/login").permitAll()

                // ── Catálogo público (sem autenticação) ────────
                .requestMatchers(HttpMethod.GET, "/api/v1/livros").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/livros/{id}").permitAll()

                // ── Somente ADMIN e GERENTE ────────────────────
                // Gestão do acervo
                .requestMatchers(HttpMethod.POST,  "/api/v1/livros").hasAnyRole("ADMIN","GERENTE")
                .requestMatchers(HttpMethod.PUT,   "/api/v1/livros/{id}").hasAnyRole("ADMIN","GERENTE")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/livros/{id}/status").hasAnyRole("ADMIN","GERENTE")
                .requestMatchers(HttpMethod.GET,   "/api/v1/livros/admin").hasAnyRole("ADMIN","GERENTE")

                // Gestão de usuários
                .requestMatchers("/api/v1/usuarios/**").hasAnyRole("ADMIN","GERENTE")

                // Operações de empréstimo (somente Admin/Gerente registram fisicamente)
                .requestMatchers(HttpMethod.POST,  "/api/v1/emprestimos").hasAnyRole("ADMIN","GERENTE")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/emprestimos/{id}/devolver").hasAnyRole("ADMIN","GERENTE")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/emprestimos/{id}/renovar").hasAnyRole("ADMIN","GERENTE")
                .requestMatchers(HttpMethod.GET,   "/api/v1/emprestimos/atraso").hasAnyRole("ADMIN","GERENTE")
                .requestMatchers(HttpMethod.GET,   "/api/v1/emprestimos").hasAnyRole("ADMIN","GERENTE")

                // Gestão de reservas (confirmar/recusar)
                .requestMatchers(HttpMethod.PATCH, "/api/v1/reservas/{id}/status").hasAnyRole("ADMIN","GERENTE")
                .requestMatchers(HttpMethod.GET,   "/api/v1/reservas/pendentes").hasAnyRole("ADMIN","GERENTE")
                .requestMatchers(HttpMethod.GET,   "/api/v1/reservas").hasAnyRole("ADMIN","GERENTE")
                .requestMatchers(HttpMethod.POST,  "/api/v1/reservas/expirar").hasAnyRole("ADMIN","GERENTE")

                // Relatório (somente GERENTE e ADMIN)
                .requestMatchers("/api/v1/relatorio").hasAnyRole("ADMIN","GERENTE")

                // ── CLIENTE ────────────────────────────────────
                // Solicitar renovação, ver próprios empréstimos e reservas
                .requestMatchers(HttpMethod.GET,  "/api/v1/emprestimos/{id}/solicitar").hasAnyRole("CLIENTE","ADMIN","GERENTE")
                .requestMatchers(HttpMethod.GET,  "/api/v1/emprestimos/cliente/**").hasAnyRole("CLIENTE","ADMIN","GERENTE")
                .requestMatchers(HttpMethod.POST, "/api/v1/reservas").hasAnyRole("CLIENTE","ADMIN","GERENTE")
                .requestMatchers(HttpMethod.PATCH,"/api/v1/reservas/{id}/cancelar").hasAnyRole("CLIENTE","ADMIN","GERENTE")
                .requestMatchers(HttpMethod.GET,  "/api/v1/reservas/cliente/**").hasAnyRole("CLIENTE","ADMIN","GERENTE")

                // Qualquer outra rota precisa de autenticação
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
