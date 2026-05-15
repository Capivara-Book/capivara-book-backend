package br.com.capivarabook.capivara_book;

import br.com.capivarabook.capivara_book.entity.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

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


    @Test
    @DisplayName("registrarEmprestimo() → cria Emprestimo ATIVO com prazo de 14 dias (RN08)")
    void isRegistrarEmprestimo() {
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
    void registrarEmprestimoClienteNoLimite() {
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
    void registrarEmprestimoLivroIndisponivel() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l = livroIndisponivel();

        assertThrows(IllegalStateException.class,
                () -> func.registrarEmprestimo(c, l, 14, new BigDecimal("2.00")));
    }

    @Test
    @DisplayName("registrarEmprestimo() → lança exceção quando duplicata ativa (RN03)")
    void registrarEmprestimoDuplicataAtiva() {
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

    @Test
    @DisplayName("registrarEmprestimo() → lança exceção quando cliente é nulo")
    void registrarEmprestimoClienteNulo() {
        Funcionario func = adminBase();
        Livro l = livroDisponivel();

        assertThrows(Exception.class,
                () -> func.registrarEmprestimo(null, l, 14, new BigDecimal("2.00")));
    }

    @Test
    @DisplayName("registrarEmprestimo() → lança exceção quando livro é nulo")
    void registrarEmprestimoLivroNulo() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();

        assertThrows(Exception.class,
                () -> func.registrarEmprestimo(c, null, 14, new BigDecimal("2.00")));
    }

    @Test
    @DisplayName("registrarEmprestimo() → decrementa exemplaresDisponiveis ao criar empréstimo")
    void registrarEmprestimoDecrementarExemplaresDisponiveis() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l = livroDisponivel();
        int exemplaresAntes = l.getExemplaresDisponiveis();

        func.registrarEmprestimo(c, l, 14, new BigDecimal("2.00"));

        assertEquals(exemplaresAntes - 1, l.getExemplaresDisponiveis());
    }

    @Test
    @DisplayName("registrarEmprestimo() → empréstimo de livro com apenas 1 exemplar disponível é permitido")
    void registrarEmprestimoUltimoExemplarDisponivel() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l = Livro.builder()
                .titulo("Livro Raro").autor("Autor").editora("Ed")
                .isbn("1234567890123").anoPublicacao(2000).genero(Genero.ROMANCE)
                .exemplares(1).exemplaresDisponiveis(1).status(StatusLivro.DISPONIVEL).build();

        Emprestimo emp = func.registrarEmprestimo(c, l, 14, new BigDecimal("2.00"));

        assertNotNull(emp);
        assertEquals(0, l.getExemplaresDisponiveis());
    }



    @Test
    @DisplayName("registrarDevolucao() → retorna multa 0 quando no prazo (CT07)")
    void registrarDevolucaoMultaZeroQuandoNoPrazo() {
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
    void registrarDevolucaoCalcularMultaQuandoAtrasado() {
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
    void registrarDevolucaoQuandoDevolvido() {
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

    @Test
    @DisplayName("registrarDevolucao() → lança exceção quando empréstimo é nulo")
    void registrarDevolucaoQuandoEmprestimoNulo() {
        Funcionario func = adminBase();

        assertThrows(Exception.class, () -> func.registrarDevolucao(null));
    }

    @Test
    @DisplayName("registrarDevolucao() → incrementa exemplaresDisponiveis ao devolver")
    void registrarDevolucaoIncrementarExemplaresDisponiveis() {
        Funcionario func = adminBase();
        Livro l = livroDisponivel();
        Cliente c = clienteDisponivel();
        int exemplaresAntes = l.getExemplaresDisponiveis();

        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(5))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(9))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        func.registrarDevolucao(emp);

        assertEquals(exemplaresAntes + 1, l.getExemplaresDisponiveis());
    }

    @Test
    @DisplayName("registrarDevolucao() → status do empréstimo é DEVOLVIDO após devolução em atraso")
    void registrarDevolucaoQuandoAtrasado() {
        Funcionario func = adminBase();
        Livro l = livroDisponivel();
        Cliente c = clienteDisponivel();
        int diasAtraso = 6;

        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(diasAtraso + 14))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(diasAtraso))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        func.registrarDevolucao(emp);

        assertEquals(StatusEmprestimo.DEVOLVIDO, emp.getStatus());
    }

    @Test
    @DisplayName("registrarDevolucao() → multa não é negativa quando no prazo")
    void registrarDevolucaoMultaNaoNegativaQuandoNoPrazo() {
        Funcionario func = adminBase();
        Livro l = livroDisponivel();
        Cliente c = clienteDisponivel();

        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(3))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(11))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        BigDecimal multa = func.registrarDevolucao(emp);

        assertTrue(multa.compareTo(BigDecimal.ZERO) >= 0);
    }



    @Test
    @DisplayName("statusLivro() → seta DISPONIVEL quando livro tem exemplares disponíveis")
    void statusLivroDisponivel() {
        Funcionario func = adminBase();
        Livro l = livroDisponivel();
        func.statusLivro(l, StatusLivro.DISPONIVEL);
        assertEquals(StatusLivro.DISPONIVEL, l.getStatus());
    }

    @Test
    @DisplayName("statusLivro() → lança exceção ao inativar livro com exemplares emprestados (RN06)")
    void statusLivroQuandoExemplaresEmprestados() {
        Funcionario func = adminBase();
        Livro l = Livro.builder()
                .titulo("Livro").autor("A").editora("E").isbn("1111111111111")
                .anoPublicacao(2020).genero(Genero.DRAMA)
                .exemplares(2).exemplaresDisponiveis(1)
                .status(StatusLivro.DISPONIVEL).build();

        assertThrows(IllegalStateException.class,
                () -> func.statusLivro(l, StatusLivro.INDISPONIVEL));
    }

    @Test
    @DisplayName("statusLivro() → lança exceção quando livro é nulo")
    void statusLivroNulo() {
        Funcionario func = adminBase();

        assertThrows(Exception.class,
                () -> func.statusLivro(null, StatusLivro.DISPONIVEL));
    }

    @Test
    @DisplayName("statusLivro() → permite INDISPONIVEL quando todos os exemplares estão disponíveis")
    void statusLivroIndisponivelQuandoTodosExemplaresDisponiveis() {
        Funcionario func = adminBase();
        Livro l = Livro.builder()
                .titulo("Livro Completo").autor("A").editora("E").isbn("9999999999999")
                .anoPublicacao(2021).genero(Genero.TECNOLOGIA)
                .exemplares(3).exemplaresDisponiveis(3)
                .status(StatusLivro.DISPONIVEL).build();

        assertDoesNotThrow(() -> func.statusLivro(l, StatusLivro.INDISPONIVEL));
        assertEquals(StatusLivro.INDISPONIVEL, l.getStatus());
    }

    @Test
    @DisplayName("statusLivro() → não altera status do livro quando lança exceção por exemplares emprestados")
    void statusLivroQuandoExcecaoExemplaresEmprestados() {
        Funcionario func = adminBase();
        Livro l = Livro.builder()
                .titulo("Livro").autor("A").editora("E").isbn("1111111111111")
                .anoPublicacao(2020).genero(Genero.DRAMA)
                .exemplares(2).exemplaresDisponiveis(1)
                .status(StatusLivro.DISPONIVEL).build();

        assertThrows(IllegalStateException.class,
                () -> func.statusLivro(l, StatusLivro.INDISPONIVEL));

        assertEquals(StatusLivro.DISPONIVEL, l.getStatus());
    }



    @Test
    @DisplayName("statusRenovacao() → delega para renovarEmprestimo() do Emprestimo")
    void statusRenovacaoEmprestimo() {
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

    @Test
    @DisplayName("statusRenovacao() → lança exceção quando empréstimo está em atraso (RN04)")
    void statusRenovacaoQuandoEmprestimoAtrasado() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l = livroDisponivel();
        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(20))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(6))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        assertThrows(IllegalStateException.class, () -> func.statusRenovacao(emp));
    }

    @Test
    @DisplayName("statusRenovacao() → lança exceção quando limite de renovações atingido (RN09)")
    void statusRenovacaoQuandoLimiteRenovacoesAtingido() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l = livroDisponivel();
        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(5))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(9))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(Emprestimo.LIMITE_RENOVACOES) // 2
                .multaDiaria(new BigDecimal("2.00")).build();

        assertThrows(IllegalStateException.class, () -> func.statusRenovacao(emp));
    }

    @Test
    @DisplayName("statusRenovacao() → lança exceção quando empréstimo já está DEVOLVIDO")
    void statusRenovacaoQuandoEmprestimoDevolvido() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l = livroDisponivel();
        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(20))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(6))
                .dataDevolucao(LocalDate.now().minusDays(1))
                .status(StatusEmprestimo.DEVOLVIDO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        assertThrows(IllegalStateException.class, () -> func.statusRenovacao(emp));
    }

    @Test
    @DisplayName("statusRenovacao() → lança exceção quando empréstimo é nulo")
    void statusRenovacaoQuandoEmprestimoNulo() {
        Funcionario func = adminBase();

        assertThrows(Exception.class, () -> func.statusRenovacao(null));
    }

    @Test
    @DisplayName("statusRenovacao() → não altera renovacoesRealizadas quando lança exceção por atraso")
    void statusRenovacaoQuandoExcecaoAtraso() {
        Funcionario func = adminBase();
        Cliente c = clienteDisponivel();
        Livro l = livroDisponivel();
        Emprestimo emp = Emprestimo.builder()
                .cliente(c).livro(l)
                .dataEmprestimo(LocalDate.now().minusDays(20))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(6))
                .status(StatusEmprestimo.ATIVO)
                .renovacoesRealizadas(0)
                .multaDiaria(new BigDecimal("2.00")).build();

        assertThrows(IllegalStateException.class, () -> func.statusRenovacao(emp));

        assertEquals(0, emp.getRenovacoesRealizadas());
    }
}