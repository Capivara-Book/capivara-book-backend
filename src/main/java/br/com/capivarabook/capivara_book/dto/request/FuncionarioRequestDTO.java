package br.com.capivarabook.capivara_book.dto.request;

import br.com.capivarabook.capivara_book.entity.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FuncionarioRequestDTO {
    @NotBlank private String nome;
    @NotBlank @Email private String email;
    @NotBlank private String senha;
    @NotBlank private String matricula;
    @NotBlank private String cargo;
}
