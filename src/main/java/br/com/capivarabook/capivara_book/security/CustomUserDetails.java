package br.com.capivarabook.capivara_book.security;

import br.com.capivarabook.capivara_book.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

// ── CustomUserDetails ─────────────────────────────────────────
// Adapta a entidade Usuario para o contrato UserDetails do Spring Security.
// A role vem do campo usuario.getRole() — já persiste "ROLE_ADMIN" etc.
// isEnabled() delega para statusUsuario == ATIVO (exclusão lógica).
public class CustomUserDetails implements UserDetails {
    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getIdUser()      { return usuario.getId(); }
    public String getCargoUser()  { return usuario.getRole().name(); }
    public Usuario getUsuario() { return usuario; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // usuario.getRole() → ex: "ROLE_ADMIN"
        return List.of(new SimpleGrantedAuthority(usuario.getRole().name()));
    }

    @Override
    public String getPassword() { return usuario.getSenha(); }

    @Override
    public String getUsername() { return usuario.getEmail(); }

    // Conta não expirada, não bloqueada, credenciais não expiradas
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    // Exclusão lógica — StatusUsuario.INATIVO desabilita o login
    @Override
    public boolean isEnabled() { return usuario.isEnabled(); }
}
