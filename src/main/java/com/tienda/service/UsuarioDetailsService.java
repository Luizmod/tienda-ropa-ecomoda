package com.tienda.service;

import com.tienda.repository.UsuarioRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) {
        return usuarioRepository.findByCorreo(correo.strip().toLowerCase(Locale.ROOT))
                .map(UsuarioAutenticado::new)
                .orElseThrow(() -> new UsernameNotFoundException("credenciales.login.error"));
    }
}
