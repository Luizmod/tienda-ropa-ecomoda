package com.tienda.controller;

import com.tienda.SecurityConfig;
import com.tienda.domain.Consulta;
import com.tienda.domain.Promociones;
import com.tienda.domain.Rol;
import com.tienda.domain.Usuario;
import com.tienda.repository.ConsultaRepository;
import com.tienda.service.ConsultaService;
import com.tienda.service.PromocionesService;
import com.tienda.service.UsuarioAutenticado;
import com.tienda.service.UsuarioDetailsService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ConsultaController.class, PromocionesController.class})
@Import({SecurityConfig.class, ConsultaService.class})
class AccesoConsultaPromocionTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private ConsultaRepository consultas;
    @MockitoBean private PromocionesService promociones;
    @MockitoBean private UsuarioDetailsService detalles;

    @Test
    void consultasSeFiltranPorLaCuentaAutenticada() throws Exception {
        var cliente = cliente();
        mvc.perform(get("/consulta/listado").with(user(cliente)))
                .andExpect(status().isOk());
        verify(consultas).findByIdUsuario(12L);

        mvc.perform(get("/consulta/modificar/9").with(user(cliente)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/consulta/guardar").with(user(cliente)).with(csrf())
                .param("idConsulta", "9").param("correo", "cliente@example.com")
                .param("asunto", "Tallas").param("mensaje", "Consulta de tallas"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/consulta/eliminar/9").with(user(cliente)).with(csrf()))
                .andExpect(status().isNotFound());
        verify(consultas, times(3)).findByIdConsultaAndIdUsuario(9L, 12L);
        verify(consultas, never()).save(any(Consulta.class));
        verify(consultas, never()).delete(any(Consulta.class));
    }

    @Test
    void consultaNuevaTomaLaCuentaYElEstadoDelServidor() throws Exception {
        mvc.perform(post("/consulta/guardar").with(user(cliente())).with(csrf())
                .param("idConsulta", "").param("idUsuario", "99").param("estado", "RESPONDIDA")
                .param("correo", "cliente@example.com").param("asunto", "Tallas")
                .param("mensaje", "Consulta de tallas"))
                .andExpect(redirectedUrl("/consulta/listado"));
        verify(consultas).save(argThat(consulta -> consulta.getIdUsuario().equals(12L)
                && consulta.getEstado().equals("PENDIENTE") && consulta.getRespuesta() == null));
    }

    @Test
    void visitanteVeLasPromocionesYElAdministradorPuedeBorrarConCsrf() throws Exception {
        var promocion = new Promociones(9L);
        promocion.setNombre("Temporada");
        when(promociones.getPromociones()).thenReturn(List.of(promocion));
        when(promociones.listarVigentes()).thenReturn(List.of(promocion));
        var cliente = user(cliente());
        var admin = user("admin").roles("ADMIN");
        mvc.perform(get("/promocion/listado"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Temporada")))
                .andExpect(content().string(not(containsString("/promocion/eliminar/9"))));
        mvc.perform(get("/promocion/listado").with(cliente))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/promocion/eliminar/9"))));
        mvc.perform(get("/promocion/listado").with(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/promocion/eliminar/9\"")));
        mvc.perform(get("/promocion/modificar/9").with(cliente))
                .andExpect(status().isForbidden());
        mvc.perform(post("/promocion/guardar").with(cliente).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/promocion/eliminar/9").with(cliente).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/promocion/eliminar/9").with(admin))
                .andExpect(status().isForbidden());
        mvc.perform(post("/promocion/eliminar/9").with(admin).with(csrf()))
                .andExpect(redirectedUrl("/promocion/listado"))
                .andExpect(flash().attributeExists("todoOk"));
        verify(promociones).delete(9L);
    }

    private UsuarioAutenticado cliente() {
        var usuario = new Usuario();
        usuario.setIdUsuario(12L);
        usuario.setCorreo("cliente@example.com");
        usuario.setContrasena("");
        usuario.setRol(Rol.CLIENTE);
        return new UsuarioAutenticado(usuario);
    }
}
