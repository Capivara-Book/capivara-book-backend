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

    private Livro livro(String isbn) {
        return Livro.builder()
                .titulo("Livro Teste").autor("Autor").editora("Ed")
                .isbn(isbn).anoPublicacao(2020).genero(Genero.ROMANCE)
                .exemplares(5).exemplaresDisponiveis(3)
                .status(StatusLivro.DISPONIVEL).build();
    }

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

    @Test
    @DisplayName("podeEmprestar() → true quando cliente não tem nenhum empréstimo")
    void podeEmprestarSemEmprestimos() {
        Cliente c = clienteVazio();
        assertTrue(c.podeEmprestar());
    }

    @Test
    @DisplayName("podeEmprestar() → true com 1 empréstimo ativo")
    void podeEmprestarComUmAtivo() {
        Cliente c = clienteVazio();
        Livro l = livro("1111111111111");
        c.setEmprestimos(List.of(emprestimoAtivo(c, l)));

        assertTrue(c.podeEmprestar());
    }

    @Test
    @DisplayName("podeEmprestar() → true com 2 empréstimos ativos")
    void podeEmprestarComDoisAtivos() {
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
    void podeEmprestarQuandoNoLimite() {
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
    void podeEmprestarNaoContaDevolvidos() {
        Cliente c = clienteVazio();
        c.setEmprestimos(List.of(
                emprestimoAtivo(c, livro("1111111111111")),
                emprestimoAtivo(c, livro("2222222222222")),
                emprestimoAtivo(c, livro("3333333333333")),
                emprestimoDevolvido(c, livro("4444444444444")),
                emprestimoDevolvido(c, livro("5555555555555"))
        ));

        assertFalse(c.podeEmprestar());
    }

    @Test
    @DisplayName("podeEmprestar() → true quando todos os empréstimos estão DEVOLVIDOS")
    void podeEmprestarQuandoTodosDevolvidos() {
        Cliente c = clienteVazio();
        c.setEmprestimos(List.of(
                emprestimoDevolvido(c, livro("1111111111111")),
                emprestimoDevolvido(c, livro("2222222222222")),
                emprestimoDevolvido(c, livro("3333333333333"))
        ));
        assertTrue(c.podeEmprestar());
    }

    @Test
    @DisplayName("podeEmprestar() → false conta empréstimos ATRASADO e RENOVADO como ativos")
    void podeEmprestarContaAtrasadoERenovado() {
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
        assertFalse(c.podeEmprestar());
    }

    // ── Cenários de erro — podeEmprestar() ────────────────

    @Test
    @DisplayName("podeEmprestar() → false com 3 ATRASADOS (sem nenhum ATIVO)")
    void podeEmprestarQuandoTresAtrasados() {
        Cliente c = clienteVazio();

        Emprestimo atrasado1 = Emprestimo.builder()
                .cliente(c).livro(livro("1111111111111"))
                .dataEmprestimo(LocalDate.now().minusDays(30))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(16))
                .status(StatusEmprestimo.ATRASADO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        Emprestimo atrasado2 = Emprestimo.builder()
                .cliente(c).livro(livro("2222222222222"))
                .dataEmprestimo(LocalDate.now().minusDays(30))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(16))
                .status(StatusEmprestimo.ATRASADO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        Emprestimo atrasado3 = Emprestimo.builder()
                .cliente(c).livro(livro("3333333333333"))
                .dataEmprestimo(LocalDate.now().minusDays(30))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(16))
                .status(StatusEmprestimo.ATRASADO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        c.setEmprestimos(List.of(atrasado1, atrasado2, atrasado3));
        assertFalse(c.podeEmprestar());
    }

    @Test
    @DisplayName("podeEmprestar() → false com 3 RENOVADOS (sem nenhum ATIVO)")
    void podeEmprestarQuandoTresRenovados() {
        Cliente c = clienteVazio();

        Emprestimo renovado1 = Emprestimo.builder()
                .cliente(c).livro(livro("1111111111111"))
                .dataEmprestimo(LocalDate.now().minusDays(20))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(8))
                .status(StatusEmprestimo.RENOVADO)
                .renovacoesRealizadas(1)
                .multaDiaria(new BigDecimal("2.00")).build();

        Emprestimo renovado2 = Emprestimo.builder()
                .cliente(c).livro(livro("2222222222222"))
                .dataEmprestimo(LocalDate.now().minusDays(20))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(8))
                .status(StatusEmprestimo.RENOVADO)
                .renovacoesRealizadas(1)
                .multaDiaria(new BigDecimal("2.00")).build();

        Emprestimo renovado3 = Emprestimo.builder()
                .cliente(c).livro(livro("3333333333333"))
                .dataEmprestimo(LocalDate.now().minusDays(20))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(8))
                .status(StatusEmprestimo.RENOVADO)
                .renovacoesRealizadas(1)
                .multaDiaria(new BigDecimal("2.00")).build();

        c.setEmprestimos(List.of(renovado1, renovado2, renovado3));
        assertFalse(c.podeEmprestar());
    }

    @Test
    @DisplayName("solicitarRenovacao() → não lança exceção quando empréstimo pode renovar")
    void solicitarRenovacaoQuandoElegivel() {
        Cliente c = clienteVazio();
        Livro l = livro("1111111111111");
        Emprestimo emp = emprestimoAtivo(c, l); // renovacoesRealizadas = 0

        assertDoesNotThrow(() -> c.solicitarRenovacao(emp));
    }

    @Test
    @DisplayName("solicitarRenovacao() → lança IllegalStateException quando limite atingido (RN09)")
    void solicitarRenovacaoQuandoLimiteAtingido() {
        Cliente c = clienteVazio();
        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(livro("1111111111111"))
                .dataEmprestimo(LocalDate.now().minusDays(28))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(14))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(Emprestimo.LIMITE_RENOVACOES) // 2 — limite
                .multaDiaria(new BigDecimal("2.00")).build();

        assertThrows(IllegalStateException.class, () -> c.solicitarRenovacao(emp));
    }

    // ── Cenários de erro — solicitarRenovacao() ───────────

    @Test
    @DisplayName("solicitarRenovacao() → não lança exceção com 1 renovação realizada (abaixo do limite)")
    void solicitarRenovacaoQuandoUmaRenovacao() {
        Cliente c = clienteVazio();
        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(livro("1111111111111"))
                .dataEmprestimo(LocalDate.now().minusDays(14))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(14))
                .status(StatusEmprestimo.ATIVO) // ATIVO — exigido por podeRenovar()
                .renovacoesRealizadas(1)        // 1 renovação feita, abaixo do limite (2)
                .multaDiaria(new BigDecimal("2.00")).build();

        assertDoesNotThrow(() -> c.solicitarRenovacao(emp));
    }

    @Test
    @DisplayName("solicitarRenovacao() → mensagem da exceção contém o limite de renovações")
    void solicitarRenovacaoComMensagemCorreta() {
        Cliente c = clienteVazio();
        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(livro("1111111111111"))
                .dataEmprestimo(LocalDate.now().minusDays(28))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(14))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(Emprestimo.LIMITE_RENOVACOES)
                .multaDiaria(new BigDecimal("2.00")).build();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class, () -> c.solicitarRenovacao(emp));
        assertTrue(ex.getMessage().contains(String.valueOf(Emprestimo.LIMITE_RENOVACOES)));
    }

    @Test
    @DisplayName("cancelarReserva() → seta CANCELADO quando reserva está PENDENTE")
    void cancelarReservaQuandoPendente() {
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
    void cancelarReservaQuandoNaoPendente() {
        Cliente c = clienteVazio();
        Livro l = livro("1111111111111");
        Reserva reserva = Reserva.builder()
                .cliente(c).livro(l)
                .dataReserva(LocalDate.now().minusDays(5))
                .dataExpiracao(LocalDate.now().plusDays(2))
                .status(StatusReserva.CONFIRMADO).build();

        assertThrows(IllegalStateException.class, () -> c.cancelarReserva(reserva));
    }

    // ── Cenários de erro — cancelarReserva() ──────────────

    @Test
    @DisplayName("cancelarReserva() → lança IllegalStateException quando reserva já está CANCELADO")
    void cancelarReservaQuandoCancelado() {
        Cliente c = clienteVazio();
        Reserva reserva = Reserva.builder()
                .cliente(c).livro(livro("1111111111111"))
                .dataReserva(LocalDate.now().minusDays(3))
                .dataExpiracao(LocalDate.now().plusDays(4))
                .status(StatusReserva.CANCELADO).build();

        assertThrows(IllegalStateException.class, () -> c.cancelarReserva(reserva));
    }

    @Test
    @DisplayName("cancelarReserva() → lança IllegalStateException quando reserva está EXPIRADO")
    void cancelarReservaQuandoExpirado() {
        Cliente c = clienteVazio();
        Reserva reserva = Reserva.builder()
                .cliente(c).livro(livro("1111111111111"))
                .dataReserva(LocalDate.now().minusDays(10))
                .dataExpiracao(LocalDate.now().minusDays(3))
                .status(StatusReserva.EXPIRADO).build();

        assertThrows(IllegalStateException.class, () -> c.cancelarReserva(reserva));
    }

    @Test
    @DisplayName("cancelarReserva() → mensagem da exceção menciona PENDENTE")
    void cancelarReservaComMensagemCorreta() {
        Cliente c = clienteVazio();
        Reserva reserva = Reserva.builder()
                .cliente(c).livro(livro("1111111111111"))
                .dataReserva(LocalDate.now().minusDays(5))
                .dataExpiracao(LocalDate.now().plusDays(2))
                .status(StatusReserva.CONFIRMADO).build();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class, () -> c.cancelarReserva(reserva));
        assertTrue(ex.getMessage().contains("PENDENTE"));
    }

    @Test
    @DisplayName("isAtivo() → true para cliente recém-criado (padrão ATIVO)")
    void isAtivoPadrao() {
        Cliente c = clienteVazio();
        assertTrue(c.isAtivo());
        assertEquals(StatusUsuario.ATIVO, c.getStatus());
    }

    @Test
    @DisplayName("inativar() → seta StatusUsuario.INATIVO (soft-delete)")
    void isInativar() {
        Cliente c = clienteVazio();
        c.inativar();

        assertFalse(c.isAtivo());
        assertEquals(StatusUsuario.INATIVO, c.getStatus());
    }

    // ── Cenários de erro — isInativar() / isAtivoPadrao() ─────────

    @Test
    @DisplayName("inativar() → idempotente: chamar duas vezes mantém INATIVO")
    void inativarDeveSerIdempotente() {
        Cliente c = clienteVazio();
        c.inativar();
        c.inativar();

        assertEquals(StatusUsuario.INATIVO, c.getStatus());
        assertFalse(c.isAtivo());
    }

    @Test
    @DisplayName("isAtivo() → false após inativar()")
    void isAtivoAposInativar() {
        Cliente c = clienteVazio();
        c.inativar();

        assertFalse(c.isAtivo());
    }

    @Test
    @DisplayName("atualizarDados() → atualiza nome e email quando não nulos")
    void isAtualizarDados() {
        Cliente c = clienteVazio();
        c.atualizarDados("Novo Nome", "novo@email.com");

        assertEquals("Novo Nome", c.getNome());
        assertEquals("novo@email.com", c.getEmail());
    }

    @Test
    @DisplayName("atualizarDados() → não altera nome quando novo nome é nulo")
    void atualizarDadosNaoAlteraNomeQuandoNulo() {
        Cliente c = clienteVazio();
        String nomeOriginal = c.getNome();
        c.atualizarDados(null, "novo@email.com");

        assertEquals(nomeOriginal, c.getNome());
    }

    @Test
    @DisplayName("atualizarDados() → não altera email quando novo email é blank")
    void atualizarDadosNaoAlteraEmailQuandoBlank() {
        Cliente c = clienteVazio();
        String emailOriginal = c.getEmail();
        c.atualizarDados("Novo Nome", "   ");

        assertEquals(emailOriginal, c.getEmail());
    }

    // ── Cenários de erro — isAtualizarDados() ───────────────

    @Test
    @DisplayName("atualizarDados() → não altera nome quando novo nome é blank (só espaços)")
    void atualizarDadosNaoAlteraNomeQuandoBlank() {
        Cliente c = clienteVazio();
        String nomeOriginal = c.getNome();
        c.atualizarDados("   ", "novo@email.com");

        assertEquals(nomeOriginal, c.getNome());
    }

    @Test
    @DisplayName("atualizarDados() → não altera email quando novo email é nulo")
    void atualizarDadosNaoAlteraEmailQuandoNulo() {
        Cliente c = clienteVazio();
        String emailOriginal = c.getEmail();
        c.atualizarDados("Novo Nome", null);

        assertEquals(emailOriginal, c.getEmail());
    }

    @Test
    @DisplayName("atualizarDados() → ambos nulos: nenhum campo é alterado")
    void atualizarDadosAmbosNulosNaoAltera() {
        Cliente c = clienteVazio();
        String nomeOriginal = c.getNome();
        String emailOriginal = c.getEmail();
        c.atualizarDados(null, null);

        assertAll(
                () -> assertEquals(nomeOriginal, c.getNome()),
                () -> assertEquals(emailOriginal, c.getEmail())
        );
    }

    @Test
    @DisplayName("atualizarDados() → string vazia (\"\") é tratada como blank e ignorada")
    void atualizarDadosIgnoraStringVazia() {
        Cliente c = clienteVazio();
        String nomeOriginal = c.getNome();
        String emailOriginal = c.getEmail();
        c.atualizarDados("", "");

        assertAll(
                () -> assertEquals(nomeOriginal, c.getNome()),
                () -> assertEquals(emailOriginal, c.getEmail())
        );
    }
}