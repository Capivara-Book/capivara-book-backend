package br.com.capivarabook.capivara_book.controller;

import br.com.capivarabook.capivara_book.dto.request.ClienteRequestDTO;
import br.com.capivarabook.capivara_book.dto.request.FuncionarioRequestDTO;
import br.com.capivarabook.capivara_book.dto.response.ClienteResponseDTO;
import br.com.capivarabook.capivara_book.dto.response.FuncionarioResponseDTO;
import br.com.capivarabook.capivara_book.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/api/v1/usuarios/clientes")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public List<ClienteResponseDTO> listarClientes() {
        return usuarioService.listarClientes();
    }

    @GetMapping("/api/v1/usuarios/clientes/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ClienteResponseDTO buscarCliente(@PathVariable Long id) {
        return usuarioService.buscarClientePorId(id);
    }

    @PostMapping("/api/v1/usuarios/clientes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ClienteResponseDTO cadastrarCliente(@Valid @RequestBody ClienteRequestDTO req) {
        return usuarioService.cadastrarCliente(req);
    }

    @PutMapping("/api/v1/usuarios/clientes/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ClienteResponseDTO atualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO req) {
        return usuarioService.atualizarCliente(id, req);
    }

    @DeleteMapping("/api/v1/usuarios/clientes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public void inativarCliente(@PathVariable Long id) {
        usuarioService.inativarCliente(id);
    }

    @GetMapping("/api/v1/usuarios/funcionarios")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public List<FuncionarioResponseDTO> listarFuncionarios() {
        return usuarioService.listarFuncionarios();
    }

    @PostMapping("/api/v1/usuarios/funcionarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public FuncionarioResponseDTO cadastrarFuncionario(@Valid @RequestBody FuncionarioRequestDTO req) {
        return usuarioService.cadastrarFuncionario(req);
    }

    @DeleteMapping("/api/v1/usuarios/funcionarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void inativarFuncionario(@PathVariable Long id) {
        usuarioService.inativarFuncionario(id);
    }
}
