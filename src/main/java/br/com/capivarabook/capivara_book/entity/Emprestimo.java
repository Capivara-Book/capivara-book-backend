package br.com.capivarabook.capivara_book.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "tb_emprestimo")

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emprestimo {

    public static final int LIMITE_RENOVACOES = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_emprestimo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cli", nullable = false)
    private Cliente cliente;

    // Agregação N:1 conforme seção 3.1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_livro", nullable = false)
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_func", nullable = false)
    private Funcionario funcionario;

    @Column(name = "dt_emprestimo", nullable = false)
    private LocalDate dataEmprestimo;

    @Column(name = "dt_prevista_devolucao", nullable = false)
    private LocalDate dataPrevistaDevolucao;

    @Column(name = "dt_devolucao")
    private LocalDate dataDevolucao;

    @Column(name = "renovacoes_realizadas", nullable = false)
    @Builder.Default
    private int renovacoesRealizadas = 0;

    @Column(name = "multa_diaria", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal multaDiaria = new BigDecimal("2.00");

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusEmprestimo status = StatusEmprestimo.ATIVO;

    public boolean isAtrasado() {
        if (this.status == StatusEmprestimo.DEVOLVIDO) return false;
        return LocalDate.now().isAfter(this.dataPrevistaDevolucao);
    }

    public long diasAtraso() {
        if (!isAtrasado()) return 0L;
        return ChronoUnit.DAYS.between(this.dataPrevistaDevolucao, LocalDate.now());
    }

    public boolean podeRenovar() {
        return this.status == StatusEmprestimo.ATIVO && this.renovacoesRealizadas < LIMITE_RENOVACOES;
    }

    public void renovarEmprestimo() {
        if (isAtrasado()) {
            throw new IllegalStateException("Empréstimo em atraso — renovação bloqueada. Dias de atraso: " + diasAtraso());
        }
        if (!podeRenovar()) {
            throw new IllegalStateException("Limite de " + LIMITE_RENOVACOES + " renovações atingido.");
        }
        this.dataPrevistaDevolucao = this.dataPrevistaDevolucao.plusDays(14);
        this.renovacoesRealizadas++;
        this.status = StatusEmprestimo.RENOVADO;
    }

    public BigDecimal calcularMultaAtraso() {
        long dias = diasAtraso();
        if (dias == 0) return BigDecimal.ZERO;
        return this.multaDiaria.multiply(BigDecimal.valueOf(dias));
    }

    public void devolverLivro() {
        if (this.status == StatusEmprestimo.DEVOLVIDO) {
            throw new IllegalStateException("Empréstimo já foi devolvido.");
        }
        this.dataDevolucao = LocalDate.now();
        this.status = StatusEmprestimo.DEVOLVIDO;
    }
}
