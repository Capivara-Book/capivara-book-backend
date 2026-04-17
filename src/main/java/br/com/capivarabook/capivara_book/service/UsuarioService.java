package br.com.capivarabook.capivara_book.service;

import br.com.capivarabook.capivara_book.dto.request.*;
import br.com.capivarabook.capivara_book.dto.response.*;
import br.com.capivarabook.capivara_book.entity.*;
import br.com.capivarabook.capivara_book.exception.*;
import br.com.capivarabook.capivara_book.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import lombok.*;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final IClienteRepository clienteRepository;
    private final IFuncionarioRepository funcionarioRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IEmprestimoRepository emprestimoRepository;

    // ── cadastrarCliente() ────────────────────────────────────
    // Role fixa: ROLE_CLIENTE
    @Transactional
    public ClienteResponseDTO cadastrarCliente(ClienteRequestDTO req) {
        if (usuarioRepository.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("E-mail já cadastrado: " + req.getEmail());
        if (clienteRepository.existsByCpf(req.getCpf()))
            throw new DuplicateResourceException("CPF já cadastrado: " + req.getCpf());
        if (clienteRepository.existsByTelefone(req.getTelefone()))
            throw new DuplicateResourceException("Telefone já cadastrado: " + req.getTelefone());

        Cliente c = new Cliente();
        c.setNome(req.getNome());
        c.setEmail(req.getEmail());
        c.setSenha(req.getSenha()); // BCrypt
        c.setCpf(req.getCpf());
        c.setTelefone(req.getTelefone());
        c.setStatus(StatusUsuario.ATIVO);
        c.setRole(Role.CLIENTE);                          // RBAC

        return ClienteResponseDTO.from(clienteRepository.save(c), 0L);
    }

    // ── cadastrarFuncionario() ────────────────────────────────
    // Role derivada do Cargo: ADMIN → ROLE_ADMIN etc.
    @Transactional
    public FuncionarioResponseDTO cadastrarFuncionario(FuncionarioRequestDTO req) {
        if (usuarioRepository.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("E-mail já cadastrado: " + req.getEmail());
        if (funcionarioRepository.existsByMatricula(req.getMatricula()))
            throw new DuplicateResourceException("Matrícula já cadastrada: " + req.getMatricula());

        Cargo cargo = Cargo.valueOf(req.getCargo().toUpperCase());

        Funcionario f = new Funcionario();
        f.setNome(req.getNome());
        f.setEmail(req.getEmail());
        f.setSenha(req.getSenha()); // BCrypt
        f.setMatricula(req.getMatricula());
        f.setCargo(cargo);
        f.setStatus(StatusUsuario.ATIVO);
        f.setRole(Role.ADMIN);                  // RBAC derivado do Cargo

        return FuncionarioResponseDTO.from(funcionarioRepository.save(f));
    }

    // ── listarClientes() — só ATIVOS ─────────────────────────
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(c -> ClienteResponseDTO.from(c, emprestimoRepository.countAtivosDoCliente(c.getId())))
                .toList();
    }

    // ── buscarClientePorId() ──────────────────────────────────
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarClientePorId(Long id) {
        Cliente c = buscarCliente(id);
        return ClienteResponseDTO.from(c, emprestimoRepository.countAtivosDoCliente(c.getId()));
    }

    // ── atualizarCliente() ────────────────────────────────────
    @Transactional
    public ClienteResponseDTO atualizarCliente(Long id, ClienteRequestDTO req) {
        Cliente c = buscarCliente(id);
        if (!c.getEmail().equals(req.getEmail()) && usuarioRepository.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("E-mail em uso: " + req.getEmail());
        c.atualizarDados(req.getNome(), req.getEmail());
        if (req.getTelefone() != null) c.setTelefone(req.getTelefone());
        return ClienteResponseDTO.from(clienteRepository.save(c),
                emprestimoRepository.countAtivosDoCliente(c.getId()));
    }

    // ── inativarCliente() — exclusão lógica (RN07) ────────────
    // NÃO deleta do banco — chama inativar() que seta INATIVO
    @Transactional
    public void inativarCliente(Long id) {
        Cliente c = buscarCliente(id);
        if (emprestimoRepository.countAtivosDoCliente(c.getId()) > 0)
            throw new BusinessException(
                    "Cliente com empréstimos ativos não pode ser inativado (RN07).");
        c.inativar();
        clienteRepository.save(c);
    }

    // ── helpers internos ──────────────────────────────────────
    public Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    public Funcionario buscarFuncionario(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionario", id));
    }
}
