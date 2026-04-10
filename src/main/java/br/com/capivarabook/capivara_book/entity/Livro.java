package br.com.capivarabook.capivara_book.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "tb_livro")

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_livro")
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @NotBlank(message = "Autor é obrigatório")
    @Column(name = "autor", nullable = false, length = 100)
    private String autor;

    @NotBlank(message = "Editora é obrigatória")
    @Column(name = "editora", nullable = false, length = 100)
    private String editora;

    @NotBlank(message = "ISBN é obrigatório")
    @Column(name = "isbn", nullable = false, unique = true, length = 13)
    private String isbn;

    @NotNull(message = "Ano de publicação é obrigatório")
    @Column(name = "ano_publicacao", nullable = false)
    private Integer anoPublicacao;

    @NotNull(message = "Gênero é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 50)
    private Genero genero;

    @Min(value = 0, message = "Exemplares não pode ser negativo")
    @Column(name = "exemplares", nullable = false)
    private int exemplares;

    @Min(value = 0, message = "Exemplares disponíveis não pode ser negativo")
    @Column(name = "exemplares_disponiveis", nullable = false)
    private int exemplaresDisponiveis;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusLivro status = StatusLivro.DISPONIVEL;

    public boolean isDisponivel() {
        return this.exemplaresDisponiveis > 0;
    }

    public StatusLivro statusLivro() {
        return isDisponivel() ? StatusLivro.DISPONIVEL : StatusLivro.INDISPONIVEL;
    }

    public void registrarEmprestimo() {
        if (!isDisponivel()) {
            throw new IllegalStateException("Livro sem exemplares disponíveis para empréstimo.");
        }
        this.exemplaresDisponiveis--;
        sincronizarStatus();
    }

    public void registrarDevolucao() {
        if (this.exemplaresDisponiveis >= this.exemplares) {
            throw new IllegalStateException("Todos os exemplares já estão disponíveis.");
        }
        this.exemplaresDisponiveis++;
        sincronizarStatus();
    }

    public void adicionarLivro() {
        this.exemplaresDisponiveis = this.exemplares;
        this.status = StatusLivro.DISPONIVEL;
    }

    private void sincronizarStatus() {
        this.status = statusLivro();
    }
}

