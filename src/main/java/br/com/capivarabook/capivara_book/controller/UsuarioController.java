package br.com.capivarabook.capivara_book.controller;

import br.com.capivarabook.capivara_book.dto.request.ClienteRequestDTO;
import br.com.capivarabook.capivara_book.dto.request.FuncionarioRequestDTO;
import br.com.capivarabook.capivara_book.dto.response.ClienteResponseDTO;
import br.com.capivarabook.capivara_book.dto.response.FuncionarioResponseDTO;
import br.com.capivarabook.capivara_book.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @PostMapping("/api/v1/usuarios/funcionarios")
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionarioResponseDTO cadastrarFuncionario(@Valid @RequestBody FuncionarioRequestDTO req) {
        return usuarioService.cadastrarFuncionario(req);
    }

    @PostMapping("/api/v1/usuarios/clientes")
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponseDTO cadastrarCliente(@Valid @RequestBody ClienteRequestDTO req) {
        return usuarioService.cadastrarCliente(req);
    }

    @GetMapping("/api/v1/usuarios/clientes")
    @ResponseStatus(HttpStatus.OK)
    public List<ClienteResponseDTO> listarClientes() {
        return usuarioService.listarClientes();
    }

    @GetMapping("/api/v1/usuarios/clientes/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ClienteResponseDTO buscarCliente(@PathVariable Long id) {
        return usuarioService.buscarClientePorId(id);
    }

    @PutMapping("/api/v1/usuarios/clientes/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ClienteResponseDTO atualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO req) {
        return usuarioService.atualizarCliente(id, req);
    }

    @DeleteMapping("/api/v1/usuarios/clientes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerCliente(@PathVariable Long id) {
        usuarioService.removerCliente(id);
    }

    @PostMapping("/api/v1/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        boolean ok = usuarioService.login(body.get("email"), body.get("senha"));
        if (!ok) {
            throw new BusinessException(
                    "Credenciais inválidas.");
        }
        return Map.of("autenticado", true, "mensagem", "Login realizado com sucesso.");
    }
}
