package br.com.capivarabook.capivara_book.config;

import br.com.capivarabook.capivara_book.entity.*;
import br.com.capivarabook.capivara_book.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

// Inicializa o banco com dados de demonstração.
// Executado apenas se o banco estiver vazio (evita duplicatas).

// USUÁRIOS SEED (para testar login):
//   Admin  → email: admin@capivarabook.com | senha: admin123
//   Cliente→ email: maria.silva@email.com | senha: senha123
//   Cliente→ email: joao.santos@email.com | senha: senha123
//   Cliente→ email: ana.oliveira@email.com | senha: senha123
//   Cliente→ email: carlos.ferreira@email.com | senha: senha123

// Após o seeder rodar, use POST /api/v1/auth/login com as
// credenciais acima para obter o Bearer token.
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final IUsuarioRepository    usuarioRepository;
    private final IClienteRepository    clienteRepository;
    private final IFuncionarioRepository funcionarioRepository;
    private final ILivroRepository      livroRepository;
    private final IEmprestimoRepository emprestimoRepository;
    private final IReservaRepository    reservaRepository;
    private final PasswordEncoder       passwordEncoder;

    @Bean
    CommandLineRunner seed() {
        return args -> {
            if (usuarioRepository.count() > 0) {
                log.info("[DataSeeder] Banco já populado (seed ignorado).");
                return;
            }
            log.info("[DataSeeder] Iniciando seed...");

            seedFuncionarios();
            seedClientes();
            seedLivros();
            seedEmprestimos();
            seedReservas();

            log.info("[DataSeeder] Seed concluído com sucesso.");
            log.info("[DataSeeder] Login Admin (admin@capivarabook.com / admin123)");
            log.info("[DataSeeder] Login Cliente (maria.silva@email.com  / senha123)");
        };
    }

    // ── Funcionários ──────────────────────────────────────────
    private void seedFuncionarios() {
        Funcionario admin = new Funcionario();
        admin.setNome("Admin");
        admin.setEmail("admin@capivarabook.com");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setMatricula("ADM000001");
        admin.setCargo(Cargo.ADMIN);
        admin.setStatus(StatusUsuario.ATIVO);
        admin.setRole(Role.ADMIN);
        funcionarioRepository.save(admin);

        Funcionario colaborador = new Funcionario();
        colaborador.setNome("Colaborador Silva");
        colaborador.setEmail("colaborador@capivarabook.com");
        colaborador.setSenha(passwordEncoder.encode("colab123"));
        colaborador.setMatricula("COL000001");
        colaborador.setCargo(Cargo.COLABORADOR);
        colaborador.setStatus(StatusUsuario.ATIVO);
        colaborador.setRole(Role.ADMIN);
        funcionarioRepository.save(colaborador);

        log.info("[DataSeeder] 2 funcionário(s) inserido(s).");
    }

    // ── Clientes ──────────────────────────────────────────────
    private void seedClientes() {
        Cliente maria = new Cliente();
        maria.setNome("Maria Silva");
        maria.setEmail("maria.silva@email.com");
        maria.setSenha(passwordEncoder.encode("senha123"));
        maria.setCpf("12345678901");
        maria.setTelefone("11987654321");
        maria.setStatus(StatusUsuario.ATIVO);
        maria.setRole(Role.CLIENTE);
        clienteRepository.save(maria);

        Cliente joao = new Cliente();
        joao.setNome("João Santos");
        joao.setEmail("joao.santos@email.com");
        joao.setSenha(passwordEncoder.encode("senha123"));
        joao.setCpf("98765432100");
        joao.setTelefone("11912345678");
        joao.setStatus(StatusUsuario.ATIVO);
        joao.setRole(Role.CLIENTE);
        clienteRepository.save(joao);

        Cliente ana = new Cliente();
        ana.setNome("Ana Oliveira");
        ana.setEmail("ana.oliveira@email.com");
        ana.setSenha(passwordEncoder.encode("senha123"));
        ana.setCpf("45678912300");
        ana.setTelefone("21998765432");
        ana.setStatus(StatusUsuario.ATIVO);
        ana.setRole(Role.CLIENTE);
        clienteRepository.save(ana);

        Cliente carlos = new Cliente();
        carlos.setNome("Carlos Ferreira");
        carlos.setEmail("carlos.f@email.com");
        carlos.setSenha(passwordEncoder.encode("senha123"));
        carlos.setCpf("32165498700");
        carlos.setTelefone("31987651234");
        carlos.setStatus(StatusUsuario.ATIVO);
        carlos.setRole(Role.CLIENTE);
        clienteRepository.save(carlos);

        // Cliente INATIVO — para testar bloqueio de login (401)
        Cliente inativo = new Cliente();
        inativo.setNome("Usuário Inativo");
        inativo.setEmail("inativo@capivarabook.com");
        inativo.setSenha(passwordEncoder.encode("senha123"));
        inativo.setCpf("11122233344");
        inativo.setTelefone("11911112222");
        inativo.setStatus(StatusUsuario.INATIVO);
        inativo.setRole(Role.CLIENTE);
        clienteRepository.save(inativo);

        log.info("[DataSeeder] 5 cliente(s) inserido(s). (1 inativo para testes)");
    }

    // ── Livros ────────────────────────────────────────────────
    private void seedLivros() {
        criarLivro("Dom Casmurro",                         "Machado de Assis",        "Companhia das Letras", "9788535909555", 1899, Genero.ROMANCE,           5, 3);
        criarLivro("O Hobbit",                             "J.R.R. Tolkien",          "HarperCollins",        "9788533613377", 1937, Genero.FANTASIA,           3, 1);
        criarLivro("A Arte da Guerra",                     "Sun Tzu",                 "L&PM",                 "9788525406958", 2006, Genero.HISTORIA,           4, 4);
        criarLivro("It: A Coisa",                          "Stephen King",            "Suma",                 "9788576655336", 1986, Genero.MISTERIO,           2, 0); // INDISPONIVEL
        criarLivro("O Pequeno Príncipe",                   "Antoine de Saint-Exupéry","Agir",                 "9788522005192", 1943, Genero.FANTASIA,           6, 5);
        criarLivro("Sapiens",                              "Yuval Noah Harari",       "L&PM",                 "9788525432001", 2011, Genero.HISTORIA,           3, 2);
        criarLivro("Clean Code",                           "Robert C. Martin",        "Alta Books",           "9788576082675", 2008, Genero.TECNOLOGIA,         4, 3);
        criarLivro("O Poder do Hábito",                    "Charles Duhigg",          "Objetiva",             "9788562533518", 2012, Genero.BIOGRAFIA,          5, 4);

        log.info("[DataSeeder] 8 livro(s) inserido(s).");
    }

    private void criarLivro(String titulo, String autor, String editora,
                            String isbn, int ano, Genero genero,
                            int exemplares, int disponíveis) {
        Livro l = Livro.builder()
                .titulo(titulo).autor(autor).editora(editora)
                .isbn(isbn).anoPublicacao(ano).genero(genero)
                .exemplares(exemplares).exemplaresDisponiveis(disponíveis)
                .status(disponíveis > 0 ? StatusLivro.DISPONIVEL : StatusLivro.INDISPONIVEL)
                .build();
        livroRepository.save(l);
    }

    // ── Empréstimos ───────────────────────────────────────────
    private void seedEmprestimos() {
        Funcionario admin     = funcionarioRepository.findAll().get(0);
        Cliente     maria     = clienteRepository.findAll().get(0); // maria
        Cliente     joao      = clienteRepository.findAll().get(1); // joao
        Cliente     ana       = clienteRepository.findAll().get(2); // ana
        Cliente     carlos    = clienteRepository.findAll().get(3); // carlos

        Livro domCasmurro  = livroRepository.findAll().get(0);
        Livro hobbit       = livroRepository.findAll().get(1);
        Livro itACoisa     = livroRepository.findAll().get(3); // INDISPONIVEL
        Livro pqPrincipe   = livroRepository.findAll().get(4);
        Livro sapiens      = livroRepository.findAll().get(5);

        // Empréstimo ATIVO — Maria + Dom Casmurro
        criarEmprestimo(maria, domCasmurro, admin,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(9),
                null, StatusEmprestimo.ATIVO, 0);

        // Empréstimo ATIVO — Maria + Hobbit (renováveis)
        criarEmprestimo(maria, hobbit, admin,
                LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(11),
                null, StatusEmprestimo.ATIVO, 0);

        // Empréstimo ATRASADO — João + It A Coisa (para testar RN04 e CT10)
        criarEmprestimo(joao, itACoisa, admin,
                LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(16),
                null, StatusEmprestimo.ATRASADO, 0);

        // Empréstimo DEVOLVIDO — Ana + O Pequeno Príncipe (para CT08)
        criarEmprestimo(ana, pqPrincipe, admin,
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(6),
                LocalDate.now().minusDays(8),
                StatusEmprestimo.DEVOLVIDO, 0);

        // Empréstimo ATIVO — Carlos + Sapiens (já renovado 2x — CT09c)
        criarEmprestimo(carlos, sapiens, admin,
                LocalDate.now().minusDays(28),
                LocalDate.now().plusDays(14),
                null, StatusEmprestimo.RENOVADO, 2);

        log.info("[DataSeeder] 5 empréstimo(s) inserido(s).");
    }

    private void criarEmprestimo(Cliente cliente, Livro livro, Funcionario func,
                                 LocalDate dtEmprestimo, LocalDate dtPrevista,
                                 LocalDate dtDevolucao, StatusEmprestimo status,
                                 int renovacoes) {
        Emprestimo e = Emprestimo.builder()
                .cliente(cliente).livro(livro).funcionario(func)
                .dataEmprestimo(dtEmprestimo)
                .dataPrevistaDevolucao(dtPrevista)
                .dataDevolucao(dtDevolucao)
                .status(status)
                .renovacoesRealizadas(renovacoes)
                .multaDiaria(new BigDecimal("2.00"))
                .build();
        emprestimoRepository.save(e);
    }

    // ── Reservas ──────────────────────────────────────────────
    private void seedReservas() {
        Cliente ana    = clienteRepository.findAll().get(2);
        Cliente carlos = clienteRepository.findAll().get(3);
        Livro   itACoisa = livroRepository.findAll().get(3); // INDISPONIVEL
        Livro   cleanCode = livroRepository.findAll().get(6);

        // Reserva PENDENTE — Ana aguardando "It: A Coisa"
        Reserva r1 = Reserva.builder()
                .cliente(ana).livro(itACoisa)
                .dataReserva(LocalDate.now().minusDays(2))
                .dataExpiracao(LocalDate.now().plusDays(5))
                .status(StatusReserva.PENDENTE)
                .build();
        reservaRepository.save(r1);

        // Reserva CONFIRMADA — Carlos com "Clean Code"
        Reserva r2 = Reserva.builder()
                .cliente(carlos).livro(cleanCode)
                .dataReserva(LocalDate.now().minusDays(10))
                .dataExpiracao(LocalDate.now().plusDays(4))
                .status(StatusReserva.CONFIRMADO)
                .build();
        reservaRepository.save(r2);

        log.info("[DataSeeder] 2 reserva(s) inserida(s).");
    }
}
