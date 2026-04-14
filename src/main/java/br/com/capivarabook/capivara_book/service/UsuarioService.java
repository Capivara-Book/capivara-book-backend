package br.com.capivarabook.capivara_book.service;

import br.com.capivarabook.capivara_book.dto.request.*;
import br.com.capivarabook.capivara_book.dto.response.*;
import br.com.capivarabook.capivara_book.entity.*;
import br.com.capivarabook.capivara_book.exception.*;
import br.com.capivarabook.capivara_book.repository.*;
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

//    public UsuarioService(IClienteRepository c, IFuncionarioRepository f, IUsuarioRepository u, IEmprestimoRepository e) {
//        clienteRepository = c;
//        funcionarioRepository = f;
//        usuarioRepository = u;
//        emprestimoRepository = e;
//    }

    @Transactional
    public ClienteResponseDTO cadastrarCliente(ClienteRequestDTO req) {
        if (usuarioRepository.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("E-mail já cadastrado: " + req.getEmail());
        if (clienteRepository.existsByCpf(req.getCpf()))
            throw new DuplicateResourceException("CPF já cadastrado: " + req.getCpf());
        if (clienteRepository.existsByTelefone(req.getTelefone()))
            throw new DuplicateResourceException("Telefone já cadastrado: " + req.getTelefone());
        Cliente c = new Cliente();
        c.setNome(req.getNome()); c.setEmail(req.getEmail());
        c.setSenha(req.getSenha()); c.setCpf(req.getCpf()); c.setTelefone(req.getTelefone());
        clienteRepository.save(c);
        return ClienteResponseDTO.from(c, 0L);
    }

    @Transactional
    public FuncionarioResponseDTO cadastrarFuncionario(FuncionarioRequestDTO req) {
        if (usuarioRepository.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("E-mail já cadastrado: " + req.getEmail());
        if (funcionarioRepository.existsByMatricula(req.getMatricula()))
            throw new DuplicateResourceException("Matrícula já cadastrada: " + req.getMatricula());
        Funcionario f = new Funcionario();
        f.setNome(req.getNome()); f.setEmail(req.getEmail()); f.setSenha(req.getSenha());
        f.setMatricula(req.getMatricula());
        f.setCargo(Cargo.valueOf(req.getCargo().toUpperCase()));
        funcionarioRepository.save(f);
        return FuncionarioResponseDTO.from(f);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(c -> ClienteResponseDTO.from(c, emprestimoRepository.countAtivosDoCliente(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarClientePorId(Long id) {
        Cliente c = buscarCliente(id);
        return ClienteResponseDTO.from(c, emprestimoRepository.countAtivosDoCliente(c.getId()));
    }

    @Transactional
    public ClienteResponseDTO atualizarCliente(Long id, ClienteRequestDTO req) {
        Cliente c = buscarCliente(id);
        if (!c.getEmail().equals(req.getEmail()) && usuarioRepository.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("E-mail em uso: " + req.getEmail());
        c.atualizarDados(req.getNome(), req.getEmail());
        if (req.getTelefone() != null) c.setTelefone(req.getTelefone());
        clienteRepository.save(c);
        return ClienteResponseDTO.from(c, emprestimoRepository.countAtivosDoCliente(c.getId()));
    }

    @Transactional
    public void removerCliente(Long id) {
        Cliente c = buscarCliente(id);
        long ativos = emprestimoRepository.countAtivosDoCliente(c.getId());
        if (ativos > 0)
            throw new BusinessException("Cliente com " + ativos + " empréstimo(s) ativo(s) não pode ser removido (RN07).");
        clienteRepository.delete(c);
    }

    @Transactional(readOnly = true)
    public boolean login(String email, String senha) {
        return usuarioRepository.findByEmail(email).map(u -> u.login(email, senha)).orElse(false);
    }

    public Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    public Funcionario buscarFuncionario(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionario", id));
    }
}
