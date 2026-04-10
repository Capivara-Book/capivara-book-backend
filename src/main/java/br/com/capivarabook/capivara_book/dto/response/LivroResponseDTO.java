package br.com.capivarabook.capivara_book.dto.response;

import br.com.capivarabook.capivara_book.entity.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LivroResponseDTO {
    private Long id;
    private String titulo;
    private String autor;
    private String editora;
    private String isbn;
    private Integer anoPublicacao;
    private String genero;
    private int exemplares;
    private int exemplaresDisponiveis;
    private String status;

    public static LivroResponseDTO from(Livro livro) {
        return LivroResponseDTO.builder()
            .id(livro.getId())
            .titulo(livro.getTitulo())
            .autor(livro.getAutor())
            .editora(livro.getEditora())
            .isbn(livro.getIsbn())
            .anoPublicacao(livro.getAnoPublicacao())
            .genero(livro.getGenero().name())
            .exemplares(livro.getExemplares())
            .exemplaresDisponiveis(livro.getExemplaresDisponiveis())
            .status(livro.getStatus().name())
            .build();
    }
}
