package br.com.capivarabook.capivara_book.dto.request;

import br.com.capivarabook.capivara_book.entity.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmprestimoRequestDTO {
    @NotNull(message = "clienteId é obrigatório")
    private Long clienteId;

    @NotNull(message = "livroId é obrigatório")
    private Long livroId;
}
