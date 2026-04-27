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

//  SecurityConfig — RBAC baseado no Cargo do Funcionario

//  ROLE_ADMIN       → acesso total ao sistema
//  ROLE_GERENTE     → acesso total + relatório
//  ROLE_COLABORADOR → somente leitura + pré-reservas
//  ROLE_CLIENTE     → catálogo público + pré-reserva + meus dados

//  Autenticação : email + senha (BCrypt) → JWT Bearer
//  Sessão       : STATELESS

//  Mapeamento extraído diretamente dos controllers do projeto:
//    AuthController      → POST /api/v1/auth/login
//    LivroController     → /api/v1/livros/**
//    EmprestimoController→ /api/v1/emprestimos/**
//    ReservaController   → /api/v1/reservas/**
//    UsuarioController   → /api/v1/usuarios/**
//    RelatorioController → /api/v1/relatorio
@Configuration
@EnableWebSecurity
@EnableMethodSecurity       // habilita @PreAuthorize nos controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter            jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        //  PÚBLICO — sem autenticação
                        // AuthController: POST /api/v1/auth/login
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET,"/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/swagger-ui/**").permitAll()

                        // LivroController: GET /api/v1/livros  (catálogo público — só DISPONIVEL)
                        // LivroController: GET /api/v1/livros/{id}
                        .requestMatchers(HttpMethod.GET, "/api/v1/livros").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/livros/{id}").permitAll()

                        //  LIVROS — ADMIN | GERENTE
                        // LivroController: POST /api/v1/livros
                        .requestMatchers(HttpMethod.POST,  "/api/v1/livros")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // LivroController: PUT /api/v1/livros/{id}
                        .requestMatchers(HttpMethod.PUT,   "/api/v1/livros/{id}")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // LivroController: PATCH /api/v1/livros/{id}/status
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/livros/{id}/status")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // LivroController: GET /api/v1/livros/admin  (catálogo completo)
                        .requestMatchers(HttpMethod.GET,   "/api/v1/livros/admin")
                        .hasAnyRole("ADMIN", "GERENTE")

                        //  USUÁRIOS — ADMIN | GERENTE
                        // UsuarioController: GET  /api/v1/usuarios/clientes
                        // UsuarioController: GET  /api/v1/usuarios/clientes/{id}
                        // UsuarioController: POST /api/v1/usuarios/clientes
                        // UsuarioController: PUT  /api/v1/usuarios/clientes/{id}
                        // UsuarioController: DELETE /api/v1/usuarios/clientes/{id} (soft-delete)
                        // UsuarioController: POST /api/v1/usuarios/funcionarios
                        .requestMatchers("/api/v1/usuarios/**")
                        .hasAnyRole("ADMIN", "GERENTE")

                        //  EMPRÉSTIMOS — ADMIN | GERENTE (operações físicas)
                        // EmprestimoController: POST /api/v1/emprestimos
                        .requestMatchers(HttpMethod.POST,  "/api/v1/emprestimos")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // EmprestimoController: PATCH /api/v1/emprestimos/{id}/devolver
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/emprestimos/{id}/devolver")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // EmprestimoController: PATCH /api/v1/emprestimos/{id}/renovar
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/emprestimos/{id}/renovar")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // EmprestimoController: GET /api/v1/emprestimos/atraso
                        .requestMatchers(HttpMethod.GET,   "/api/v1/emprestimos/atraso")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // EmprestimoController: GET /api/v1/emprestimos  (todos)
                        .requestMatchers(HttpMethod.GET,   "/api/v1/emprestimos")
                        .hasAnyRole("ADMIN", "GERENTE")

                        //  EMPRÉSTIMOS — CLIENTE | ADMIN | GERENTE (consultas pessoais)
                        // EmprestimoController: GET /api/v1/emprestimos/{id}
                        .requestMatchers(HttpMethod.GET, "/api/v1/emprestimos/{id}")
                        .hasAnyRole("CLIENTE", "ADMIN", "GERENTE")

                        // EmprestimoController: GET /api/v1/emprestimos/{id}/solicitar?clienteId=
                        .requestMatchers(HttpMethod.GET, "/api/v1/emprestimos/{id}/solicitar")
                        .hasAnyRole("CLIENTE", "ADMIN", "GERENTE")

                        // EmprestimoController: GET /api/v1/emprestimos/cliente/{clienteId}
                        .requestMatchers(HttpMethod.GET, "/api/v1/emprestimos/cliente/{clienteId}")
                        .hasAnyRole("CLIENTE", "ADMIN", "GERENTE")

                        //  RESERVAS — ADMIN | GERENTE (gestão)
                        // ReservaController: GET /api/v1/reservas  (todas)
                        .requestMatchers(HttpMethod.GET,  "/api/v1/reservas")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // ReservaController: GET /api/v1/reservas/pendentes
                        .requestMatchers(HttpMethod.GET,  "/api/v1/reservas/pendentes")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // ReservaController: PATCH /api/v1/reservas/{id}/status
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/reservas/{id}/status")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // ReservaController: POST /api/v1/reservas/expirar
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservas/expirar")
                        .hasAnyRole("ADMIN", "GERENTE")

                        //  RESERVAS — CLIENTE | ADMIN | GERENTE (consultas e ações pessoais)
                        // ReservaController: POST /api/v1/reservas  (pré-reservar)
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservas")
                        .hasAnyRole("CLIENTE", "ADMIN", "GERENTE")

                        // ReservaController: PATCH /api/v1/reservas/{id}/cancelar
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/reservas/{id}/cancelar")
                        .hasAnyRole("CLIENTE", "ADMIN", "GERENTE")

                        // ReservaController: GET /api/v1/reservas/cliente/{clienteId}
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservas/cliente/{clienteId}")
                        .hasAnyRole("CLIENTE", "ADMIN", "GERENTE")

                        //  RELATÓRIO — ADMIN | GERENTE
                        // RelatorioController: GET /api/v1/relatorio
                        .requestMatchers(HttpMethod.GET, "/api/v1/relatorio")
                        .hasAnyRole("ADMIN", "GERENTE")

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
