package br.com.capivarabook.capivara_book.service;

import br.com.capivarabook.capivara_book.dto.response.*;
import br.com.capivarabook.capivara_book.entity.*;
import br.com.capivarabook.capivara_book.exception.*;
import br.com.capivarabook.capivara_book.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Service
@RequiredArgsConstructor
public class EmprestimoService {
    private final IEmprestimoRepository emprestimoRepository;
    private final LivroService livroService;
    private final UsuarioService usuarioService;

    @Value("${app.emprestimo.prazo-dias:14}") private int prazoDias;
    @Value("${app.multa.diaria:2.00}")        private BigDecimal multaDiaria;

//    public EmprestimoService(IEmprestimoRepository e, LivroService l, UsuarioService u) {
//        emprestimoRepository = e; livroService = l; usuarioService = u;
//    }

    @Transactional
    public EmprestimoResponseDTO registrarEmprestimo(Long clienteId, Long livroId, Long funcId) {
        Cliente c = usuarioService.buscarCliente(clienteId);
        Livro   l = livroService.buscarLivro(livroId);
        Funcionario f = usuarioService.buscarFuncionario(funcId);

        if (emprestimoRepository.countAtivosDoCliente(clienteId) >= Cliente.LIMITE_EMPRESTIMOS)
            throw new BusinessException("Cliente no limite de " + Cliente.LIMITE_EMPRESTIMOS + " empréstimos (RN01).");
        if (!l.isDisponivel())
            throw new BusinessException("Livro sem exemplares disponíveis (RN02).");
        if (emprestimoRepository.existsDuplicataAtiva(clienteId, livroId))
            throw new BusinessException("Cliente já possui este livro emprestado (RN03).");

        Emprestimo emp = f.registrarEmprestimo(c, l, prazoDias, multaDiaria);
        return EmprestimoResponseDTO.from(emprestimoRepository.save(emp));
    }

    @Transactional
    public DevolucaoResponseDTO devolverLivro(Long empId, Long funcId) {
        Emprestimo emp = buscarEmprestimo(empId);
        Funcionario f  = usuarioService.buscarFuncionario(funcId);
        BigDecimal multa = f.registrarDevolucao(emp);
        emprestimoRepository.save(emp);
        String msg = multa.compareTo(BigDecimal.ZERO) > 0
                ? "Devolvido com atraso. Multa: R$ " + multa
                : "Devolvido no prazo.";
        return DevolucaoResponseDTO.builder()
                .emprestimo(EmprestimoResponseDTO.from(emp)).multaGerada(multa).mensagem(msg).build();
    }

    @Transactional
    public EmprestimoResponseDTO aprovarRenovacao(Long empId, Long funcId) {
        Emprestimo emp = buscarEmprestimo(empId);
        usuarioService.buscarFuncionario(funcId).statusRenovacao(emp);
        return EmprestimoResponseDTO.from(emprestimoRepository.save(emp));
    }

    @Transactional(readOnly = true)
    public EmprestimoResponseDTO validarSolicitacaoRenovacao(Long empId, Long clienteId) {
        Emprestimo emp = buscarEmprestimo(empId);
        if (!emp.getCliente().getId().equals(clienteId))
            throw new BusinessException("Este empréstimo não pertence ao cliente.");
        if (emp.isAtrasado())
            throw new BusinessException("Em atraso — renovação bloqueada (RN04). Dias: " + emp.diasAtraso());
        if (!emp.podeRenovar())
            throw new BusinessException("Limite de " + Emprestimo.LIMITE_RENOVACOES + " renovações atingido (RN09).");
        return EmprestimoResponseDTO.from(emp);
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponseDTO> listarEmAtraso() {
        return emprestimoRepository.findEmAtraso().stream().map(EmprestimoResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponseDTO> listarPorCliente(Long cId) {
        return emprestimoRepository.findByClienteId(cId).stream().map(EmprestimoResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponseDTO> listarTodos() {
        return emprestimoRepository.findAll().stream().map(EmprestimoResponseDTO::from).toList();
    }

    public Emprestimo buscarEmprestimo(Long id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo", id));
    }
}
