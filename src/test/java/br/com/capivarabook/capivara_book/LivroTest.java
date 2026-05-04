package br.com.capivarabook.capivara_book;

import br.com.capivarabook.capivara_book.entity.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

// Testa a lógica isolada da entidade Livro
@DisplayName("Livro — Testes Unitários")
class LivroTest {

    // ── Fábrica de livro disponível (3 de 5 exemplares) ───────
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

    // ── Fábrica de livro sem exemplares ───────────────────────
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

    // ══════════════════════════════════════════════════════════
    //  isDisponivel()
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("isDisponivel() → true quando exemplaresDisponiveis > 0")
    void isDisponivel_deveRetornarTrue_quandoHaExemplares() {
        // Cenário do PDF seção 6.5: "Verifica que isDisponivel() retorna
        // verdadeiro quando há exemplares disponíveis"
        Livro livro = livroDisponivel();
        assertTrue(livro.isDisponivel());
    }

    @Test
    @DisplayName("isDisponivel() → false quando exemplaresDisponiveis == 0")
    void isDisponivel_deveRetornarFalse_quandoSemExemplares() {
        Livro livro = livroIndisponivel();
        assertFalse(livro.isDisponivel());
    }

    @Test
    @DisplayName("isDisponivel() → false quando exemplaresDisponiveis é exatamente 0")
    void isDisponivel_limiteZero() {
        Livro livro = livroDisponivel();
        livro.setExemplaresDisponiveis(0);
        assertFalse(livro.isDisponivel());
    }

    // ══════════════════════════════════════════════════════════
    //  registrarEmprestimo()
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("registrarEmprestimo() → decrementa exemplaresDisponiveis")
    void registrarEmprestimo_deveDecrementar_quandoDisponivel() {
        // Cenário do PDF seção 6.5: "Verifica que registrarEmprestimo()
        // decrementa corretamente o contador"
        Livro livro = livroDisponivel();
        int antes = livro.getExemplaresDisponiveis(); // 3

        livro.registrarEmprestimo();

        assertEquals(antes - 1, livro.getExemplaresDisponiveis());
    }

    @Test
    @DisplayName("registrarEmprestimo() → status fica INDISPONIVEL após último exemplar")
    void registrarEmprestimo_statusFicaIndisponivel_quandoUltimoExemplar() {
        Livro livro = livroDisponivel();
        livro.setExemplaresDisponiveis(1);

        livro.registrarEmprestimo();

        assertEquals(0, livro.getExemplaresDisponiveis());
        assertEquals(StatusLivro.INDISPONIVEL, livro.getStatus());
    }

    @Test
    @DisplayName("registrarEmprestimo() → status permanece DISPONIVEL quando há mais exemplares")
    void registrarEmprestimo_statusPermanecDisponivel_quandoAindaHaExemplares() {
        Livro livro = livroDisponivel(); // 3 disponíveis

        livro.registrarEmprestimo();

        assertEquals(2, livro.getExemplaresDisponiveis());
        assertEquals(StatusLivro.DISPONIVEL, livro.getStatus());
    }

    @Test
    @DisplayName("registrarEmprestimo() → lança IllegalStateException quando sem exemplares (RN02)")
    void registrarEmprestimo_deveLancarExcecao_quandoSemExemplares() {
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
    void registrarEmprestimo_multiplosDecrementos() {
        Livro livro = livroDisponivel(); // 3 disponíveis

        livro.registrarEmprestimo(); // 2
        livro.registrarEmprestimo(); // 1
        livro.registrarEmprestimo(); // 0

        assertEquals(0, livro.getExemplaresDisponiveis());
        assertEquals(StatusLivro.INDISPONIVEL, livro.getStatus());

        // 4ª tentativa deve lançar exceção
        assertThrows(IllegalStateException.class, livro::registrarEmprestimo);
    }

    // ══════════════════════════════════════════════════════════
    //  registrarDevolucao() — RN05
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("registrarDevolucao() → incrementa exemplaresDisponiveis (RN05)")
    void registrarDevolucao_deveIncrementar() {
        Livro livro = livroDisponivel(); // 3 disponíveis
        livro.registrarEmprestimo();     // 2
        int antes = livro.getExemplaresDisponiveis();

        livro.registrarDevolucao();

        assertEquals(antes + 1, livro.getExemplaresDisponiveis());
    }

    @Test
    @DisplayName("registrarDevolucao() → status volta para DISPONIVEL após devolução")
    void registrarDevolucao_statusVoltaDisponivel() {
        Livro livro = livroIndisponivel(); // 0 disponíveis
        livro.setExemplares(2);

        livro.registrarDevolucao();

        assertEquals(1, livro.getExemplaresDisponiveis());
        assertEquals(StatusLivro.DISPONIVEL, livro.getStatus());
    }

    @Test
    @DisplayName("registrarDevolucao() → lança exceção quando todos exemplares já disponíveis")
    void registrarDevolucao_deveLancarExcecao_quandoTodosDisponiveis() {
        Livro livro = livroDisponivel();
        // exemplaresDisponiveis (3) == exemplares (5)? não, mas vamos igualar:
        livro.setExemplaresDisponiveis(livro.getExemplares());

        assertThrows(IllegalStateException.class, livro::registrarDevolucao);
    }

    // ══════════════════════════════════════════════════════════
    //  adicionarLivro()
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("adicionarLivro() → exemplaresDisponiveis = exemplares no cadastro inicial")
    void adicionarLivro_deveInicializarExemplares() {
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

    // ══════════════════════════════════════════════════════════
    //  statusLivro()
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("statusLivro() → DISPONIVEL quando exemplaresDisponiveis > 0")
    void statusLivro_deveSerDisponivel_quandoHaExemplares() {
        assertEquals(StatusLivro.DISPONIVEL, livroDisponivel().statusLivro());
    }

    @Test
    @DisplayName("statusLivro() → INDISPONIVEL quando exemplaresDisponiveis == 0")
    void statusLivro_deveSerIndisponivel_quandoSemExemplares() {
        assertEquals(StatusLivro.INDISPONIVEL, livroIndisponivel().statusLivro());
    }
}
