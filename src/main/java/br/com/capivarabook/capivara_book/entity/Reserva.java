package br.com.capivarabook.capivara_book.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "tb_reserva")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cli", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_livro", nullable = false)
    private Livro livro;

    @Column(name = "dt_reserva", nullable = false)
    private LocalDate dataReserva;

    @Column(name = "dt_expiracao", nullable = false)
    private LocalDate dataExpiracao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusReserva status = StatusReserva.PENDENTE;

    public void statusReserva(StatusReserva novoStatus) {
        this.status = novoStatus;
    }

    public boolean isExpiracao() {
        if (this.status == StatusReserva.CANCELADO || this.status == StatusReserva.EXPIRADO) {
            return false;
        }
        return LocalDate.now().isAfter(this.dataExpiracao);
    }
}
