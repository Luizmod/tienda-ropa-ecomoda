package com.tienda.service;

import com.tienda.domain.Usuario;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UsuarioAutenticado extends User {
    private static final long serialVersionUID = 1L;
    private final Long idUsuario;

    public UsuarioAutenticado(Usuario usuario) {
        super(usuario.getCorreo(), usuario.getContrasena(), autoridades(usuario));
        this.idUsuario = usuario.getIdUsuario();
    }

    private static Collection<? extends GrantedAuthority> autoridades(Usuario usuario) {
        return java.util.List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    public Long getIdUsuario() {
        return idUsuario;
    }
}
