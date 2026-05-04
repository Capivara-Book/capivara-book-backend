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
    void isAtivo_deveRetornarTrue_quandoAtivo() {
        assertTrue(usuarioBase().isAtivo());
    }

    @Test
    @DisplayName("isAtivo() → false para StatusUsuario.INATIVO")
    void isAtivo_deveRetornarFalse_quandoInativo() {
        Cliente c = usuarioBase();
        c.setStatus(StatusUsuario.INATIVO);
        assertFalse(c.isAtivo());
    }

    @Test
    @DisplayName("inativar() → seta StatusUsuario.INATIVO")
    void inativar_deveSetarInativo() {
        Cliente c = usuarioBase();
        c.inativar();
        assertEquals(StatusUsuario.INATIVO, c.getStatus());
        assertFalse(c.isAtivo());
    }

    @Test
    @DisplayName("atualizarDados() → atualiza nome e email quando válidos")
    void atualizarDados_deveAtualizar_quandoValidos() {
        Cliente c = usuarioBase();
        c.atualizarDados("Novo Nome", "novo@email.com");
        assertEquals("Novo Nome", c.getNome());
        assertEquals("novo@email.com", c.getEmail());
    }

    @Test
    @DisplayName("atualizarDados() → ignora nome nulo")
    void atualizarDados_ignora_nomeNulo() {
        Cliente c = usuarioBase();
        String nomeOriginal = c.getNome();
        c.atualizarDados(null, "outro@email.com");
        assertEquals(nomeOriginal, c.getNome());
    }

    @Test
    @DisplayName("atualizarDados() → ignora email blank")
    void atualizarDados_ignora_emailBlank() {
        Cliente c = usuarioBase();
        String emailOriginal = c.getEmail();
        c.atualizarDados("Nome Novo", "  ");
        assertEquals(emailOriginal, c.getEmail());
    }
}