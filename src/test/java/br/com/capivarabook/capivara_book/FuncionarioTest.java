package br.com.capivarabook.capivara_book;

import br.com.capivarabook.capivara_book.entity.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

// Testa a lógica isolada da entidade Funcionario
@DisplayName("Funcionario — Testes Unitários")
class FuncionarioTest {

    private Funcionario adminBase() {
        Funcionario f = new Funcionario();
        f.setNome("Admin Sistema");
        f.setEmail("admin@capivarabook.com");
        f.setSenha("admin123");
        f.setMatricula("ADM000001");
        f.setCargo(Cargo.ADMIN);
        f.setStatus(StatusUsuario.ATIVO);
        f.setRole(Role.ADMIN);
        return f;
    }

    private Cliente clienteDisponivel() {
        Cliente c = new Cliente();
        c.setNome("Maria"); c.setEmail("maria@email.com");
        c.setSenha("s"); c.setCpf("12345678901"); c.setTelefone("11999998888");
        c.setStatus(StatusUsuario.ATIVO); c.setRole(Role.CLIENTE);
        c.setEmprestimos(new ArrayList<>());
        return c;
    }

    private Livro livroDisponivel() {
        return Livro.builder()
                .titulo("Clean Code").autor("Robert C. Martin").editora("Alta Books")
                .isbn("9788576082675").anoPublicacao(2008).genero(Genero.TECNOLOGIA)
                .exemplares(4).exemplaresDisponiveis(3).status(StatusLivro.DISPONIVEL).build();
    }

    private Livro livroIndisponivel() {
        return Livro.builder()
                .titulo("It").autor("Stephen King").editora("Suma")
                .isbn("9788576655336").anoPublicacao(1986).genero(Genero.MISTERIO)
                .exemplares(2).exemplaresDisponiveis(0).status(StatusLivro.INDISPONIVEL).build();
    }

    // ── registrarEmprestimo() ─────────────────────────────────

    @Test
    @DisplayName("registrarEmprestimo() → cria Emprestimo ATIVO com prazo de 14 dias (RN08)")
    void registrarEmprestimo_deveCriar_quandoCondicoesOk() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l = livroDisponivel();

        Emprestimo emp = func.registrarEmprestimo(c, l, 14, new BigDecimal("2.00"));

        assertNotNull(emp);
        assertEquals(StatusEmprestimo.ATIVO, emp.getStatus());
        assertEquals(LocalDate.now().plusDays(14), emp.getDataPrevistaDevolucao());
        assertEquals(0, emp.getRenovacoesRealizadas());
        assertEquals(2, l.getExemplaresDisponiveis()); // 3-1
    }

    @Test
    @DisplayName("registrarEmprestimo() → lança exceção quando cliente no limite (RN01)")
    void registrarEmprestimo_deveLancar_quandoClienteNoLimite() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l1 = livroDisponivel();

        // Adiciona 3 empréstimos ativos manualmente
        for (int i = 0; i < 3; i++) {
            Emprestimo e = Emprestimo.builder()
                    .cliente(c).livro(l1)
                    .dataEmprestimo(LocalDate.now())
                    .dataPrevistaDevolucao(LocalDate.now().plusDays(14))
                    .status(StatusEmprestimo.ATIVO)
                    .renovacoesRealizadas(0)
                    .multaDiaria(new BigDecimal("2.00")).build();
            c.getEmprestimos().add(e);
        }

        assertThrows(IllegalStateException.class,
                () -> func.registrarEmprestimo(c, l1, 14, new BigDecimal("2.00")));
    }

    @Test
    @DisplayName("registrarEmprestimo() → lança exceção quando livro indisponível (RN02)")
    void registrarEmprestimo_deveLancar_quandoLivroIndisponivel() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l = livroIndisponivel();

        assertThrows(IllegalStateException.class,
                () -> func.registrarEmprestimo(c, l, 14, new BigDecimal("2.00")));
    }

    @Test
    @DisplayName("registrarEmprestimo() → lança exceção quando duplicata ativa (RN03)")
    void registrarEmprestimo_deveLancar_quandoDuplicataAtiva() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l = livroDisponivel();

        // Adiciona empréstimo ativo do mesmo livro
        Emprestimo empExistente = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(3))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(11))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();
        c.getEmprestimos().add(empExistente);

        assertThrows(IllegalStateException.class,
                () -> func.registrarEmprestimo(c, l, 14, new BigDecimal("2.00")));
    }

    // ── registrarDevolucao() ──────────────────────────────────

    @Test
    @DisplayName("registrarDevolucao() → retorna multa 0 quando no prazo (CT07)")
    void registrarDevolucao_deveRetornarMultaZero_quandoNoPrazo() {
        Funcionario func = adminBase();
        Livro l = livroDisponivel();
        Cliente c = clienteDisponivel();
        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(5))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(9))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        BigDecimal multa = func.registrarDevolucao(emp);

        assertEquals(BigDecimal.ZERO, multa);
        assertEquals(StatusEmprestimo.DEVOLVIDO, emp.getStatus());
    }

    @Test
    @DisplayName("registrarDevolucao() → calcula multa corretamente quando em atraso (RN10)")
    void registrarDevolucao_deveCalcularMulta_quandoAtrasado() {
        Funcionario func = adminBase();
        Livro l = livroDisponivel();
        l.setExemplaresDisponiveis(1);
        Cliente c = clienteDisponivel();
        int diasAtraso = 4;

        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(diasAtraso + 14))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(diasAtraso))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        BigDecimal multa = func.registrarDevolucao(emp);

        assertEquals(new BigDecimal("2.00").multiply(BigDecimal.valueOf(diasAtraso)), multa);
    }

    @Test
    @DisplayName("registrarDevolucao() → lança exceção quando já devolvido (CT08)")
    void registrarDevolucao_deveLancar_quandoJaDevolvido() {
        Funcionario func = adminBase();
        Livro l = livroDisponivel();
        Cliente c = clienteDisponivel();
        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(20))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(6))
                .dataDevolucao(LocalDate.now().minusDays(7))
                .status(StatusEmprestimo.DEVOLVIDO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        assertThrows(IllegalStateException.class, () -> func.registrarDevolucao(emp));
    }

    // ── statusLivro() ─────────────────────────────────────────

    @Test
    @DisplayName("statusLivro() → seta DISPONIVEL quando livro tem exemplares disponíveis")
    void statusLivro_deveSetarDisponivel() {
        Funcionario func = adminBase();
        Livro l = livroDisponivel();
        func.statusLivro(l, StatusLivro.DISPONIVEL);
        assertEquals(StatusLivro.DISPONIVEL, l.getStatus());
    }

    @Test
    @DisplayName("statusLivro() → lança exceção ao inativar livro com exemplares emprestados (RN06)")
    void statusLivro_deveLancar_quandoExemplaresEmprestados() {
        Funcionario func = adminBase();
        // 2 exemplares, apenas 1 disponível → 1 emprestado
        Livro l = Livro.builder()
                .titulo("Livro").autor("A").editora("E").isbn("1111111111111")
                .anoPublicacao(2020).genero(Genero.DRAMA)
                .exemplares(2).exemplaresDisponiveis(1)
                .status(StatusLivro.DISPONIVEL).build();

        assertThrows(IllegalStateException.class,
                () -> func.statusLivro(l, StatusLivro.INDISPONIVEL));
    }

    // ── statusRenovacao() ─────────────────────────────────────

    @Test
    @DisplayName("statusRenovacao() → delega para renovarEmprestimo() do Emprestimo")
    void statusRenovacao_deveDelegarParaRenovarEmprestimo() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l = livroDisponivel();
        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(5))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(9))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        func.statusRenovacao(emp);

        assertEquals(StatusEmprestimo.RENOVADO, emp.getStatus());
        assertEquals(1, emp.getRenovacoesRealizadas());
    }
}