package com.tienda.controller;

import com.tienda.SecurityConfig;
import com.tienda.domain.Cupon;
import com.tienda.dto.CuponFormulario;
import com.tienda.service.CuponService;
import com.tienda.service.CuponService.CuponNoEncontradoException;
import com.tienda.service.UsuarioDetailsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuponController.class)
@Import(SecurityConfig.class)
class CuponControllerTest {
    private static final LocalDate VENCIMIENTO = LocalDate.of(2027, 5, 20);
    private static final BigDecimal DESCUENTO = new BigDecimal("12.50");
    @Autowired private MockMvc mvc;
    @MockitoBean private CuponService servicio;
    @MockitoBean private UsuarioDetailsService detalles;

    @Test
    void visitantePuedeVerCuponesVigentesSinAccionesDeAdministracion() throws Exception {
        Cupon cupon = new Cupon();
        ReflectionTestUtils.setField(cupon, "idCupon", 7L);
        cupon.actualizar("MODA", DESCUENTO, VENCIMIENTO, true);
        when(servicio.listarVigentes()).thenReturn(List.of(cupon));

        mvc.perform(get("/cupon/listado"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("MODA")))
                .andExpect(content().string(not(containsString("/cupon/modifica/7"))));
        mvc.perform(get("/cupon/nuevo")).andExpect(redirectedUrl("/login"));
        verify(servicio).listarVigentes();
        verify(servicio, never()).listar();
    }

    @Test
    void administradorPuedeListarYEditar() throws Exception {
        Cupon cupon = new Cupon();
        ReflectionTestUtils.setField(cupon, "idCupon", 7L);
        cupon.actualizar("MODA", DESCUENTO, VENCIMIENTO, false);
        when(servicio.listar()).thenReturn(List.of(cupon));
        when(servicio.consultar(7L)).thenReturn(cupon);

        mvc.perform(get("/cupon/listado").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("MODA")));
        mvc.perform(get("/cupon/modifica/7").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/cupon/modifica/7\"")))
                .andExpect(content().string(containsString("value=\"2027-05-20\"")));
    }

    @Test
    void formularioValidoGuardaYElDuplicadoConservaLosDatos() throws Exception {
        var solicitud = post("/cupon/nuevo").with(user("admin").roles("ADMIN")).with(csrf())
                .param("codigo", "MODA").param("porcentaje", "12.50")
                .param("fechaVencimiento", VENCIMIENTO.toString()).param("activo", "false");
        var formulario = new CuponFormulario("MODA", DESCUENTO, VENCIMIENTO, false);

        mvc.perform(solicitud).andExpect(redirectedUrl("/cupon/listado"))
                .andExpect(flash().attributeExists("todoOk"));
        verify(servicio).guardar(null, formulario);

        doThrow(new DataIntegrityViolationException("UNIQUE")).when(servicio).guardar(null, formulario);
        mvc.perform(solicitud).andExpect(status().isOk())
                .andExpect(model().attribute("formulario", formulario))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void formularioInvalidoYClienteNoGuardan() throws Exception {
        mvc.perform(post("/cupon/nuevo").with(user("admin").roles("ADMIN")).with(csrf())
                .param("codigo", "MODA").param("porcentaje", "101")
                .param("fechaVencimiento", VENCIMIENTO.toString()).param("activo", "true"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("formulario", "porcentaje"));
        mvc.perform(post("/cupon/nuevo").with(user("cliente").roles("CLIENTE")).with(csrf()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(servicio);
    }

    @Test
    void confirmarNoBorraYLaEliminacionRequiereCsrf() throws Exception {
        Cupon cupon = new Cupon();
        ReflectionTestUtils.setField(cupon, "idCupon", 7L);
        cupon.actualizar("MODA", DESCUENTO, VENCIMIENTO, true);
        when(servicio.consultar(7L)).thenReturn(cupon);

        mvc.perform(get("/cupon/eliminar/7").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(view().name("cupon/eliminar"));
        mvc.perform(post("/cupon/eliminar/7").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        verify(servicio, never()).eliminar(7L);

        doThrow(new DataIntegrityViolationException("FK")).when(servicio).eliminar(7L);
        mvc.perform(post("/cupon/eliminar/7").with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(redirectedUrl("/cupon/listado")).andExpect(flash().attributeExists("error"));
    }

    @Test
    void cuponInexistenteVuelveAlListado() throws Exception {
        when(servicio.consultar(7L)).thenThrow(new CuponNoEncontradoException());
        mvc.perform(get("/cupon/modifica/7").with(user("admin").roles("ADMIN")))
                .andExpect(redirectedUrl("/cupon/listado")).andExpect(flash().attributeExists("error"));
    }
}
