package br.com.capivarabook.capivara_book.dto.response;

import br.com.capivarabook.capivara_book.entity.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FuncionarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String status;
    private String cargo;
    private String matricula;

    public static FuncionarioResponseDTO from(Funcionario funcionario) {
        return FuncionarioResponseDTO.builder()
            .id(funcionario.getId())
            .nome(funcionario.getNome())
            .email(funcionario.getEmail())
            .status(funcionario.getStatus().name())
            .matricula(funcionario.getMatricula())
            .cargo(funcionario.getCargo().name())
            .build();
    }
}
