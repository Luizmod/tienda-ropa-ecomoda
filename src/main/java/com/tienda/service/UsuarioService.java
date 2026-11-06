package com.tienda.service;

import com.tienda.domain.Rol;
import com.tienda.domain.Usuario;
import com.tienda.dto.*;
import com.tienda.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registrar(RegistroFormulario formulario) {
        String correo = normalizar(formulario.getCorreo());
        validarContrasena(formulario.getContrasena(), formulario.getConfirmacion());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new CredencialesException("credenciales.correo.duplicado");
        }
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(formulario.getContrasena()));
        // El formulario público no puede asignar privilegios.
        usuario.setRol(Rol.CLIENTE);
        usuarioRepository.saveAndFlush(usuario);
    }

    @Transactional(readOnly = true)
    public PerfilVista consultar(Long idUsuario) {
        return new PerfilVista(obtener(idUsuario).getCorreo());
    }

    @Transactional
    public void modificar(Long idUsuario, PerfilFormulario formulario) {
        Usuario usuario = obtener(idUsuario);
        if (!passwordEncoder.matches(formulario.getContrasenaActual(), usuario.getContrasena())) {
            throw new CredencialesException("credenciales.actual.incorrecta");
        }
        String correo = normalizar(formulario.getCorreo());
        if (usuarioRepository.existsByCorreoAndIdUsuarioNot(correo, idUsuario)) {
            throw new CredencialesException("credenciales.correo.duplicado");
        }
        String nueva = formulario.getNuevaContrasena();
        String confirmacion = formulario.getConfirmacion();
        if (nueva != null && !nueva.isEmpty()) {
            validarContrasena(nueva, confirmacion);
            usuario.setContrasena(passwordEncoder.encode(nueva));
        } else if (confirmacion != null && !confirmacion.isEmpty()) {
            throw new CredencialesException("credenciales.confirmacion.invalida");
        }
        usuario.setCorreo(correo);
        usuarioRepository.saveAndFlush(usuario);
    }

    @Transactional
    public void eliminar(Long idUsuario) {
        // Sin cascadas: las claves foráneas impiden borrar datos asociados.
        usuarioRepository.delete(obtener(idUsuario));
        usuarioRepository.flush();
    }

    private Usuario obtener(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new CredencialesException("credenciales.usuario.inexistente"));
    }

    private String normalizar(String correo) {
        return correo.strip().toLowerCase(Locale.ROOT);
    }

    private void validarContrasena(String contrasena, String confirmacion) {
        // BCrypt admite hasta 72 bytes, no necesariamente 72 caracteres.
        if (contrasena == null || contrasena.isBlank() || contrasena.length() < 8
                || contrasena.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new CredencialesException("credenciales.contrasena.invalida");
        }
        if (!contrasena.equals(confirmacion)) {
            throw new CredencialesException("credenciales.confirmacion.invalida");
        }
    }
}
