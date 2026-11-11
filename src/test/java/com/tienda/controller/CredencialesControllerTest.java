package com.tienda.controller;

import com.tienda.SecurityConfig;
import com.tienda.domain.*;
import com.tienda.dto.PerfilVista;
import com.tienda.service.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CredencialesController.class, IndexController.class})
@Import(SecurityConfig.class)
class CredencialesControllerTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private UsuarioService servicio;
    @MockitoBean private UsuarioDetailsService detalles;
    @MockitoBean private PromocionesService promociones;

    private UsuarioAutenticado cliente() {
        var usuario = new Usuario();
        usuario.setIdUsuario(7L);
        usuario.setCorreo("cliente@example.com");
        usuario.setContrasena("hash-no-visible");
        usuario.setRol(Rol.CLIENTE);
        return new UsuarioAutenticado(usuario);
    }

    @Test
    void registroPublicoRenderizaFormulario() throws Exception {
        mvc.perform(get("/registro/nuevo"))
                .andExpect(status().isOk()).andExpect(view().name("registro/nuevo"));
    }

    @Test
    void perfilRequiereSesion() throws Exception {
        mvc.perform(get("/perfil")).andExpect(status().is3xxRedirection());
        verifyNoInteractions(servicio);
    }

    @ParameterizedTest
    @CsvSource({
        "es, Mi perfil, Lo esencial se queda contigo.",
        "en, My profile, The essentials stay with you.",
        "fr, Mon profil, L’essentiel reste avec vous.",
        "pt, Meu perfil, O essencial fica com você."
    })
    void cambioDeIdiomaEnPerfilSeConservaAlAbrirInicio(String idioma, String perfil, String portada)
            throws Exception {
        when(servicio.consultar(7L)).thenReturn(new PerfilVista("cliente@example.com"));
        when(promociones.listarVigentes()).thenReturn(List.of());
        var sesion = new MockHttpSession();

        mvc.perform(get("/perfil").session(sesion).with(user(cliente())).param("lang", idioma))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("lang=\"" + idioma + "\"")))
                .andExpect(content().string(containsString("<h1>" + perfil + "</h1>")))
                .andExpect(content().string(containsString("href=\"?lang=en\"")));

        mvc.perform(get("/").session(sesion).with(user(cliente())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("lang=\"" + idioma + "\"")))
                .andExpect(content().string(containsString("<h1>" + portada + "</h1>")));
    }

    @Test
    void clienteNoPuedeAdministrarCupones() throws Exception {
        mvc.perform(get("/cupon/nuevo").with(user(cliente()))).andExpect(status().isForbidden());
    }

    @Test
    void registroSinCsrfNoGuarda() throws Exception {
        mvc.perform(post("/registro/guardar")).andExpect(status().isForbidden());
        verifyNoInteractions(servicio);
    }

    @Test
    void registroValidoRedirigeALogin() throws Exception {
        mvc.perform(post("/registro/guardar").with(csrf())
                .param("correo", "cliente@example.com").param("contrasena", "segura-nueva")
                .param("confirmacion", "segura-nueva").param("rol", "ADMIN"))
                .andExpect(redirectedUrl("/login")).andExpect(flash().attributeExists("todoOk"));
        verify(servicio).registrar(any());
    }

    @Test
    void formularioInvalidoNoGuarda() throws Exception {
        mvc.perform(post("/registro/guardar").with(csrf())
                .param("correo", "no-es-correo").param("contrasena", "corta"))
                .andExpect(redirectedUrl("/registro/nuevo")).andExpect(flash().attributeExists("error"));
        verifyNoInteractions(servicio);
    }

    @Test
    void perfilUsaIdentidadAutenticadaIgnorandoIdExterno() throws Exception {
        when(servicio.consultar(7L)).thenReturn(new PerfilVista("cliente@example.com"));
        mvc.perform(get("/perfil").with(user(cliente())).param("idUsuario", "99"))
                .andExpect(status().isOk()).andExpect(view().name("usuario/listado"))
                .andExpect(content().string(containsString("id=\"navbarCollapse\"")))
                .andExpect(content().string(containsString("/webjars/bootstrap/js/bootstrap.min.js")))
                .andExpect(content().string(not(containsString("class=\"tienda-admin\""))))
                .andExpect(content().string(not(containsString("hash-no-visible"))));
        verify(servicio).consultar(7L);
        verify(servicio, never()).consultar(99L);
    }

    @Test
    void confirmacionGetNoBorra() throws Exception {
        when(servicio.consultar(7L)).thenReturn(new PerfilVista("cliente@example.com"));
        mvc.perform(get("/perfil/eliminar").with(user(cliente())))
                .andExpect(status().isOk()).andExpect(view().name("usuario/eliminar"));
        verify(servicio, never()).eliminar(anyLong());
    }

    @Test
    void borradoSinCsrfNoBorra() throws Exception {
        mvc.perform(post("/perfil/eliminar").with(user(cliente()))).andExpect(status().isForbidden());
        verifyNoInteractions(servicio);
    }

    @Test
    void borradoConAsociacionesMuestraError() throws Exception {
        doThrow(new DataIntegrityViolationException("FK")).when(servicio).eliminar(7L);
        mvc.perform(post("/perfil/eliminar").with(user(cliente())).with(csrf()).param("idUsuario", "99"))
                .andExpect(redirectedUrl("/perfil")).andExpect(flash().attributeExists("error"));
        verify(servicio).eliminar(7L);
    }

    @Test
    void borradoExitosoCierraSesion() throws Exception {
        mvc.perform(post("/perfil/eliminar").with(user(cliente())).with(csrf()))
                .andExpect(redirectedUrl("/login?eliminado"));
        verify(servicio).eliminar(7L);
    }

    @Test
    void loginRenderizaSinAccesoLibrePorRol() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
    }

    @Test
    void loginAutenticaPorCorreoYBCrypt() throws Exception {
        var usuario = new Usuario();
        usuario.setIdUsuario(7L);
        usuario.setCorreo("cliente@example.com");
        usuario.setContrasena(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("segura-nueva"));
        usuario.setRol(Rol.CLIENTE);
        when(detalles.loadUserByUsername("cliente@example.com")).thenReturn(new UsuarioAutenticado(usuario));
        mvc.perform(post("/login").with(csrf()).param("correo", "cliente@example.com")
                .param("contrasena", "segura-nueva"))
                .andExpect(redirectedUrl("/perfil"));
    }

    @Test
    void modificarRenderizaCorreoSinHash() throws Exception {
        when(servicio.consultar(7L)).thenReturn(new PerfilVista("cliente@example.com"));
        mvc.perform(get("/perfil/modifica").with(user(cliente())))
                .andExpect(status().isOk()).andExpect(view().name("usuario/modifica"));
    }

    @Test
    void modificarUsaIdAutenticadoYCierraSesion() throws Exception {
        mvc.perform(post("/perfil/guardar").with(user(cliente())).with(csrf())
                .param("correo", "nuevo@example.com").param("contrasenaActual", "segura-anterior")
                .param("idUsuario", "99").param("rol", "ADMIN"))
                .andExpect(redirectedUrl("/login?actualizado"));
        verify(servicio).modificar(eq(7L), any());
    }

    @Test
    void loginRechazaContrasenaIncorrecta() throws Exception {
        when(detalles.loadUserByUsername("cliente@example.com")).thenReturn(cliente());
        mvc.perform(post("/login").with(csrf()).param("correo", "cliente@example.com")
                .param("contrasena", "incorrecta"))
                .andExpect(redirectedUrl("/login?error"));
    }
}
