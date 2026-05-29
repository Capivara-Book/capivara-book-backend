package br.com.capivarabook.capivara_book.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.userdetails.UserDetails;

@SuperBuilder
@Entity
@Table(name = "tb_usuario")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    protected Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "nome", nullable = false, length = 100)
    protected String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    protected String email;

    @NotBlank(message = "Senha é obrigatória")
    @Column(name = "senha", nullable = false, length = 255)
    protected String senha;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    protected StatusUsuario status = StatusUsuario.ATIVO;

    @NotNull(message = "Tipo usuário é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    protected Role role;

    public boolean isAtivo() {
        return this.status == StatusUsuario.ATIVO;
    }

    // Exclusão lógica
    public void inativar() {
        this.status = StatusUsuario.INATIVO;
    }

    public void atualizarDados(String novoNome, String novoEmail) {
        if (novoNome  != null && !novoNome.isBlank())  this.nome  = novoNome;
        if (novoEmail != null && !novoEmail.isBlank()) this.email = novoEmail;
    }
}
