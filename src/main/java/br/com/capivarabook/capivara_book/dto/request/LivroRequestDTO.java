package br.com.capivarabook.capivara_book.dto.request;

import br.com.capivarabook.capivara_book.entity.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LivroRequestDTO {
    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @NotBlank(message = "Autor é obrigatório")
    private String autor;

    @NotBlank(message = "Editora é obrigatória")
    private String editora;

    @NotBlank(message = "ISBN é obrigatório")
    @Pattern(regexp = "\\d{13}", message = "ISBN deve ter 13 dígitos")
    private String isbn;

    @NotNull(message = "Ano de publicação é obrigatório")
    @Min(value = 1000) @Max(value = 2100)
    private Integer anoPublicacao;

    @NotNull(message = "Gênero é obrigatório")
    private Genero genero;

    @NotNull @Min(value = 1, message = "Deve ter ao menos 1 exemplar")
    private Integer exemplares;
}
