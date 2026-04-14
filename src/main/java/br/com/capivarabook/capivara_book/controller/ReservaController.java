package br.com.capivarabook.capivara_book.controller;

import br.com.capivarabook.capivara_book.dto.response.ReservaResponseDTO;
import br.com.capivarabook.capivara_book.entity.StatusReserva;
import br.com.capivarabook.capivara_book.service.ReservaService;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController {
    private final ReservaService reservaService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservaResponseDTO> listarTodas() {
        return reservaService.listarTodas();
    }

    @GetMapping("/pendentes")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservaResponseDTO> listarPendentes() {
        return reservaService.listarPendentes();
    }

    @GetMapping("/cliente/{clienteId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservaResponseDTO> listarPorCliente(@PathVariable Long clienteId) {
        return reservaService.listarPorCliente(clienteId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponseDTO reservarLivro(@RequestBody Map<String, Long> body) {
        return reservaService.reservarLivro(
                body.get("clienteId"),
                body.get("livroId"));
    }

    @PatchMapping("/{id}/cancelar")
    @ResponseStatus(HttpStatus.OK)
    public ReservaResponseDTO cancelarReserva(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        return reservaService.cancelarReserva(id, body.get("clienteId"));
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ReservaResponseDTO atualizarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long funcId = Long.valueOf(body.get("funcionarioId").toString());
        StatusReserva novoStatus =
                StatusReserva.valueOf(body.get("status").toString().toUpperCase());
        return reservaService.atualizarStatus(id, funcId, novoStatus);
    }

    @PostMapping("/expirar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void expirarReservasVencidas() {
        reservaService.expirarReservasVencidas();
    }
}
