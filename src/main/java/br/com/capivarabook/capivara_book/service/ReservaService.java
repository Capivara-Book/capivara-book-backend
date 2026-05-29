package br.com.capivarabook.capivara_book.service;

import br.com.capivarabook.capivara_book.dto.response.ReservaResponseDTO;
import br.com.capivarabook.capivara_book.entity.*;
import br.com.capivarabook.capivara_book.exception.*;
import br.com.capivarabook.capivara_book.repository.*;
import br.com.capivarabook.capivara_book.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final IReservaRepository reservaRepository;
    private final LivroService       livroService;
    private final UsuarioService     usuarioService;

    private static final int DIAS_EXPIRACAO = 7;

    @Transactional
    public ReservaResponseDTO reservarLivro(Long clienteId, Long livroId) {
        Cliente c = usuarioService.buscarCliente(clienteId);
        Livro   l = livroService.buscarLivro(livroId);

        if (l.isDisponivel())
            throw new BusinessException("Livro disponível — realize o empréstimo diretamente.");
        if (reservaRepository.existsReservaAtiva(clienteId, livroId))
            throw new BusinessException("Cliente já possui pré-reserva ativa para este livro.");

        Reserva r = Reserva.builder()
                .cliente(c).livro(l)
                .dataReserva(LocalDate.now())
                .dataExpiracao(LocalDate.now().plusDays(DIAS_EXPIRACAO))
                .status(StatusReserva.PENDENTE)
                .build();
        return ReservaResponseDTO.from(reservaRepository.save(r));
    }

    // CORRIGIDO — C2 (IDOR)
    // Valida que o clienteId do body pertence ao usuário autenticado
    // antes de cancelar. Admin/gerente passam sem restrição.
    @Transactional
    public ReservaResponseDTO cancelarReserva(Long resId, Long clienteId, Authentication auth) {
        Reserva r = buscarReserva(resId);

        if (!usuarioService.isAdminOuGerente(auth)) {
            CustomUserDetails me = (CustomUserDetails) auth.getPrincipal();
            if (!me.getIdUser().equals(clienteId))
                throw new BusinessException("Acesso negado: você só pode cancelar suas próprias reservas.");
        }

        if (!r.getCliente().getId().equals(clienteId))
            throw new BusinessException("Esta reserva não pertence ao cliente informado.");

        usuarioService.buscarCliente(clienteId).cancelarReserva(r);
        return ReservaResponseDTO.from(reservaRepository.save(r));
    }

    @Transactional
    public ReservaResponseDTO atualizarStatus(Long resId, Long funcId, StatusReserva novoStatus) {
        Reserva r = buscarReserva(resId);
        usuarioService.buscarFuncionario(funcId).statusReserva(r, novoStatus);
        return ReservaResponseDTO.from(reservaRepository.save(r));
    }

    // CORRIGIDO — C5
    // Este método era chamado via endpoint HTTP (POST /reservas/expirar),
    // o que expunha uma operação administrativa à internet sem necessidade.
    // Agora é chamado exclusivamente pelo ReservaScheduler (@Scheduled).
    // O endpoint foi removido do ReservaController.
    @Transactional
    public void expirarReservasVencidas() {
        List<Reserva> candidatas = reservaRepository.findByStatusIn(
                List.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADO));

        List<Reserva> vencidas = candidatas.stream()
                .filter(Reserva::isExpiracao)
                .peek(r -> r.setStatus(StatusReserva.EXPIRADO))
                .toList();

        if (!vencidas.isEmpty())
            reservaRepository.saveAll(vencidas);
    }

    // CORRIGIDO — C2 (IDOR)
    // Admin/gerente veem qualquer cliente; cliente autenticado só vê os próprios.
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarPorCliente(Long clienteId, Authentication auth) {
        if (!usuarioService.isAdminOuGerente(auth)) {
            CustomUserDetails me = (CustomUserDetails) auth.getPrincipal();
            if (!me.getIdUser().equals(clienteId))
                throw new BusinessException("Acesso negado: você só pode consultar suas próprias reservas.");
        }
        return reservaRepository.findByClienteId(clienteId).stream()
                .map(ReservaResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarPendentes() {
        return reservaRepository.findByStatus(StatusReserva.PENDENTE).stream()
                .map(ReservaResponseDTO::from).toList();
    }

    // CORRIGIDO — C8
    // findAll() expunha reservas CANCELADAS e EXPIRADAS misturadas com as ativas.
    // Agora o admin recebe todas mas pode filtrar por status via parâmetro.
    // Passar null retorna tudo (comportamento original, sem filtro).
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarTodas(StatusReserva filtro) {
        List<Reserva> reservas = (filtro != null)
                ? reservaRepository.findByStatus(filtro)
                : reservaRepository.findAll();
        return reservas.stream().map(ReservaResponseDTO::from).toList();
    }

    // CORRIGIDO — I3
    // @Transactional adicionado para evitar LazyInitializationException
    // ao acessar cliente/livro (LAZY) fora de contexto transacional.
    @Transactional(readOnly = true)
    public Reserva buscarReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
    }
}
