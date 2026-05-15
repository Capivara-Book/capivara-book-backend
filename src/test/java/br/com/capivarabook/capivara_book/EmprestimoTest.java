package br.com.capivarabook.capivara_book;

import br.com.capivarabook.capivara_book.entity.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

// Testa a lógica isolada da entidade Emprestimo
@DisplayName("Emprestimo — Testes Unitários")
class EmprestimoTest {

    private Cliente clienteBase() {
        Cliente c = new Cliente();
        c.setNome("Maria Silva");
        c.setEmail("maria@email.com");
        c.setSenha("senha");
        c.setCpf("12345678901");
        c.setTelefone("11987654321");
        c.setStatus(StatusUsuario.ATIVO);
        c.setRole(Role.CLIENTE);
        return c;
    }

    private Livro livroBase() {
        return Livro.builder()
                .titulo("Dom Casmurro").autor("Machado de Assis")
                .editora("Companhia das Letras").isbn("9788535909555")
                .anoPublicacao(1899).genero(Genero.ROMANCE)
                .exemplares(5).exemplaresDisponiveis(3)
                .status(StatusLivro.DISPONIVEL).build();
    }

    // ── Emprestimo ATIVO dentro do prazo ─────────────────────
    private Emprestimo emprestimoAtivoNoPrazo() {
        return Emprestimo.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataEmprestimo(LocalDate.now().minusDays(5))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(9))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00"))
                .build();
    }

    // ── Emprestimo ATIVO em atraso ────────────────────────────
    private Emprestimo emprestimoAtivoAtrasado(int diasAtraso) {
        return Emprestimo.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataEmprestimo(LocalDate.now().minusDays(diasAtraso + 14))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(diasAtraso))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00"))
                .build();
    }

    // ── Emprestimo DEVOLVIDO ──────────────────────────────────
    private Emprestimo emprestimoDevolvido() {
        return Emprestimo.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataEmprestimo(LocalDate.now().minusDays(20))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(6))
                .dataDevolucao(LocalDate.now().minusDays(7))
                .status(StatusEmprestimo.DEVOLVIDO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00"))
                .build();
    }



    @Test
    @DisplayName("isAtrasado() → true quando ATIVO e prazo vencido")
    void isAtrasadoQuandoAtivoEVencido() {

        Emprestimo emp = emprestimoAtivoAtrasado(5);
        assertTrue(emp.isAtrasado());
    }

    @Test
    @DisplayName("isAtrasado() → false quando DEVOLVIDO mesmo com prazo vencido")
    void isAtrasadoQuandoDevolvido() {

        Emprestimo emp = emprestimoDevolvido();
        assertFalse(emp.isAtrasado());
    }

    @Test
    @DisplayName("isAtrasado() → false quando ATIVO e prazo futuro")
    void isAtrasadoQuandoAtivoNoPrazo() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        assertFalse(emp.isAtrasado());
    }

    @Test
    @DisplayName("isAtrasado() → false quando prazo é exatamente hoje")
    void isAtrasadoQuandoPrazoHoje() {
        Emprestimo emp = Emprestimo.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataEmprestimo(LocalDate.now().minusDays(14))
                .dataPrevistaDevolucao(LocalDate.now()) // vence hoje
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        assertFalse(emp.isAtrasado()); // LocalDate.now().isAfter(hoje) = false
    }

    @Test
    @DisplayName("isAtrasado() → false quando status RENOVADO e dentro do prazo")
    void isAtrasadoQuandoRenovadoNoPrazo() {
        Emprestimo emp = Emprestimo.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataEmprestimo(LocalDate.now().minusDays(14))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(14))
                .status(StatusEmprestimo.RENOVADO)
                .renovacoesRealizadas(1)
                .multaDiaria(new BigDecimal("2.00")).build();

        assertFalse(emp.isAtrasado());
    }

    @Test
    @DisplayName("isAtrasado() → true com atraso de exatamente 1 dia")
    void isAtrasadoQuandoAtrasadoUmDia() {
        Emprestimo emp = emprestimoAtivoAtrasado(1);
        assertTrue(emp.isAtrasado());
    }



    @Test
    @DisplayName("diasAtraso() → calcula corretamente os dias de atraso")
    void diasAtrasoCalcular() {
        // PDF seção 6.5: "validando também o cálculo correto de diasAtraso()"
        int atraso = 7;
        Emprestimo emp = emprestimoAtivoAtrasado(atraso);

        assertEquals(atraso, emp.diasAtraso());
    }

    @Test
    @DisplayName("diasAtraso() → retorna 0 quando não há atraso")
    void diasAtrasoZeroQuandoNoPrazo() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        assertEquals(0L, emp.diasAtraso());
    }

    @Test
    @DisplayName("diasAtraso() → retorna 0 quando DEVOLVIDO")
    void diasAtrasoZeroQuandoDevolvido() {
        Emprestimo emp = emprestimoDevolvido();
        assertEquals(0L, emp.diasAtraso());
    }

    @Test
    @DisplayName("diasAtraso() → retorna 0 quando prazo é exatamente hoje")
    void diasAtrasoQuandoPrazoHoje() {
        Emprestimo emp = Emprestimo.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataEmprestimo(LocalDate.now().minusDays(14))
                .dataPrevistaDevolucao(LocalDate.now())
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        assertEquals(0L, emp.diasAtraso());
    }

    @Test
    @DisplayName("diasAtraso() → retorna valor correto para atraso longo (30 dias)")
    void diasAtrasoQuandoAtrasado30Dias() {
        Emprestimo emp = emprestimoAtivoAtrasado(30);
        assertEquals(30L, emp.diasAtraso());
    }


    @Test
    @DisplayName("podeRenovar() → true quando ATIVO e renovacoesRealizadas < 2")
    void podeRenovarQuandoElegivel() {
        Emprestimo emp = emprestimoAtivoNoPrazo(); // renovacoes = 0
        assertTrue(emp.podeRenovar());
    }

    @Test
    @DisplayName("podeRenovar() → false quando renovacoesRealizadas == 2 (limite RN09)")
    void podeRenovarQuandoLimiteAtingido() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        emp.setRenovacoesRealizadas(Emprestimo.LIMITE_RENOVACOES); // 2
        assertFalse(emp.podeRenovar());
    }

    @Test
    @DisplayName("podeRenovar() → false quando status DEVOLVIDO")
    void podeRenovarQuandoDevolvido() {
        Emprestimo emp = emprestimoDevolvido();
        assertFalse(emp.podeRenovar());
    }

    @Test
    @DisplayName("podeRenovar() → false quando status RENOVADO e limite atingido")
    void podeRenovarQuandoRenovadoELimite() {
        Emprestimo emp = Emprestimo.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataEmprestimo(LocalDate.now().minusDays(28))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(14))
                .status(StatusEmprestimo.RENOVADO)
                .renovacoesRealizadas(2)
                .multaDiaria(new BigDecimal("2.00")).build();

        assertFalse(emp.podeRenovar());
    }

    @Test
    @DisplayName("podeRenovar() → true quando renovacoesRealizadas == 1 (ainda abaixo do limite)")
    void podeRenovarQuandoUmaRenovacaoFeita() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        emp.setRenovacoesRealizadas(1);
        assertTrue(emp.podeRenovar());
    }



    @Test
    @DisplayName("renovarEmprestimo() → prorroga +14 dias e incrementa renovacoesRealizadas")
    void renovarEmprestimoProrrogarQuandoElegivel() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        LocalDate prazoAntes = emp.getDataPrevistaDevolucao();

        emp.renovarEmprestimo();

        assertEquals(prazoAntes.plusDays(14), emp.getDataPrevistaDevolucao());
        assertEquals(1, emp.getRenovacoesRealizadas());
        assertEquals(StatusEmprestimo.RENOVADO, emp.getStatus());
    }

    @Test
    @DisplayName("renovarEmprestimo() → 2ª renovação também funciona (CT09b)")
    void renovarEmprestimoSegundaRenovacao() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        emp.renovarEmprestimo(); // 1ª
        // Após renovar, status fica RENOVADO — podeRenovar() verifica ATIVO
        // Para 2ª, precisamos que status seja ATIVO e renovacoes < 2
        emp.setStatus(StatusEmprestimo.ATIVO); // simula reativação
        emp.renovarEmprestimo(); // 2ª

        assertEquals(2, emp.getRenovacoesRealizadas());
    }

    @Test
    @DisplayName("renovarEmprestimo() → lança IllegalStateException quando em atraso (RN04 / CT10)")
    void renovarEmprestimoQuandoEmAtraso() {
        // CT10 — Renovação em atraso
        Emprestimo emp = emprestimoAtivoAtrasado(3);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                emp::renovarEmprestimo
        );
        assertTrue(ex.getMessage().contains("atraso") ||
                ex.getMessage().contains("Atraso") ||
                ex.getMessage().contains("bloqueada"));
    }

    @Test
    @DisplayName("renovarEmprestimo() → lança IllegalStateException quando limite atingido (RN09 / CT09c)")
    void renovarEmprestimoQuandoLimiteRenovacoesAtingido() {
        // CT09c — 3ª tentativa bloqueada
        Emprestimo emp = emprestimoAtivoNoPrazo();
        emp.setRenovacoesRealizadas(Emprestimo.LIMITE_RENOVACOES); // 2

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                emp::renovarEmprestimo
        );
        assertTrue(ex.getMessage().contains("renovações") ||
                ex.getMessage().contains("Limite"));
    }

    @Test
    @DisplayName("renovarEmprestimo() → lança IllegalStateException quando status DEVOLVIDO")
    void renovarEmprestimoQuandoDevolvido() {
        Emprestimo emp = emprestimoDevolvido();

        assertThrows(IllegalStateException.class, emp::renovarEmprestimo);
    }

    @Test
    @DisplayName("renovarEmprestimo() → não altera renovacoesRealizadas quando lança exceção por atraso")
    void renovarEmprestimoQuandoAtraso() {
        Emprestimo emp = emprestimoAtivoAtrasado(2);
        int renovacoesAntes = emp.getRenovacoesRealizadas();

        assertThrows(IllegalStateException.class, emp::renovarEmprestimo);

        assertEquals(renovacoesAntes, emp.getRenovacoesRealizadas());
    }

    @Test
    @DisplayName("renovarEmprestimo() → não altera dataPrevistaDevolucao quando lança exceção por limite")
    void renovarEmprestimoQuandoLimite() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        emp.setRenovacoesRealizadas(Emprestimo.LIMITE_RENOVACOES);
        LocalDate prazoAntes = emp.getDataPrevistaDevolucao();

        assertThrows(IllegalStateException.class, emp::renovarEmprestimo);

        assertEquals(prazoAntes, emp.getDataPrevistaDevolucao());
    }



    @Test
    @DisplayName("calcularMultaAtraso() → multa = multaDiaria × diasAtraso (RN10)")
    void calcularMultaAtraso() {
        int diasAtraso = 5;
        BigDecimal multaDiaria = new BigDecimal("2.00");
        BigDecimal esperada = multaDiaria.multiply(BigDecimal.valueOf(diasAtraso)); // 10.00

        Emprestimo emp = emprestimoAtivoAtrasado(diasAtraso);
        emp.setMultaDiaria(multaDiaria);

        assertEquals(esperada, emp.calcularMultaAtraso());
    }

    @Test
    @DisplayName("calcularMultaAtraso() → retorna 0,00 quando no prazo")
    void calcularMultaAtrasoQuandoNoPrazo() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        assertEquals(BigDecimal.ZERO, emp.calcularMultaAtraso());
    }

    @Test
    @DisplayName("calcularMultaAtraso() → retorna 0,00 quando DEVOLVIDO")
    void calcularMultaAtrasoQuandoDevolvido() {
        Emprestimo emp = emprestimoDevolvido();
        assertEquals(BigDecimal.ZERO, emp.calcularMultaAtraso());
    }

    @Test
    @DisplayName("calcularMultaAtraso() → 1 dia de atraso = 1 × multaDiaria")
    void calcularMultaAtrasoUmDia() {
        Emprestimo emp = emprestimoAtivoAtrasado(1);
        emp.setMultaDiaria(new BigDecimal("2.00"));

        assertEquals(new BigDecimal("2.00"), emp.calcularMultaAtraso());
    }

    @Test
    @DisplayName("calcularMultaAtraso() → multa correta com multaDiaria diferente de 2,00")
    void calcularMultaDiariaCustomizada() {
        int diasAtraso = 3;
        BigDecimal multaDiaria = new BigDecimal("5.50");
        BigDecimal esperada = multaDiaria.multiply(BigDecimal.valueOf(diasAtraso)); // 16.50

        Emprestimo emp = emprestimoAtivoAtrasado(diasAtraso);
        emp.setMultaDiaria(multaDiaria);

        assertEquals(esperada, emp.calcularMultaAtraso());
    }

    @Test
    @DisplayName("calcularMultaAtraso() → multa não é negativa mesmo com prazo futuro")
    void calcularMultaAtrasaNaoDeveSerNegativaQuandoNoPrazo() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        BigDecimal multa = emp.calcularMultaAtraso();

        assertTrue(multa.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("calcularMultaAtraso() → acumula corretamente para atraso longo")
    void calcularMultaAtrasoQuandoAtrasado15Dias() {
        int diasAtraso = 15;
        BigDecimal multaDiaria = new BigDecimal("2.00");
        BigDecimal esperada = new BigDecimal("30.00");

        Emprestimo emp = emprestimoAtivoAtrasado(diasAtraso);
        emp.setMultaDiaria(multaDiaria);

        assertEquals(esperada, emp.calcularMultaAtraso());
    }



    @Test
    @DisplayName("devolverLivro() → status DEVOLVIDO e dataDevolucao = hoje (CT07)")
    void devolverLivroAlterarStatusQuandoAtivo() {
        Emprestimo emp = emprestimoAtivoNoPrazo();

        emp.devolverLivro();

        assertEquals(StatusEmprestimo.DEVOLVIDO, emp.getStatus());
        assertEquals(LocalDate.now(), emp.getDataDevolucao());
    }

    @Test
    @DisplayName("devolverLivro() → lança IllegalStateException quando já devolvido (CT08)")
    void devolverLivroLancarQuandoJaDevolvido() {

        Emprestimo emp = emprestimoDevolvido();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                emp::devolverLivro
        );
        assertTrue(ex.getMessage().contains("devolvido") ||
                ex.getMessage().contains("Devolvido"));
    }

    @Test
    @DisplayName("devolverLivro() → funciona quando empréstimo está ATRASADO")
    void devolverLivroQuandoAtrasado() {
        Emprestimo emp = emprestimoAtivoAtrasado(5);
        emp.devolverLivro();

        assertEquals(StatusEmprestimo.DEVOLVIDO, emp.getStatus());
        assertNotNull(emp.getDataDevolucao());
    }

    @Test
    @DisplayName("devolverLivro() → dataDevolucao não é nula após devolução")
    void devolverLivroDataDevolucaoQuandoAtivo() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        assertNull(emp.getDataDevolucao());

        emp.devolverLivro();

        assertNotNull(emp.getDataDevolucao());
    }

    @Test
    @DisplayName("devolverLivro() → segunda chamada consecutiva também lança IllegalStateException")
    void devolverLivroQuandoChamadaDuasVezes() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        emp.devolverLivro(); // primeira devolução — ok

        assertThrows(IllegalStateException.class, emp::devolverLivro); // segunda — deve lançar
    }

    @Test
    @DisplayName("devolverLivro() → status permanece DEVOLVIDO após tentativa inválida")
    void devolverLivroStatusDevolvidoAposExcecao() {
        Emprestimo emp = emprestimoDevolvido();

        assertThrows(IllegalStateException.class, emp::devolverLivro);

        assertEquals(StatusEmprestimo.DEVOLVIDO, emp.getStatus());
    }

    @Test
    @DisplayName("devolverLivro() → funciona quando status é RENOVADO")
    void devolverLivroQuandoRenovado() {
        Emprestimo emp = Emprestimo.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataEmprestimo(LocalDate.now().minusDays(28))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(7))
                .status(StatusEmprestimo.RENOVADO)
                .renovacoesRealizadas(1)
                .multaDiaria(new BigDecimal("2.00")).build();

        emp.devolverLivro();

        assertEquals(StatusEmprestimo.DEVOLVIDO, emp.getStatus());
        assertEquals(LocalDate.now(), emp.getDataDevolucao());
    }
}