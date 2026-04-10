package br.com.capivarabook.capivara_book.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RelatorioResponseDTO {
    private long totalLivros;
    private long totalClientes;
    private long emprestimosAtivos;
    private long emprestimosEmAtraso;
    private long reservasPendentes;
}
