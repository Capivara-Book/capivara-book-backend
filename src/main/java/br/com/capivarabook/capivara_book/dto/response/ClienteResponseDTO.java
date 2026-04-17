package br.com.capivarabook.capivara_book.dto.response;

import br.com.capivarabook.capivara_book.entity.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClienteResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String cargo;
    private String status;
    private String cpf;
    private String telefone;
    private int emprestimosAtivos;
    private int limiteEmprestimos;

    public static ClienteResponseDTO from(Cliente cliente, long ativos) {
        return ClienteResponseDTO.builder()
            .id(cliente.getId())
            .nome(cliente.getNome())
            .email(cliente.getEmail())
            .status(cliente.getStatus().name())
            .cpf(cliente.getCpf())
            .telefone(cliente.getTelefone())
            .emprestimosAtivos((int) ativos)
            .limiteEmprestimos(Cliente.LIMITE_EMPRESTIMOS)
            .build();
    }
}
