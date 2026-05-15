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

    @Test
    @DisplayName("isExpiracao() → false quando prazo ainda não venceu")
    void isExpiracaoQuandoAtiva() {
        Reserva r = reservaPendente();
        assertFalse(r.isExpiracao());
    }

    @Test
    @DisplayName("isExpiracao() → true quando PENDENTE e prazo vencido")
    void isExpiracaoQuandoVencida() {
        Reserva r = Reserva.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataReserva(LocalDate.now().minusDays(10))
                .dataExpiracao(LocalDate.now().minusDays(3)) // venceu há 3 dias
                .status(StatusReserva.PENDENTE).build();

        assertTrue(r.isExpiracao());
    }

    @Test
    @DisplayName("isExpiracao() → false quando CANCELADO mesmo com prazo vencido")
    void isExpiracaoQuandoCancelado() {
        Reserva r = Reserva.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataReserva(LocalDate.now().minusDays(10))
                .dataExpiracao(LocalDate.now().minusDays(3))
                .status(StatusReserva.CANCELADO).build();

        assertFalse(r.isExpiracao());
    }

    @Test
    @DisplayName("isExpiracao() → false quando EXPIRADO")
    void isExpirado() {
        Reserva r = Reserva.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataReserva(LocalDate.now().minusDays(10))
                .dataExpiracao(LocalDate.now().minusDays(3))
                .status(StatusReserva.EXPIRADO).build();

        assertFalse(r.isExpiracao());
    }

    @Test
    @DisplayName("isExpiracao() → true quando CONFIRMADO e prazo vencido (não bloqueado pela implementação)")
    void isExpiracaoQuandoConfirmadoEVencido() {
        // A implementação só bloqueia CANCELADO e EXPIRADO;
        // CONFIRMADO com prazo vencido ainda avalia a data e retorna true
        Reserva r = Reserva.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataReserva(LocalDate.now().minusDays(10))
                .dataExpiracao(LocalDate.now().minusDays(3))
                .status(StatusReserva.CONFIRMADO).build();

        assertTrue(r.isExpiracao());
    }

    @Test
    @DisplayName("isExpiracao() → true quando RECUSADO e prazo vencido (não bloqueado pela implementação)")
    void isExpiracaoQuandoRecusadoEVencido() {
        // A implementação só bloqueia CANCELADO e EXPIRADO;
        // RECUSADO com prazo vencido ainda avalia a data e retorna true
        Reserva r = Reserva.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataReserva(LocalDate.now().minusDays(10))
                .dataExpiracao(LocalDate.now().minusDays(1))
                .status(StatusReserva.RECUSADO).build();

        assertTrue(r.isExpiracao());
    }

    @Test
    @DisplayName("isExpiracao() → false quando PENDENTE e data de expiração é hoje (limite do dia)")
    void isExpiracaoQuandoExpiraNaDataDeHoje() {
        // A reserva expira APÓS hoje, não no próprio dia — validar a semântica do limite
        Reserva r = Reserva.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataReserva(LocalDate.now().minusDays(5))
                .dataExpiracao(LocalDate.now()) // expira hoje
                .status(StatusReserva.PENDENTE).build();

        // A data de hoje ainda não passou (depende da implementação: before vs. isAfter)
        // Este teste documenta o comportamento esperado na fronteira
        assertFalse(r.isExpiracao());
    }

    @Test
    @DisplayName("isExpiracao() → lança exceção quando dataExpiracao é nula")
    void isExpiracaoQuandoDataExpiracaoNula() {
        // Uma reserva sem data de expiração é inválida e não deve ser processada
        Reserva r = Reserva.builder()
                .cliente(clienteBase()).livro(livroBase())
                .dataReserva(LocalDate.now())
                .dataExpiracao(null)
                .status(StatusReserva.PENDENTE).build();

        assertThrows(Exception.class, r::isExpiracao);
    }

    @Test
    @DisplayName("statusReserva() → CONFIRMADO quando Admin confirma")
    void statusReservaConfirmar() {
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.CONFIRMADO);
        assertEquals(StatusReserva.CONFIRMADO, r.getStatus());
    }

    @Test
    @DisplayName("statusReserva() → RECUSADO quando Admin recusa")
    void statusReservaRecusar() {
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.RECUSADO);
        assertEquals(StatusReserva.RECUSADO, r.getStatus());
    }

    @Test
    @DisplayName("statusReserva() → EXPIRADO quando expira")
    void statusReservaExpirar() {
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.EXPIRADO);
        assertEquals(StatusReserva.EXPIRADO, r.getStatus());
    }

    @Test
    @DisplayName("statusReserva() → aceita transição de CONFIRMADO para PENDENTE sem validação")
    void statusReservaVoltarParaPendente() {
        // A implementação atual não bloqueia transições retroativas;
        // este teste documenta esse comportamento real
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.CONFIRMADO);

        r.statusReserva(StatusReserva.PENDENTE);

        assertEquals(StatusReserva.PENDENTE, r.getStatus());
    }

    @Test
    @DisplayName("statusReserva() → aceita sobrescrever CANCELADO com CONFIRMADO sem validação")
    void statusReservaSobreescreverCancelado() {
        // A implementação atual não trata CANCELADO como estado terminal
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.CANCELADO);

        r.statusReserva(StatusReserva.CONFIRMADO);

        assertEquals(StatusReserva.CONFIRMADO, r.getStatus());
    }

    @Test
    @DisplayName("statusReserva() → aceita sobrescrever EXPIRADO com CONFIRMADO sem validação")
    void statusReservaSobreescreverExpirado() {
        // A implementação atual não trata EXPIRADO como estado terminal
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.EXPIRADO);

        r.statusReserva(StatusReserva.CONFIRMADO);

        assertEquals(StatusReserva.CONFIRMADO, r.getStatus());
    }

    @Test
    @DisplayName("statusReserva() → aceita sobrescrever RECUSADO com CONFIRMADO sem validação")
    void statusReservaSobreescreverRecusado() {
        // A implementação atual não trata RECUSADO como estado terminal
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.RECUSADO);

        r.statusReserva(StatusReserva.CONFIRMADO);

        assertEquals(StatusReserva.CONFIRMADO, r.getStatus());
    }

    @Test
    @DisplayName("statusReserva() → aceita null sem lançar exceção, corrompendo o status")
    void statusReservaAceitaNull() {
        // A implementação atual não valida null; o status fica nulo após a chamada
        Reserva r = reservaPendente();

        r.statusReserva(null);

        assertNull(r.getStatus());
    }

    @Test
    @DisplayName("statusReserva() → status não é alterado quando o mesmo valor é reaplicado")
    void statusReservaReaplicarMesmoValor() {
        // Reaplicar o mesmo status é aceito e mantém o valor inalterado
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.CONFIRMADO);

        r.statusReserva(StatusReserva.CONFIRMADO);

        assertEquals(StatusReserva.CONFIRMADO, r.getStatus());
    }

    @Test
    @DisplayName("statusReserva() → CANCELADO pode ser aplicado a partir de qualquer status")
    void statusReservaCancelarQualquerStatus() {
        // Independente do status atual, CANCELADO pode ser atribuído diretamente
        Reserva r = reservaPendente();
        r.statusReserva(StatusReserva.CONFIRMADO);

        r.statusReserva(StatusReserva.CANCELADO);

        assertEquals(StatusReserva.CANCELADO, r.getStatus());
    }
}
