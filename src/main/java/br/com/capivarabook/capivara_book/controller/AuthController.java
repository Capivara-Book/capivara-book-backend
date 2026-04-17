package br.com.capivarabook.capivara_book.controller;

import br.com.capivarabook.capivara_book.dto.request.LoginRequestDTO;
import br.com.capivarabook.capivara_book.dto.response.LoginResponseDTO;
import br.com.capivarabook.capivara_book.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// ── AuthController ─────────────────────────────────────────────
// POST /api/v1/auth/login → retorna JWT com role baseada no Cargo
// Endpoint público (permitAll no SecurityConfig)
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO req) {
        return authService.login(req);
    }
}

