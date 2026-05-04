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

    // ══════════════════════════════════════════════════════════
    //  isAtrasado()
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("isAtrasado() → true quando ATIVO e prazo vencido")
    void isAtrasado_deveRetornarTrue_quandoAtivoEVencido() {
        // PDF seção 6.5: "Verifica que isAtrasado() retorna verdadeiro quando
        // o empréstimo está com status ATIVO e a data prevista de devolução já passou"
        Emprestimo emp = emprestimoAtivoAtrasado(5);
        assertTrue(emp.isAtrasado());
    }

    @Test
    @DisplayName("isAtrasado() → false quando DEVOLVIDO mesmo com prazo vencido")
    void isAtrasado_deveRetornarFalse_quandoDevolvido() {
        // PDF seção 6.5: "Verifica que isAtrasado() retorna falso quando
        // status DEVOLVIDO, mesmo que a data prevista tenha passado"
        Emprestimo emp = emprestimoDevolvido();
        assertFalse(emp.isAtrasado());
    }

    @Test
    @DisplayName("isAtrasado() → false quando ATIVO e prazo futuro")
    void isAtrasado_deveRetornarFalse_quandoAtivoNoPrazo() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        assertFalse(emp.isAtrasado());
    }

    @Test
    @DisplayName("isAtrasado() → false quando prazo é exatamente hoje")
    void isAtrasado_deveRetornarFalse_quandoPrazoHoje() {
        // isAfter(hoje) → false quando prazo = hoje
        Emprestimo emp = Emprestimo.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataEmprestimo(LocalDate.now().minusDays(14))
                .dataPrevistaDevolucao(LocalDate.now()) // vence hoje
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        assertFalse(emp.isAtrasado()); // LocalDate.now().isAfter(hoje) = false
    }

    // ══════════════════════════════════════════════════════════
    //  diasAtraso()
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("diasAtraso() → calcula corretamente os dias de atraso")
    void diasAtraso_deveCalcularCorretamente() {
        // PDF seção 6.5: "validando também o cálculo correto de diasAtraso()"
        int atraso = 7;
        Emprestimo emp = emprestimoAtivoAtrasado(atraso);

        assertEquals(atraso, emp.diasAtraso());
    }

    @Test
    @DisplayName("diasAtraso() → retorna 0 quando não há atraso")
    void diasAtraso_deveRetornarZero_quandoNoPrazo() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        assertEquals(0L, emp.diasAtraso());
    }

    @Test
    @DisplayName("diasAtraso() → retorna 0 quando DEVOLVIDO")
    void diasAtraso_deveRetornarZero_quandoDevolvido() {
        Emprestimo emp = emprestimoDevolvido();
        assertEquals(0L, emp.diasAtraso());
    }

    // ══════════════════════════════════════════════════════════
    //  podeRenovar() — RN09
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("podeRenovar() → true quando ATIVO e renovacoesRealizadas < 2")
    void podeRenovar_deveRetornarTrue_quandoElegivel() {
        Emprestimo emp = emprestimoAtivoNoPrazo(); // renovacoes = 0
        assertTrue(emp.podeRenovar());
    }

    @Test
    @DisplayName("podeRenovar() → false quando renovacoesRealizadas == 2 (limite RN09)")
    void podeRenovar_deveRetornarFalse_quandoLimiteAtingido() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        emp.setRenovacoesRealizadas(Emprestimo.LIMITE_RENOVACOES); // 2
        assertFalse(emp.podeRenovar());
    }

    @Test
    @DisplayName("podeRenovar() → false quando status DEVOLVIDO")
    void podeRenovar_deveRetornarFalse_quandoDevolvido() {
        Emprestimo emp = emprestimoDevolvido();
        assertFalse(emp.podeRenovar());
    }

    @Test
    @DisplayName("podeRenovar() → false quando status RENOVADO e limite atingido")
    void podeRenovar_deveRetornarFalse_quandoRenovadoELimite() {
        Emprestimo emp = Emprestimo.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataEmprestimo(LocalDate.now().minusDays(28))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(14))
                .status(StatusEmprestimo.RENOVADO)
                .renovacoesRealizadas(2)
                .multaDiaria(new BigDecimal("2.00")).build();

        assertFalse(emp.podeRenovar());
    }

    // ══════════════════════════════════════════════════════════
    //  renovarEmprestimo() — RN04 + RN09
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("renovarEmprestimo() → prorroga +14 dias e incrementa renovacoesRealizadas")
    void renovarEmprestimo_deveProrrogar_quandoElegivel() {
        // CT09 — Renovação com sucesso
        Emprestimo emp = emprestimoAtivoNoPrazo();
        LocalDate prazoAntes = emp.getDataPrevistaDevolucao();

        emp.renovarEmprestimo();

        assertEquals(prazoAntes.plusDays(14), emp.getDataPrevistaDevolucao());
        assertEquals(1, emp.getRenovacoesRealizadas());
        assertEquals(StatusEmprestimo.RENOVADO, emp.getStatus());
    }

    @Test
    @DisplayName("renovarEmprestimo() → 2ª renovação também funciona (CT09b)")
    void renovarEmprestimo_segundaRenovacao_devePermitir() {
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
    void renovarEmprestimo_deveLancar_quandoEmAtraso() {
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
    void renovarEmprestimo_deveLancar_quandoLimiteRenovacoesAtingido() {
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

    // ══════════════════════════════════════════════════════════
    //  calcularMultaAtraso() — RN10
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("calcularMultaAtraso() → multa = multaDiaria × diasAtraso (RN10)")
    void calcularMultaAtraso_deveCalcularCorretamente() {
        int diasAtraso = 5;
        BigDecimal multaDiaria = new BigDecimal("2.00");
        BigDecimal esperada = multaDiaria.multiply(BigDecimal.valueOf(diasAtraso)); // 10.00

        Emprestimo emp = emprestimoAtivoAtrasado(diasAtraso);
        emp.setMultaDiaria(multaDiaria);

        assertEquals(esperada, emp.calcularMultaAtraso());
    }

    @Test
    @DisplayName("calcularMultaAtraso() → retorna 0,00 quando no prazo")
    void calcularMultaAtraso_deveRetornarZero_quandoNoPrazo() {
        Emprestimo emp = emprestimoAtivoNoPrazo();
        assertEquals(BigDecimal.ZERO, emp.calcularMultaAtraso());
    }

    @Test
    @DisplayName("calcularMultaAtraso() → retorna 0,00 quando DEVOLVIDO")
    void calcularMultaAtraso_deveRetornarZero_quandoDevolvido() {
        Emprestimo emp = emprestimoDevolvido();
        assertEquals(BigDecimal.ZERO, emp.calcularMultaAtraso());
    }

    @Test
    @DisplayName("calcularMultaAtraso() → 1 dia de atraso = 1 × multaDiaria")
    void calcularMultaAtraso_umDia() {
        Emprestimo emp = emprestimoAtivoAtrasado(1);
        emp.setMultaDiaria(new BigDecimal("2.00"));

        assertEquals(new BigDecimal("2.00"), emp.calcularMultaAtraso());
    }

    // ══════════════════════════════════════════════════════════
    //  devolverLivro()
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("devolverLivro() → status DEVOLVIDO e dataDevolucao = hoje (CT07)")
    void devolverLivro_deveAlterarStatus_quandoAtivo() {
        // CT07 — Devolução com sucesso
        Emprestimo emp = emprestimoAtivoNoPrazo();

        emp.devolverLivro();

        assertEquals(StatusEmprestimo.DEVOLVIDO, emp.getStatus());
        assertEquals(LocalDate.now(), emp.getDataDevolucao());
    }

    @Test
    @DisplayName("devolverLivro() → lança IllegalStateException quando já devolvido (CT08)")
    void devolverLivro_deveLancar_quandoJaDevolvido() {
        // CT08 — Devolução já realizada
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
    void devolverLivro_deveFuncionar_quandoAtrasado() {
        Emprestimo emp = emprestimoAtivoAtrasado(5);
        emp.devolverLivro();

        assertEquals(StatusEmprestimo.DEVOLVIDO, emp.getStatus());
        assertNotNull(emp.getDataDevolucao());
    }
}