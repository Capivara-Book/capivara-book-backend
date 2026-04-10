package br.com.capivarabook.capivara_book.dto.response;

import br.com.capivarabook.capivara_book.entity.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmprestimoResponseDTO {
    private Long id;
    private Long clienteId;
    private String clienteNome;
    private Long livroId;
    private String livroTitulo;
    private Long funcionarioId;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevistaDevolucao;
    private LocalDate dataDevolucao;
    private int renovacoesRealizadas;
    private int limiteRenovacoes;
    private String status;
    private boolean atrasado;
    private long diasAtraso;
    private BigDecimal multaDiaria;
    private BigDecimal multaCalculada;

    public static EmprestimoResponseDTO from(Emprestimo emprestimo) {
        return EmprestimoResponseDTO.builder()
            .id(emprestimo.getId())
            .clienteId(emprestimo.getCliente().getId())
            .clienteNome(emprestimo.getCliente().getNome())
            .livroId(emprestimo.getLivro().getId())
            .livroTitulo(emprestimo.getLivro().getTitulo())
            .funcionarioId(emprestimo.getFuncionario().getId())
            .dataEmprestimo(emprestimo.getDataEmprestimo())
            .dataPrevistaDevolucao(emprestimo.getDataPrevistaDevolucao())
            .dataDevolucao(emprestimo.getDataDevolucao())
            .renovacoesRealizadas(emprestimo.getRenovacoesRealizadas())
            .limiteRenovacoes(Emprestimo.LIMITE_RENOVACOES)
            .status(emprestimo.getStatus().name())
            .atrasado(emprestimo.isAtrasado())
            .diasAtraso(emprestimo.diasAtraso())
            .multaDiaria(emprestimo.getMultaDiaria())
            .multaCalculada(emprestimo.calcularMultaAtraso())
            .build();
    }
}
