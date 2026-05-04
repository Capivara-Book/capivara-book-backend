package br.com.capivarabook.capivara_book;

import br.com.capivarabook.capivara_book.entity.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

// Testa a lógica isolada da entidade Reserva
@DisplayName("Reserva — Testes Unitários")
class ReservaTest {

    private Cliente clienteBase() {
        Cliente c = new Cliente();
        c.setNome("Ana"); c.setEmail("ana@email.com");
        c.setSenha("s"); c.setCpf("45678912300"); c.setTelefone("21998765432");
        c.setStatus(StatusUsuario.ATIVO); c.setRole(Role.CLIENTE);
        return c;
    }

    private Livro livroBase() {
        return Livro.builder()
                .titulo("It").autor("Stephen King").editora("Suma")
                .isbn("9788576655336").anoPublicacao(1986).genero(Genero.MISTERIO)
                .exemplares(2).exemplaresDisponiveis(0).status(StatusLivro.INDISPONIVEL).build();
    }

    private Reserva reservaPendente() {
        return Reserva.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataReserva(LocalDate.now().minusDays(2))
                .dataExpiracao(LocalDate.now().plusDays(5))
                .status(StatusReserva.PENDENTE).build();
    }

    // ── isExpiracao() ─────────────────────────────────────────

    @Test
    @DisplayName("isExpiracao() → false quando prazo ainda não venceu")
    void isExpiracao_deveRetornarFalse_quandoAtiva() {
        Reserva r = reservaPendente();
        assertFalse(r.isExpiracao());
    }

    @Test
    @DisplayName("isExpiracao() → true quando PENDENTE e prazo vencido")
    void isExpiracao_deveRetornarTrue_quandoVencida() {
        Reserva r = Reserva.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataReserva(LocalDate.now().minusDays(10))
                .dataExpiracao(LocalDate.now().minusDays(3)) // venceu há 3 dias
                .status(StatusReserva.PENDENTE).build();

        assertTrue(r.isExpiracao());
    }

    @Test
    @DisplayName("isExpiracao() → false quando CANCELADO mesmo com prazo vencido")
    void isExpiracao_deveRetornarFalse_quandoCancelado() {
        Reserva r = Reserva.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataReserva(LocalDate.now().minusDays(10))
                .dataExpiracao(LocalDate.now().minusDays(3))
                .status(StatusReserva.CANCELADO).build();

        assertFalse(r.isExpiracao());
    }

    @Test
    @DisplayName("isExpiracao() → false quando EXPIRADO")
    void isExpiracao_deveRetornarFalse_quandoJaExpirado() {
        Reserva r = Reserva.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataReserva(LocalDate.now().minusDays(10))
                .dataExpiracao(LocalDate.now().minusDays(3))
                .status(StatusReserva.EXPIRADO).build();

        assertFalse(r.isExpiracao());
    }

    // ── statusReserva() ───────────────────────────────────────

    @Test
    @DisplayName("statusReserva() → CONFIRMADO quando Admin confirma")
    void statusReserva_deveConfirmar() {
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.CONFIRMADO);
        assertEquals(StatusReserva.CONFIRMADO, r.getStatus());
    }

    @Test
    @DisplayName("statusReserva() → RECUSADO quando Admin recusa")
    void statusReserva_deveRecusar() {
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.RECUSADO);
        assertEquals(StatusReserva.RECUSADO, r.getStatus());
    }

    @Test
    @DisplayName("statusReserva() → EXPIRADO quando expira")
    void statusReserva_deveExpirar() {
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.EXPIRADO);
        assertEquals(StatusReserva.EXPIRADO, r.getStatus());
    }
}