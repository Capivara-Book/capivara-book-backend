package br.com.capivarabook.capivara_book.security;

import br.com.capivarabook.capivara_book.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ── CustomUserDetailsService ───────────────────────────────────
// Spring Security chama loadUserByUsername(email) em cada autenticação.
// Verifica statusUsuario via isEnabled() no CustomUserDetails.
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final IUsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado: " + email));
    }
}
