package br.com.capivarabook.capivara_book.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponseDTO {
    private String token;
    private String tipo;        // "Bearer"
    private Long   id;
    private String nome;
    private String email;
    private String role;        // ex: // "CLIENTE" | "FUNCIONARIO"
    private String cargo;       // ex: "ADMIN" (para exibição no frontend)
}
