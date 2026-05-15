package br.com.capivarabook.capivara_book;

import br.com.capivarabook.capivara_book.entity.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

// Testa a lógica isolada da entidade Usuario
@DisplayName("Usuario — Testes Unitários")
class UsuarioTest {

    // Usuario é abstrata — testamos via Cliente (subclasse concreta)
    private Cliente usuarioBase() {
        Cliente c = new Cliente();
        c.setNome("Maria Silva");
        c.setEmail("maria@email.com");
        c.setSenha("senha123");
        c.setCpf("12345678901");
        c.setTelefone("11987654321");
        c.setStatus(StatusUsuario.ATIVO);
        c.setRole(Role.CLIENTE);
        return c;
    }

    @Test
    @DisplayName("isAtivo() → true para StatusUsuario.ATIVO")
    void isAtivoPadrao() {
        assertTrue(usuarioBase().isAtivo());
    }

    @Test
    @DisplayName("isAtivo() → false para StatusUsuario.INATIVO")
    void isAtivoQuandoInativo() {
        Cliente c = usuarioBase();
        c.setStatus(StatusUsuario.INATIVO);
        assertFalse(c.isAtivo());
    }

    // ── Cenários de erro — isAtivoPadrao() ──────────────────────

    @Test
    @DisplayName("isAtivo() → false quando status é nulo")
    void isAtivoQuandoStatusNulo() {
        Cliente c = usuarioBase();
        c.setStatus(null);
        assertFalse(c.isAtivo());
    }

    @Test
    @DisplayName("inativar() → seta StatusUsuario.INATIVO")
    void isInativo() {
        Cliente c = usuarioBase();
        c.inativar();
        assertEquals(StatusUsuario.INATIVO, c.getStatus());
        assertFalse(c.isAtivo());
    }

    // ── Cenários de erro — isInativo() ─────────────────────

    @Test
    @DisplayName("inativar() → idempotente: chamar duas vezes mantém INATIVO")
    void inativarDeveSerIdempotente() {
        Cliente c = usuarioBase();
        c.inativar();
        c.inativar();
        assertEquals(StatusUsuario.INATIVO, c.getStatus());
        assertFalse(c.isAtivo());
    }

    @Test
    @DisplayName("inativar() → não lança exceção quando status já é nulo")
    void inativarQuandoStatusNulo() {
        Cliente c = usuarioBase();
        c.setStatus(null);
        assertDoesNotThrow(c::inativar);
        assertEquals(StatusUsuario.INATIVO, c.getStatus());
    }

    @Test
    @DisplayName("atualizarDados() → atualiza nome e email quando válidos")
    void atualizarDadosValidos() {
        Cliente c = usuarioBase();
        c.atualizarDados("Novo Nome", "novo@email.com");
        assertEquals("Novo Nome", c.getNome());
        assertEquals("novo@email.com", c.getEmail());
    }

    @Test
    @DisplayName("atualizarDados() → ignora nome nulo")
    void atualizarDadosIgnoraNomeNulo() {
        Cliente c = usuarioBase();
        String nomeOriginal = c.getNome();
        c.atualizarDados(null, "outro@email.com");
        assertEquals(nomeOriginal, c.getNome());
    }

    @Test
    @DisplayName("atualizarDados() → ignora email blank")
    void atualizarDadosIgnoraEmailBlank() {
        Cliente c = usuarioBase();
        String emailOriginal = c.getEmail();
        c.atualizarDados("Nome Novo", "  ");
        assertEquals(emailOriginal, c.getEmail());
    }

    // ── Cenários de erro — atualizarDados() ───────────────

    @Test
    @DisplayName("atualizarDados() → ignora nome blank (só espaços)")
    void atualizarDadosIgnoraNomeBlank() {
        Cliente c = usuarioBase();
        String nomeOriginal = c.getNome();
        c.atualizarDados("   ", "novo@email.com");
        assertEquals(nomeOriginal, c.getNome());
    }

    @Test
    @DisplayName("atualizarDados() → ignora email nulo")
    void atualizarDadosIgnoraEmailNulo() {
        Cliente c = usuarioBase();
        String emailOriginal = c.getEmail();
        c.atualizarDados("Nome Novo", null);
        assertEquals(emailOriginal, c.getEmail());
    }

    @Test
    @DisplayName("atualizarDados() → ambos nulos: nenhum campo é alterado")
    void atualizarDadosAmbosNulosNaoAltera() {
        Cliente c = usuarioBase();
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
        Cliente c = usuarioBase();
        String nomeOriginal = c.getNome();
        String emailOriginal = c.getEmail();
        c.atualizarDados("", "");
        assertAll(
                () -> assertEquals(nomeOriginal, c.getNome()),
                () -> assertEquals(emailOriginal, c.getEmail())
        );
    }
}