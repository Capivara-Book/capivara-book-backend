package br.com.capivarabook.capivara_book.dto.response;

import br.com.capivarabook.capivara_book.entity.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservaResponseDTO {
    private Long id;
    private Long clienteId;
    private String clienteNome;
    private Long livroId;
    private String livroTitulo;
    private LocalDate dataReserva;
    private LocalDate dataExpiracao;
    private String status;
    private boolean expirada;

    public static ReservaResponseDTO from(Reserva reserva) {
        return ReservaResponseDTO.builder()
            .id(reserva.getId())
            .clienteId(reserva.getCliente().getId())
            .clienteNome(reserva.getCliente().getNome())
            .livroId(reserva.getLivro().getId())
            .livroTitulo(reserva.getLivro().getTitulo())
            .dataReserva(reserva.getDataReserva())
            .dataExpiracao(reserva.getDataExpiracao())
            .status(reserva.getStatus().name())
            .expirada(reserva.isExpiracao())
            .build();
    }
}
