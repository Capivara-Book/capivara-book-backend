package br.com.capivarabook.capivara_book.dto.request;

import br.com.capivarabook.capivara_book.entity.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ClienteRequestDTO {
    @NotBlank private String nome;
    @NotBlank @Email private String email;
    @NotBlank private String senha;
    @NotBlank private String status;
    @NotBlank @Pattern(regexp = "\\d{11}") private String cpf;
    @NotBlank @Pattern(regexp = "\\d{10,11}") private String telefone;
}
