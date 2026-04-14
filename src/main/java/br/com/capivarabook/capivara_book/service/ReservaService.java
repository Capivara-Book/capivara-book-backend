package br.com.capivarabook.capivara_book.service;

import br.com.capivarabook.capivara_book.dto.response.ReservaResponseDTO;
import br.com.capivarabook.capivara_book.entity.*;
import br.com.capivarabook.capivara_book.exception.*;
import br.com.capivarabook.capivara_book.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Service
@RequiredArgsConstructor
public class ReservaService {
    private final IReservaRepository reservaRepository;
    private final LivroService livroService;
    private final UsuarioService usuarioService;
    private static final int DIAS_EXPIRACAO = 7;

//    public ReservaService(IReservaRepository r, LivroService l, UsuarioService u) {
//        reservaRepository = r; livroService = l; usuarioService = u;
//    }

    @Transactional
    public ReservaResponseDTO reservarLivro(Long clienteId, Long livroId) {
        Cliente c = usuarioService.buscarCliente(clienteId);
        Livro   l = livroService.buscarLivro(livroId);
        if (l.isDisponivel())
            throw new BusinessException("Livro disponível — realize o empréstimo diretamente.");
        if (reservaRepository.existsReservaAtiva(clienteId, livroId))
            throw new BusinessException("Cliente já possui pré-reserva ativa para este livro.");
        Reserva r = Reserva.builder().cliente(c).livro(l)
                .dataReserva(LocalDate.now())
                .dataExpiracao(LocalDate.now().plusDays(DIAS_EXPIRACAO))
                .status(StatusReserva.PENDENTE).build();
        return ReservaResponseDTO.from(reservaRepository.save(r));
    }

    @Transactional
    public ReservaResponseDTO cancelarReserva(Long resId, Long clienteId) {
        Reserva r = buscarReserva(resId);
        if (!r.getCliente().getId().equals(clienteId))
            throw new BusinessException("Esta reserva não pertence ao cliente.");
        usuarioService.buscarCliente(clienteId).cancelarReserva(r);
        return ReservaResponseDTO.from(reservaRepository.save(r));
    }

    @Transactional
    public ReservaResponseDTO atualizarStatus(Long resId, Long funcId, StatusReserva novoStatus) {
        Reserva r = buscarReserva(resId);
        usuarioService.buscarFuncionario(funcId).statusReserva(r, novoStatus);
        return ReservaResponseDTO.from(reservaRepository.save(r));
    }

    @Transactional
    public void expirarReservasVencidas() {
        reservaRepository.findByStatus(StatusReserva.PENDENTE).stream()
                .filter(Reserva::isExpiracao)
                .forEach(r -> { r.setStatus(StatusReserva.EXPIRADO); reservaRepository.save(r); });
    }

    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarPorCliente(Long cId) {
        return reservaRepository.findByClienteId(cId).stream().map(ReservaResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarPendentes() {
        return reservaRepository.findByStatus(StatusReserva.PENDENTE).stream().map(ReservaResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarTodas() {
        return reservaRepository.findAll().stream().map(ReservaResponseDTO::from).toList();
    }

    public Reserva buscarReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
    }
}
