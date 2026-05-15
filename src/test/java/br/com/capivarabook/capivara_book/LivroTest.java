package br.com.capivarabook.capivara_book;

import br.com.capivarabook.capivara_book.entity.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Livro — Testes Unitários")
class LivroTest {


    private Livro livroDisponivel() {
        return Livro.builder()
                .titulo("Dom Casmurro")
                .autor("Machado de Assis")
                .editora("Companhia das Letras")
                .isbn("9788535909555")
                .anoPublicacao(1899)
                .genero(Genero.ROMANCE)
                .exemplares(5)
                .exemplaresDisponiveis(3)
                .status(StatusLivro.DISPONIVEL)
                .build();
    }

    private Livro livroIndisponivel() {
        return Livro.builder()
                .titulo("It: A Coisa")
                .autor("Stephen King")
                .editora("Suma")
                .isbn("9788576655336")
                .anoPublicacao(1986)
                .genero(Genero.MISTERIO)
                .exemplares(2)
                .exemplaresDisponiveis(0)
                .status(StatusLivro.INDISPONIVEL)
                .build();
    }


    @Test
    @DisplayName("isDisponivel() → true quando exemplaresDisponiveis > 0")
    void isDisponivelQuandoTemExemplares() {
        // Cenário do PDF seção 6.5: "Verifica que isDisponivel() retorna
        // verdadeiro quando há exemplares disponíveis"
        Livro livro = livroDisponivel();
        assertTrue(livro.isDisponivel());
    }

    @Test
    @DisplayName("isDisponivel() → false quando exemplaresDisponiveis == 0")
    void isDisponivelQuandoSemExemplares() {
        Livro livro = livroIndisponivel();
        assertFalse(livro.isDisponivel());
    }

    @Test
    @DisplayName("isDisponivel() → false quando exemplaresDisponiveis é exatamente 0")
    void isDisponivelLimiteZero() {
        Livro livro = livroDisponivel();
        livro.setExemplaresDisponiveis(0);
        assertFalse(livro.isDisponivel());
    }

    @Test
    @DisplayName("isDisponivel() → false quando exemplaresDisponiveis é negativo (estado inválido)")
    void isDisponivelQuandoExemplaresNegativos() {
        // Protege contra corrupção de estado: contadores negativos não representam disponibilidade
        Livro livro = livroDisponivel();
        livro.setExemplaresDisponiveis(-1);
        assertFalse(livro.isDisponivel());
    }

    @Test
    @DisplayName("isDisponivel() → coerência com status INDISPONIVEL quando sem exemplares")
    void isDisponivelComStatusIndisponivel() {
        // isDisponivel() e getStatus() devem concordar quando exemplaresDisponiveis == 0
        Livro livro = livroIndisponivel();
        assertFalse(livro.isDisponivel());
        assertEquals(StatusLivro.INDISPONIVEL, livro.getStatus());
    }


    @Test
    @DisplayName("registrarEmprestimo() → decrementa exemplaresDisponiveis")
    void registrarEmprestimoQuandoDisponivel() {
        // Cenário do PDF seção 6.5: "Verifica que registrarEmprestimo()
        // decrementa corretamente o contador"
        Livro livro = livroDisponivel();
        int antes = livro.getExemplaresDisponiveis(); // 3

        livro.registrarEmprestimo();

        assertEquals(antes - 1, livro.getExemplaresDisponiveis());
    }

    @Test
    @DisplayName("registrarEmprestimo() → status fica INDISPONIVEL após último exemplar")
    void registrarEmprestimoQuandoUltimoExemplar() {
        Livro livro = livroDisponivel();
        livro.setExemplaresDisponiveis(1);

        livro.registrarEmprestimo();

        assertEquals(0, livro.getExemplaresDisponiveis());
        assertEquals(StatusLivro.INDISPONIVEL, livro.getStatus());
    }

    @Test
    @DisplayName("registrarEmprestimo() → status permanece DISPONIVEL quando há mais exemplares")
    void registrarEmprestimoQuandoAindaTemExemplares() {
        Livro livro = livroDisponivel(); // 3 disponíveis

        livro.registrarEmprestimo();

        assertEquals(2, livro.getExemplaresDisponiveis());
        assertEquals(StatusLivro.DISPONIVEL, livro.getStatus());
    }

    @Test
    @DisplayName("registrarEmprestimo() → lança IllegalStateException quando sem exemplares (RN02)")
    void registrarEmprestimoQuandoSemExemplares() {
        // Cenário do PDF seção 6.5: "Verifica que registrarEmprestimo()
        // lança IllegalStateException quando não há exemplares"
        Livro livro = livroIndisponivel();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                livro::registrarEmprestimo
        );
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("registrarEmprestimo() → múltiplos empréstimos decrementam corretamente")
    void registrarEmprestimoMultiplosDecrementos() {
        Livro livro = livroDisponivel(); // 3 disponíveis

        livro.registrarEmprestimo(); // 2
        livro.registrarEmprestimo(); // 1
        livro.registrarEmprestimo(); // 0

        assertEquals(0, livro.getExemplaresDisponiveis());
        assertEquals(StatusLivro.INDISPONIVEL, livro.getStatus());

        // 4ª tentativa deve lançar exceção
        assertThrows(IllegalStateException.class, livro::registrarEmprestimo);
    }

    @Test
    @DisplayName("registrarEmprestimo() → mensagem da exceção não é vazia (RN02)")
    void registrarEmprestimoNaoDeveSerVazia() {
        // A mensagem deve ser informativa, não uma string vazia
        Livro livro = livroIndisponivel();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                livro::registrarEmprestimo
        );
        assertFalse(ex.getMessage().isBlank());
    }

    @Test
    @DisplayName("registrarEmprestimo() → exemplares totais não são alterados, apenas os disponíveis")
    void registrarEmprestimoNaoDeveAlterarExemplaresTotais() {
        Livro livro = livroDisponivel();
        int totalAntes = livro.getExemplares();

        livro.registrarEmprestimo();

        assertEquals(totalAntes, livro.getExemplares());
    }

    @Test
    @DisplayName("registrarEmprestimo() → exemplaresDisponiveis nunca fica negativo após exceção")
    void registrarEmprestimoNaoNegativaAposExcecao() {
        // Garante que o estado não é corrompido quando a exceção é lançada
        Livro livro = livroIndisponivel(); // 0 disponíveis

        assertThrows(IllegalStateException.class, livro::registrarEmprestimo);

        assertEquals(0, livro.getExemplaresDisponiveis());
    }

    @Test
    @DisplayName("registrarDevolucao() → incrementa exemplaresDisponiveis (RN05)")
    void registrarDevolucaoDeveIncrementar() {
        Livro livro = livroDisponivel(); // 3 disponíveis
        livro.registrarEmprestimo();     // 2
        int antes = livro.getExemplaresDisponiveis();

        livro.registrarDevolucao();

        assertEquals(antes + 1, livro.getExemplaresDisponiveis());
    }

    @Test
    @DisplayName("registrarDevolucao() → status volta para DISPONIVEL após devolução")
    void registrarDevolucaoStatusDisponivel() {
        Livro livro = livroIndisponivel(); // 0 disponíveis
        livro.setExemplares(2);

        livro.registrarDevolucao();

        assertEquals(1, livro.getExemplaresDisponiveis());
        assertEquals(StatusLivro.DISPONIVEL, livro.getStatus());
    }

    @Test
    @DisplayName("registrarDevolucao() → lança exceção quando todos exemplares já disponíveis")
    void registrarDevolucaoQuandoTodosDisponiveis() {
        Livro livro = livroDisponivel();
        // exemplaresDisponiveis (3) == exemplares (5)? não, mas vamos igualar:
        livro.setExemplaresDisponiveis(livro.getExemplares());

        assertThrows(IllegalStateException.class, livro::registrarDevolucao);
    }

    @Test
    @DisplayName("registrarDevolucao() → mensagem da exceção não é vazia ao devolver além do limite")
    void registrarDevolucaoMensagemQuandoTodosDisponiveis() {
        // A mensagem deve ser informativa para facilitar o diagnóstico
        Livro livro = livroDisponivel();
        livro.setExemplaresDisponiveis(livro.getExemplares());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                livro::registrarDevolucao
        );
        assertFalse(ex.getMessage().isBlank());
    }

    @Test
    @DisplayName("registrarDevolucao() → exemplares totais não são alterados, apenas os disponíveis")
    void registrarDevolucaoNaoDeveAlterarExemplaresTotais() {
        Livro livro = livroIndisponivel();
        livro.setExemplares(2);
        int totalAntes = livro.getExemplares();

        livro.registrarDevolucao();

        assertEquals(totalAntes, livro.getExemplares());
    }

    @Test
    @DisplayName("registrarDevolucao() → estado não é corrompido quando exceção é lançada")
    void registrarDevolucaoNaoDeveAlterarStatusAposExcecao() {
        // Garante atomicidade: se não pode devolver, o contador não muda
        Livro livro = livroDisponivel();
        livro.setExemplaresDisponiveis(livro.getExemplares());
        int disponivelAntes = livro.getExemplaresDisponiveis();

        assertThrows(IllegalStateException.class, livro::registrarDevolucao);

        assertEquals(disponivelAntes, livro.getExemplaresDisponiveis());
    }

    @Test
    @DisplayName("registrarDevolucao() → devolução após empréstimo restaura o estado original")
    void isRegistrarDevolucao() {
        Livro livro = livroDisponivel();
        int disponivelOriginal = livro.getExemplaresDisponiveis();
        StatusLivro statusOriginal = livro.getStatus();

        livro.registrarEmprestimo();
        livro.registrarDevolucao();

        assertEquals(disponivelOriginal, livro.getExemplaresDisponiveis());
        assertEquals(statusOriginal, livro.getStatus());
    }


    @Test
    @DisplayName("adicionarLivro() → exemplaresDisponiveis = exemplares no cadastro inicial")
    void adicionarLivroDeveInicializarExemplares() {
        Livro livro = Livro.builder()
                .titulo("Novo Livro").autor("Autor").editora("Editora")
                .isbn("9780000000001").anoPublicacao(2024)
                .genero(Genero.TECNOLOGIA)
                .exemplares(4).exemplaresDisponiveis(0)
                .status(StatusLivro.INDISPONIVEL)
                .build();

        livro.adicionarLivro();

        assertEquals(4, livro.getExemplaresDisponiveis());
        assertEquals(StatusLivro.DISPONIVEL, livro.getStatus());
    }

    @Test
    @DisplayName("adicionarLivro() → exemplaresDisponiveis fica 0 quando exemplares é zero")
    void adicionarLivroComExemplaresZero() {
        // adicionarLivro() espelha exemplares em exemplaresDisponiveis;
        // com exemplares == 0 o resultado disponível também é 0
        Livro livro = Livro.builder()
                .titulo("Sem Estoque").autor("Autor").editora("Editora")
                .isbn("9780000000002").anoPublicacao(2024)
                .genero(Genero.TECNOLOGIA)
                .exemplares(0).exemplaresDisponiveis(5)
                .status(StatusLivro.INDISPONIVEL)
                .build();

        livro.adicionarLivro();

        assertEquals(0, livro.getExemplaresDisponiveis());
    }

    @Test
    @DisplayName("adicionarLivro() → isDisponivel() retorna false quando exemplares é zero")
    void adicionarLivroExemplaresIndisponivel() {
        // Após adicionarLivro() com exemplares == 0, o livro não deve ser considerado disponível
        Livro livro = Livro.builder()
                .titulo("Sem Estoque 2").autor("Autor").editora("Editora")
                .isbn("9780000000003").anoPublicacao(2024)
                .genero(Genero.TECNOLOGIA)
                .exemplares(0).exemplaresDisponiveis(0)
                .status(StatusLivro.INDISPONIVEL)
                .build();

        livro.adicionarLivro();

        assertFalse(livro.isDisponivel());
    }

    @Test
    @DisplayName("adicionarLivro() → não altera exemplares totais ao inicializar disponíveis")
    void adicionarLivroNaoDeveAlterarExemplaresTotais() {
        Livro livro = Livro.builder()
                .titulo("Livro Válido").autor("Autor").editora("Editora")
                .isbn("9780000000004").anoPublicacao(2024)
                .genero(Genero.TECNOLOGIA)
                .exemplares(6).exemplaresDisponiveis(0)
                .status(StatusLivro.INDISPONIVEL)
                .build();

        livro.adicionarLivro();

        assertEquals(6, livro.getExemplares());
    }


    @Test
    @DisplayName("statusLivro() → DISPONIVEL quando exemplaresDisponiveis > 0")
    void statusLivroDisponivelQuandoHaExemplares() {
        assertEquals(StatusLivro.DISPONIVEL, livroDisponivel().statusLivro());
    }

    @Test
    @DisplayName("statusLivro() → INDISPONIVEL quando exemplaresDisponiveis == 0")
    void statusLivroIndisponivelQuandoSemExemplares() {
        assertEquals(StatusLivro.INDISPONIVEL, livroIndisponivel().statusLivro());
    }

    @Test
    @DisplayName("statusLivro() → coerente com getStatus() após registrarEmprestimo()")
    void statusLivroAposEmprestimo() {
        // statusLivro() e getStatus() devem sempre retornar o mesmo valor
        Livro livro = livroDisponivel();
        livro.setExemplaresDisponiveis(1);
        livro.registrarEmprestimo();

        assertEquals(livro.getStatus(), livro.statusLivro());
    }

    @Test
    @DisplayName("statusLivro() → coerente com getStatus() após registrarDevolucao()")
    void statusLivroAposDevolucao() {
        Livro livro = livroIndisponivel();
        livro.setExemplares(2);
        livro.registrarDevolucao();

        assertEquals(livro.getStatus(), livro.statusLivro());
    }

    @Test
    @DisplayName("statusLivro() → INDISPONIVEL quando exemplaresDisponiveis é negativo (estado inválido)")
    void statusLivroExemplaresNegativos() {
        // Estado corrompido não deve ser tratado como disponível
        Livro livro = livroDisponivel();
        livro.setExemplaresDisponiveis(-5);

        assertEquals(StatusLivro.INDISPONIVEL, livro.statusLivro());
    }
}
