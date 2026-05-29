package br.com.capivarabook.capivara_book.controller;

import br.com.capivarabook.capivara_book.dto.response.DevolucaoResponseDTO;
import br.com.capivarabook.capivara_book.dto.response.EmprestimoResponseDTO;
import br.com.capivarabook.capivara_book.service.EmprestimoService;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/emprestimos")
@RequiredArgsConstructor
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EmprestimoResponseDTO> listarTodos() {
        return emprestimoService.listarTodos();
    }

    @GetMapping("/atraso")
    @ResponseStatus(HttpStatus.OK)
    public List<EmprestimoResponseDTO> listarEmAtraso() {
        return emprestimoService.listarEmAtraso();
    }

    @GetMapping("/cliente/{clienteId}")
    @ResponseStatus(HttpStatus.OK)
    public List<EmprestimoResponseDTO> listarPorCliente(
            @PathVariable Long clienteId,
            Authentication auth) {
        return emprestimoService.listarPorCliente(clienteId, auth);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EmprestimoResponseDTO buscarPorId(@PathVariable Long id) {
        return EmprestimoResponseDTO.from(emprestimoService.buscarEmprestimo(id));
    }

    @GetMapping("/{id}/solicitar")
    @ResponseStatus(HttpStatus.OK)
    public EmprestimoResponseDTO solicitarRenovacao(
            @PathVariable Long id,
            @RequestParam Long clienteId) {
        return emprestimoService.validarSolicitacaoRenovacao(id, clienteId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmprestimoResponseDTO registrarEmprestimo(@RequestBody Map<String, Long> body) {
        return emprestimoService.registrarEmprestimo(
                body.get("clienteId"),
                body.get("livroId"),
                body.get("funcionarioId"));
    }

    @PatchMapping("/{id}/devolver")
    @ResponseStatus(HttpStatus.OK)
    public DevolucaoResponseDTO devolverLivro(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        return emprestimoService.devolverLivro(id, body.get("funcionarioId"));
    }

    @PatchMapping("/{id}/renovar")
    @ResponseStatus(HttpStatus.OK)
    public EmprestimoResponseDTO aprovarRenovacao(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        return emprestimoService.aprovarRenovacao(id, body.get("funcionarioId"));
    }
}
