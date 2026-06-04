package br.com.capivarabook.capivara_book.security;

import br.com.capivarabook.capivara_book.config.CorsConfig;
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
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter             jwtAuthFilter;
    private final CustomUserDetailsService  userDetailsService;
    private final CorsConfigurationSource   corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        //  PÚBLICO — sem autenticação
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/swagger-ui/**").permitAll()

                        //  LIVROS — totalmente público para testes
                        .requestMatchers("/api/v1/livros/**").permitAll()

                        //  USUÁRIOS — ADMIN | GERENTE
                        .requestMatchers(HttpMethod.GET,    "/api/v1/usuarios/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.POST,   "/api/v1/usuarios/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/usuarios/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.PATCH,  "/api/v1/usuarios/**").hasAnyRole("ADMIN", "GERENTE")

                        //  EMPRÉSTIMOS — ADMIN | GERENTE (operações)
                        .requestMatchers(HttpMethod.POST,  "/api/v1/emprestimos").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/emprestimos/{id}/devolver").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/emprestimos/{id}/renovar").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/emprestimos/atraso").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/emprestimos").hasAnyRole("ADMIN", "GERENTE")

                        //  EMPRÉSTIMOS — CLIENTE | ADMIN | GERENTE (consultas pessoais)
                        .requestMatchers(HttpMethod.GET, "/api/v1/emprestimos/{id}").hasAnyRole("CLIENTE", "ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/emprestimos/cliente/{clienteId}").hasAnyRole("CLIENTE", "ADMIN", "GERENTE")

                        //  RESERVAS — ADMIN | GERENTE (gestão)
                        .requestMatchers(HttpMethod.GET,   "/api/v1/reservas").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/reservas/pendentes").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/reservas/{id}/status").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.POST,  "/api/v1/reservas/expirar").hasAnyRole("ADMIN", "GERENTE")

                        //  RESERVAS — CLIENTE | ADMIN | GERENTE (ações pessoais)
                        .requestMatchers(HttpMethod.POST,  "/api/v1/reservas").hasAnyRole("CLIENTE", "ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/reservas/{id}/cancelar").hasAnyRole("CLIENTE", "ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/reservas/cliente/{clienteId}").hasAnyRole("CLIENTE", "ADMIN", "GERENTE")

                        //  RELATÓRIO — ADMIN | GERENTE
                        .requestMatchers(HttpMethod.GET, "/api/v1/relatorio").hasAnyRole("ADMIN", "GERENTE")

                        //  QUALQUER OUTRA ROTA — autenticado
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
