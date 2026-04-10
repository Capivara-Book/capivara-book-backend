package br.com.capivarabook.capivara_book.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "tb_usuario")
@Inheritance(strategy = InheritanceType.JOINED)

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor

public abstract class Usuario {
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
    @Column(name = "senha", nullable = false, length = 50)
    protected String senha;

    public boolean login(String emailInput, String senhaInput) {
        return this.email.equals(emailInput) && this.senha.equals(senhaInput);
    }

    public void atualizarDados(String novoNome, String novoEmail) {
        if (novoNome  != null && !novoNome.isBlank())  this.nome  = novoNome;
        if (novoEmail != null && !novoEmail.isBlank()) this.email = novoEmail;
    }
}
