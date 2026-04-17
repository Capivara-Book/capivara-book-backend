package br.com.capivarabook.capivara_book.service;

import br.com.capivarabook.capivara_book.dto.request.LoginRequestDTO;
import br.com.capivarabook.capivara_book.dto.response.LoginResponseDTO;
import br.com.capivarabook.capivara_book.entity.*;
import br.com.capivarabook.capivara_book.exception.BusinessException;
import br.com.capivarabook.capivara_book.repository.*;
import br.com.capivarabook.capivara_book.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ── AuthService ────────────────────────────────────────────────
// Responsável pelo login com email + senha.
// Determina a role a partir do tipo de usuário:
//   Funcionario → "ROLE_" + cargo.name()   (ex: ROLE_ADMIN)
//   Cliente     → "ROLE_CLIENTE"
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final IFuncionarioRepository funcionarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO req) {

        // Spring Security valida senha com BCrypt e chama isEnabled()
        // que verifica statusUsuario != INATIVO
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getSenha()));
        } catch (DisabledException e) {
            throw new BusinessException("Usuário inativo. Entre em contato com a biblioteca.");
        } catch (BadCredentialsException e) {
            throw new BusinessException("E-mail ou senha inválidos.");
        }

        Usuario usuario = usuarioRepository.findByEmail(req.getEmail()).orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        String roleDisplay;
        String cargoDisplay;

        if (funcionarioRepository.existsByUsuarioId(usuario.getId())) {
            Funcionario func = funcionarioRepository.findById(usuario.getId()).orElseThrow(() -> new RuntimeException("Funcionário não encontrado com o ID: " + usuario.getId()));;
            roleDisplay  = "ADMIN";
            cargoDisplay = func.getCargo().name();
        } else {
            roleDisplay  = "CLIENTE";
            cargoDisplay = "CLIENTE";
        }

        String token;
        token = jwtUtil.gerarToken(usuario.getId(), usuario.getEmail(), usuario.getRole().name());

        return LoginResponseDTO.builder()
            .token(token)
            .tipo("Bearer")
            .id(usuario.getId())
            .nome(usuario.getNome())
            .email(usuario.getEmail())
            .role(roleDisplay)
            .cargo(cargoDisplay)
            .build();
    }
}
