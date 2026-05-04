package br.com.capivarabook.capivara_book;

import br.com.capivarabook.capivara_book.entity.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

// Testa a lógica isolada da entidade Cliente
@DisplayName("Cliente — Testes Unitários")
class ClienteTest {

    // ── Fábrica de cliente limpo ───────────────────────────────
    private Cliente clienteVazio() {
        Cliente c = new Cliente();
        c.setNome("Maria Silva");
        c.setEmail("maria.silva@email.com");
        c.setSenha("senha123");
        c.setCpf("12345678901");
        c.setTelefone("11987654321");
        c.setStatus(StatusUsuario.ATIVO);
        c.setRole(Role.CLIENTE);
        return c;
    }

    // ── Fábrica de Livro helper ───────────────────────────────
    private Livro livro(String isbn) {
        return Livro.builder()
                .titulo("Livro Teste").autor("Autor").editora("Ed")
                .isbn(isbn).anoPublicacao(2020).genero(Genero.ROMANCE)
                .exemplares(5).exemplaresDisponiveis(3)
                .status(StatusLivro.DISPONIVEL).build();
    }

    // ── Emprestimo helper ─────────────────────────────────────
    private Emprestimo emprestimoAtivo(Cliente c, Livro l) {
        return Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(3))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(11))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00"))
                .build();
    }

    private Emprestimo emprestimoDevolvido(Cliente c, Livro l) {
        return Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(20))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(6))
                .dataDevolucao(LocalDate.now().minusDays(5))
                .status(StatusEmprestimo.DEVOLVIDO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00"))
                .build();
    }

    // ══════════════════════════════════════════════════════════
    //  podeEmprestar() — RN01
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("podeEmprestar() → true quando cliente não tem nenhum empréstimo")
    void podeEmprestar_deveRetornarTrue_quandoSemEmprestimos() {
        // PDF seção 6.5: "Verifica que podeEmprestar() retorna verdadeiro
        // quando o cliente possui menos de 3 empréstimos ativos"
        Cliente c = clienteVazio();
        assertTrue(c.podeEmprestar());
    }

    @Test
    @DisplayName("podeEmprestar() → true com 1 empréstimo ativo")
    void podeEmprestar_deveRetornarTrue_comUmAtivo() {
        Cliente c = clienteVazio();
        Livro l = livro("1111111111111");
        c.setEmprestimos(List.of(emprestimoAtivo(c, l)));

        assertTrue(c.podeEmprestar());
    }

    @Test
    @DisplayName("podeEmprestar() → true com 2 empréstimos ativos")
    void podeEmprestar_deveRetornarTrue_comDoisAtivos() {
        Cliente c = clienteVazio();
        Livro l1 = livro("1111111111111");
        Livro l2 = livro("2222222222222");
        c.setEmprestimos(List.of(
                emprestimoAtivo(c, l1),
                emprestimoAtivo(c, l2)
        ));

        assertTrue(c.podeEmprestar());
    }

    @Test
    @DisplayName("podeEmprestar() → false quando cliente tem 3 empréstimos ativos (RN01)")
    void podeEmprestar_deveRetornarFalse_quandoNoLimite() {
        // PDF seção 6.5: "Verifica que podeEmprestar() retorna falso quando
        // o cliente já atingiu o limite de 3 empréstimos simultâneos"
        Cliente c = clienteVazio();
        Livro l1 = livro("1111111111111");
        Livro l2 = livro("2222222222222");
        Livro l3 = livro("3333333333333");
        c.setEmprestimos(List.of(
                emprestimoAtivo(c, l1),
                emprestimoAtivo(c, l2),
                emprestimoAtivo(c, l3)
        ));

        assertFalse(c.podeEmprestar());
        assertEquals(3, Cliente.LIMITE_EMPRESTIMOS);
    }

    @Test
    @DisplayName("podeEmprestar() → true quando empréstimos DEVOLVIDOS não contam para o limite")
    void podeEmprestar_naoContaDevolvidos() {
        Cliente c = clienteVazio();
        Livro l1 = livro("1111111111111");
        Livro l2 = livro("2222222222222");
        Livro l3 = livro("3333333333333");
        // 3 ativos + 2 devolvidos — devolvidos NÃO devem contar
        c.setEmprestimos(List.of(
                emprestimoAtivo(c, l1),
                emprestimoAtivo(c, l2),
                emprestimoAtivo(c, l3),
                emprestimoDevolvido(c, livro("4444444444444")),
                emprestimoDevolvido(c, livro("5555555555555"))
        ));

        // Com 3 ativos, deve ser false
        assertFalse(c.podeEmprestar());
    }

    @Test
    @DisplayName("podeEmprestar() → true quando todos os empréstimos estão DEVOLVIDOS")
    void podeEmprestar_deveRetornarTrue_quandoTodosDevolvidos() {
        Cliente c = clienteVazio();
        c.setEmprestimos(List.of(
                emprestimoDevolvido(c, livro("1111111111111")),
                emprestimoDevolvido(c, livro("2222222222222")),
                emprestimoDevolvido(c, livro("3333333333333"))
        ));
        // Histórico cheio de devolvidos — cliente pode emprestar
        assertTrue(c.podeEmprestar());
    }

    @Test
    @DisplayName("podeEmprestar() → false conta empréstimos ATRASADO e RENOVADO como ativos")
    void podeEmprestar_contaAtrasadoERenovado() {
        Cliente c = clienteVazio();

        Emprestimo atrasado = Emprestimo.builder()
                .cliente(c).livro(livro("1111111111111"))
                .dataEmprestimo(LocalDate.now().minusDays(30))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(16))
                .status(StatusEmprestimo.ATRASADO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        Emprestimo renovado = Emprestimo.builder()
                .cliente(c).livro(livro("2222222222222"))
                .dataEmprestimo(LocalDate.now().minusDays(20))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(8))
                .status(StatusEmprestimo.RENOVADO)
                .renovacoesRealizadas(1)
                .multaDiaria(new BigDecimal("2.00")).build();

        Emprestimo ativo = emprestimoAtivo(c, livro("3333333333333"));

        c.setEmprestimos(List.of(atrasado, renovado, ativo));
        assertFalse(c.podeEmprestar()); // 3 "ativos" (ATIVO+ATRASADO+RENOVADO)
    }

    // ══════════════════════════════════════════════════════════
    //  solicitarRenovacao()
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("solicitarRenovacao() → não lança exceção quando empréstimo pode renovar")
    void solicitarRenovacao_devePermitir_quandoElegivel() {
        Cliente c = clienteVazio();
        Livro l = livro("1111111111111");
        Emprestimo emp = emprestimoAtivo(c, l); // renovacoesRealizadas = 0

        assertDoesNotThrow(() -> c.solicitarRenovacao(emp));
    }

    @Test
    @DisplayName("solicitarRenovacao() → lança IllegalStateException quando limite atingido (RN09)")
    void solicitarRenovacao_deveLancar_quandoLimiteAtingido() {
        Cliente c = clienteVazio();
        Livro l = livro("1111111111111");
        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(28))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(14))
                .status(StatusEmprestimo.RENOVADO)
                .renovacoesRealizadas(Emprestimo.LIMITE_RENOVACOES) // 2 — limite
                .multaDiaria(new BigDecimal("2.00")).build();

        assertThrows(IllegalStateException.class, () -> c.solicitarRenovacao(emp));
    }

    // ══════════════════════════════════════════════════════════
    //  cancelarReserva()
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("cancelarReserva() → seta CANCELADO quando reserva está PENDENTE")
    void cancelarReserva_deveCancelar_quandoPendente() {
        Cliente c = clienteVazio();
        Livro l = livro("1111111111111");
        Reserva reserva = Reserva.builder()
                .cliente(c).livro(l)
                .dataReserva(LocalDate.now().minusDays(1))
                .dataExpiracao(LocalDate.now().plusDays(6))
                .status(StatusReserva.PENDENTE).build();

        c.cancelarReserva(reserva);

        assertEquals(StatusReserva.CANCELADO, reserva.getStatus());
    }

    @Test
    @DisplayName("cancelarReserva() → lança IllegalStateException quando reserva não é PENDENTE")
    void cancelarReserva_deveLancar_quandoNaoPendente() {
        Cliente c = clienteVazio();
        Livro l = livro("1111111111111");
        Reserva reserva = Reserva.builder()
                .cliente(c).livro(l)
                .dataReserva(LocalDate.now().minusDays(5))
                .dataExpiracao(LocalDate.now().plusDays(2))
                .status(StatusReserva.CONFIRMADO).build(); // já confirmada

        assertThrows(IllegalStateException.class, () -> c.cancelarReserva(reserva));
    }

    // ══════════════════════════════════════════════════════════
    //  inativar() / isAtivo() — exclusão lógica
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("isAtivo() → true para cliente recém-criado (padrão ATIVO)")
    void isAtivo_deveRetornarTrue_padraoAtivo() {
        Cliente c = clienteVazio();
        assertTrue(c.isAtivo());
        assertEquals(StatusUsuario.ATIVO, c.getStatus());
    }

    @Test
    @DisplayName("inativar() → seta StatusUsuario.INATIVO (soft-delete)")
    void inativar_deveSetarInativo() {
        Cliente c = clienteVazio();
        c.inativar();

        assertFalse(c.isAtivo());
        assertEquals(StatusUsuario.INATIVO, c.getStatus());
    }

    // ══════════════════════════════════════════════════════════
    //  atualizarDados()
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("atualizarDados() → atualiza nome e email quando não nulos")
    void atualizarDados_deveAtualizar() {
        Cliente c = clienteVazio();
        c.atualizarDados("Novo Nome", "novo@email.com");

        assertEquals("Novo Nome", c.getNome());
        assertEquals("novo@email.com", c.getEmail());
    }

    @Test
    @DisplayName("atualizarDados() → não altera nome quando novo nome é nulo")
    void atualizarDados_naoAlteraNome_quandoNulo() {
        Cliente c = clienteVazio();
        String nomeOriginal = c.getNome();
        c.atualizarDados(null, "novo@email.com");

        assertEquals(nomeOriginal, c.getNome());
    }

    @Test
    @DisplayName("atualizarDados() → não altera email quando novo email é blank")
    void atualizarDados_naoAlteraEmail_quandoBlank() {
        Cliente c = clienteVazio();
        String emailOriginal = c.getEmail();
        c.atualizarDados("Novo Nome", "   ");

        assertEquals(emailOriginal, c.getEmail());
    }
}