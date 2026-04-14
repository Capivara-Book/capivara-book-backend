package br.com.capivarabook.capivara_book.service;

import br.com.capivarabook.capivara_book.dto.response.RelatorioResponseDTO;
import br.com.capivarabook.capivara_book.entity.*;
import br.com.capivarabook.capivara_book.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.*;

@Service
@RequiredArgsConstructor
public class RelatorioService {
    private final ILivroRepository livroRepository;
    private final IClienteRepository clienteRepository;
    private final IEmprestimoRepository emprestimoRepository;
    private final IReservaRepository reservaRepository;

//    public RelatorioService(ILivroRepository l, IClienteRepository c,
//                            IEmprestimoRepository e, IReservaRepository r) {
//        livroRepo = l; clienteRepo = c; empRepo = e; resRepo = r;
//    }

    @Transactional(readOnly = true)
    public RelatorioResponseDTO gerarRelatorio() {
        long ativos = emprestimoRepository.findByStatus(StatusEmprestimo.ATIVO).size()
                + emprestimoRepository.findByStatus(StatusEmprestimo.RENOVADO).size();
        return RelatorioResponseDTO.builder()
                .totalLivros(livroRepository.count())
                .totalClientes(clienteRepository.count())
                .emprestimosAtivos(ativos)
                .emprestimosEmAtraso(emprestimoRepository.findEmAtraso().size())
                .reservasPendentes(reservaRepository.findByStatus(StatusReserva.PENDENTE).size())
                .build();
    }
}
