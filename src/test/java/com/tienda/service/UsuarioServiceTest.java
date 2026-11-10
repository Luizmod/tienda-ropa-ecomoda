package com.tienda.service;

import com.tienda.domain.*;
import com.tienda.dto.*;
import com.tienda.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class UsuarioServiceTest {
    private final UsuarioRepository repositorio = mock(UsuarioRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UsuarioService servicio = new UsuarioService(repositorio, encoder);
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuario = new Usuario();
        usuario.setIdUsuario(7L);
        usuario.setCorreo("cliente@example.com");
        usuario.setContrasena(encoder.encode("anterior-segura"));
        usuario.setRol(Rol.CLIENTE);
        when(repositorio.findById(7L)).thenReturn(Optional.of(usuario));
    }

    private RegistroFormulario registro(String contrasena, String confirmacion) {
        var formulario = new RegistroFormulario();
        formulario.setCorreo(" CLIENTE@EXAMPLE.COM ");
        formulario.setContrasena(contrasena);
        formulario.setConfirmacion(confirmacion);
        return formulario;
    }

    private PerfilFormulario modificacion() {
        var formulario = new PerfilFormulario();
        formulario.setCorreo("nuevo@example.com");
        formulario.setContrasenaActual("anterior-segura");
        return formulario;
    }

    @Test
    void registraClienteNormalizadoConHash() {
        servicio.registrar(registro("nueva-segura", "nueva-segura"));
        var captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorio).saveAndFlush(captor.capture());
        assertEquals(Rol.CLIENTE, captor.getValue().getRol());
        assertEquals("cliente@example.com", captor.getValue().getCorreo());
        assertTrue(encoder.matches("nueva-segura", captor.getValue().getContrasena()));
        assertNotEquals("nueva-segura", captor.getValue().getContrasena());
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(repositorio.existsByCorreo("cliente@example.com")).thenReturn(true);
        assertThrows(CredencialesException.class,
                () -> servicio.registrar(registro("nueva-segura", "nueva-segura")));
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void rechazaConfirmacionDistinta() {
        assertThrows(CredencialesException.class,
                () -> servicio.registrar(registro("nueva-segura", "otra-segura")));
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void rechazaContrasenaCortaYExcesoDeBytes() {
        assertThrows(CredencialesException.class, () -> servicio.registrar(registro("corta", "corta")));
        String larga = "á".repeat(37);
        assertThrows(CredencialesException.class, () -> servicio.registrar(registro(larga, larga)));
    }

    @Test
    void consultaNoExponeHash() {
        assertEquals(new PerfilVista("cliente@example.com"), servicio.consultar(7L));
        assertFalse(servicio.consultar(7L).toString().contains(usuario.getContrasena()));
    }

    @Test
    void modificarExigeContrasenaActual() {
        var formulario = modificacion();
        formulario.setContrasenaActual("incorrecta");
        assertThrows(CredencialesException.class, () -> servicio.modificar(7L, formulario));
        assertEquals("cliente@example.com", usuario.getCorreo());
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void permiteCambiarSoloCorreoSinCambiarRolNiHash() {
        String hash = usuario.getContrasena();
        servicio.modificar(7L, modificacion());
        assertEquals("nuevo@example.com", usuario.getCorreo());
        assertEquals(hash, usuario.getContrasena());
        assertEquals(Rol.CLIENTE, usuario.getRol());
    }

    @Test
    void cambiaContrasenaConConfirmacion() {
        var formulario = modificacion();
        formulario.setNuevaContrasena("otra-segura");
        formulario.setConfirmacion("otra-segura");
        servicio.modificar(7L, formulario);
        assertTrue(encoder.matches("otra-segura", usuario.getContrasena()));
        assertFalse(encoder.matches("anterior-segura", usuario.getContrasena()));
    }

    @Test
    void rechazaCambioACorreoAjeno() {
        when(repositorio.existsByCorreoAndIdUsuarioNot("nuevo@example.com", 7L)).thenReturn(true);
        assertThrows(CredencialesException.class, () -> servicio.modificar(7L, modificacion()));
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void eliminaRealmenteYFuerzaRestricciones() {
        servicio.eliminar(7L);
        verify(repositorio).delete(usuario);
        verify(repositorio).flush();
    }

    @Test
    void propagaAsociacionesHastaFueraDeTransaccion() {
        doThrow(new DataIntegrityViolationException("FK")).when(repositorio).flush();
        assertThrows(DataIntegrityViolationException.class, () -> servicio.eliminar(7L));
    }
}
