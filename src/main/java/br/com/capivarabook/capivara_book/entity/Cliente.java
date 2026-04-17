package br.com.capivarabook.capivara_book.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "cliente")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name = "id_cli")
public class Cliente extends Usuario {

    public static final int LIMITE_EMPRESTIMOS = 3;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos numéricos")
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos")
    @Column(name = "telefone", nullable = false, unique = true, length = 11)
    private String telefone;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Emprestimo> emprestimos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();

    public boolean podeEmprestar() {
        long ativos = emprestimos.stream()
            .filter(e -> e.getStatus() == StatusEmprestimo.ATIVO
                    || e.getStatus() == StatusEmprestimo.ATRASADO
                    || e.getStatus() == StatusEmprestimo.RENOVADO)
            .count();
        return ativos < LIMITE_EMPRESTIMOS;
    }

    public void solicitarRenovacao(Emprestimo emprestimo) {
        if (!emprestimo.podeRenovar()) {
            throw new IllegalStateException(
                "Limite de " + Emprestimo.LIMITE_RENOVACOES + " renovações atingido para este empréstimo.");
        }
    }

    public void cancelarReserva(Reserva reserva) {
        if (reserva.getStatus() != StatusReserva.PENDENTE) {
            throw new IllegalStateException("Somente reservas PENDENTES podem ser canceladas.");
        }
        reserva.setStatus(StatusReserva.CANCELADO);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }
}
