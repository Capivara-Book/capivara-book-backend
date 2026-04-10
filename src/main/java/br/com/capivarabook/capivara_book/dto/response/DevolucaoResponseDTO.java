package br.com.capivarabook.capivara_book.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DevolucaoResponseDTO {
    private EmprestimoResponseDTO emprestimo;
    private BigDecimal multaGerada;
    private String mensagem;
}
