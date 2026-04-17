package br.com.capivarabook.capivara_book.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


@Entity
@Table(name = "tb_funcionario")

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor

@PrimaryKeyJoinColumn(name = "id_func")
public class Funcionario extends Usuario {

    @NotBlank(message = "Matrícula é obrigatória")
    @Column(name = "matricula", nullable = false, unique = true, length = 9)
    private String matricula;

    @NotNull(message = "Cargo é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "cargo", nullable = false, length = 20)
    protected Cargo cargo;

    public void statusLivro(Livro livro, StatusLivro novoStatus) {
        if (novoStatus == StatusLivro.INDISPONIVEL && livro.getExemplaresDisponiveis() < livro.getExemplares()) {
            throw new IllegalStateException("Não é possível inativar livro com exemplares emprestados.");
        }
        livro.setStatus(novoStatus);
    }

    public Emprestimo registrarEmprestimo(Cliente cliente, Livro livro, int prazoDias, BigDecimal multaDiaria) {
        if (!cliente.podeEmprestar()) {
            throw new IllegalStateException("Cliente atingiu o limite de " + Cliente.LIMITE_EMPRESTIMOS + " empréstimos simultâneos (RN01).");
        }
        if (!livro.isDisponivel()) {
            throw new IllegalStateException("Livro sem exemplares disponíveis.");
        }
        boolean duplicata = cliente.getEmprestimos().stream()
            .anyMatch(e -> e.getLivro().equals(livro)
                && (e.getStatus() == StatusEmprestimo.ATIVO
                || e.getStatus() == StatusEmprestimo.RENOVADO
                || e.getStatus() == StatusEmprestimo.ATRASADO));
        if (duplicata) {
            throw new IllegalStateException("Cliente já possui este livro emprestado.");
        }

        livro.registrarEmprestimo(); // decrementa exemplares_disponiveis

        Emprestimo emp = new Emprestimo();
        emp.setCliente(cliente);
        emp.setLivro(livro);
        emp.setFuncionario(this);
        emp.setDataEmprestimo(LocalDate.now());
        emp.setDataPrevistaDevolucao(LocalDate.now().plusDays(prazoDias));
        emp.setStatus(StatusEmprestimo.ATIVO);
        emp.setMultaDiaria(multaDiaria);
        emp.setRenovacoesRealizadas(0);
        return emp;
    }

    public BigDecimal registrarDevolucao(Emprestimo emprestimo) {
        if (emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
            throw new IllegalStateException("Empréstimo já foi devolvido.");
        }
        BigDecimal multa = emprestimo.calcularMultaAtraso();
        emprestimo.devolverLivro();               // altera status + data
        emprestimo.getLivro().registrarDevolucao(); // incrementa exemplares
        return multa;
    }

    public void statusReserva(Reserva reserva, StatusReserva novoStatus) {
        if (reserva.getStatus() != StatusReserva.PENDENTE) {
            throw new IllegalStateException("Somente reservas PENDENTES podem ser processadas.");
        }
        if (novoStatus != StatusReserva.CONFIRMADO && novoStatus != StatusReserva.RECUSADO) {
            throw new IllegalArgumentException("Status inválido para processamento de reserva.");
        }
        reserva.setStatus(novoStatus);
    }

    public void statusRenovacao(Emprestimo emprestimo) {
        emprestimo.renovarEmprestimo(); // lança exceção se em atraso ou limite atingido
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

