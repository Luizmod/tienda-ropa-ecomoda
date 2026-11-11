package com.tienda.controller;

import com.tienda.SecurityConfig;
import com.tienda.domain.Cupon;
import com.tienda.domain.Inventario;
import com.tienda.domain.LineaCarrito;
import com.tienda.domain.Prenda;
import com.tienda.domain.Rol;
import com.tienda.domain.Usuario;
import com.tienda.dto.LineaCarritoVista;
import com.tienda.repository.CuponRepository;
import com.tienda.repository.InventarioRepository;
import com.tienda.repository.LineaCarritoRepository;
import com.tienda.repository.UsuarioRepository;
import com.tienda.service.CarritoService;
import com.tienda.service.CuponService;
import com.tienda.service.UsuarioAutenticado;
import com.tienda.service.UsuarioDetailsService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarritoController.class)
@Import({SecurityConfig.class, CarritoService.class, CuponService.class})
class CarritoControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private PasswordEncoder claves;
    @MockitoBean private InventarioRepository inventario;
    @MockitoBean private LineaCarritoRepository lineas;
    @MockitoBean private UsuarioRepository usuarios;
    @MockitoBean private UsuarioDetailsService detalles;
    @MockitoBean private CuponRepository cupones;

    @BeforeEach
    void prepararInventario() {
        var prenda = new Prenda();
        ReflectionTestUtils.setField(prenda, "idPrenda", 7L);
        ReflectionTestUtils.setField(prenda, "nombre", "Camiseta");
        ReflectionTestUtils.setField(prenda, "precio", new BigDecimal("7500.00"));
        var variante = new Inventario();
        ReflectionTestUtils.setField(variante, "idInventario", 5L);
        ReflectionTestUtils.setField(variante, "prenda", prenda);
        ReflectionTestUtils.setField(variante, "talla", "M");
        ReflectionTestUtils.setField(variante, "cantidad", 3);
        when(inventario.findAllByOrderByPrendaNombreAscTallaAsc()).thenReturn(List.of(variante));
        when(inventario.findByPrendaIdPrendaAndTalla(7L, "M")).thenReturn(Optional.of(variante));
        when(inventario.findByIdInventario(5L)).thenReturn(Optional.of(variante));
        when(inventario.findByIdInventarioInOrderByIdInventarioAsc(anyCollection())).thenReturn(List.of(variante));
    }

    @Test
    void visitanteAgregaEditaYQuitaPrendasEnSuSesion() throws Exception {
        var sesion = new MockHttpSession();
        mvc.perform(get("/catalogo/listado").session(sesion)).andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/carrito/agregar/7\"")));
        agregar(sesion);
        mvc.perform(get("/carrito/listado").session(sesion)).andExpect(status().isOk())
                .andExpect(content().string(containsString("/carrito/compra")))
                .andExpect(model().attribute("total", new BigDecimal("7500.00")));
        mvc.perform(get("/carrito/listado")).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Camiseta"))));
        mvc.perform(get("/carrito/modifica/-5")).andExpect(redirectedUrl("/carrito/listado"))
                .andExpect(flash().attributeExists("error"));
        mvc.perform(get("/carrito/modifica/-5").session(sesion)).andExpect(status().isOk());
        mvc.perform(post("/carrito/modifica/-5").session(sesion).with(csrf()).param("cantidad", "4"))
                .andExpect(redirectedUrl("/carrito/listado")).andExpect(flash().attributeExists("error"));
        mvc.perform(post("/carrito/modifica/-5").session(sesion).with(csrf()).param("cantidad", "2"))
                .andExpect(redirectedUrl("/carrito/listado"));
        mvc.perform(get("/carrito/listado").session(sesion))
                .andExpect(model().attribute("total", new BigDecimal("15000.00")));
        mvc.perform(get("/carrito/eliminar/-5").session(sesion)).andExpect(status().isOk());
        mvc.perform(post("/carrito/eliminar/-5").session(sesion)).andExpect(status().isForbidden());
        mvc.perform(post("/carrito/eliminar/-5").session(sesion).with(csrf()))
                .andExpect(redirectedUrl("/carrito/listado"));
        mvc.perform(get("/carrito/listado").session(sesion))
                .andExpect(content().string(not(containsString("Camiseta"))));
        verifyNoInteractions(lineas, usuarios);
    }

    @Test
    void compraPideLoginYCombinaLosCarritosUnaSolaVezSegunExistencias() throws Exception {
        var sesion = new MockHttpSession();
        agregar(sesion);
        agregar(sesion);
        var usuario = new Usuario();
        usuario.setIdUsuario(9L);
        usuario.setCorreo("cliente@example.com");
        usuario.setContrasena(claves.encode("contrasena-prueba"));
        usuario.setRol(Rol.CLIENTE);
        when(detalles.loadUserByUsername(usuario.getCorreo())).thenReturn(new UsuarioAutenticado(usuario));
        when(usuarios.bloquearPorId(9L)).thenReturn(Optional.of(usuario));
        var existente = new LineaCarrito(9L, 7L, "M");
        existente.cambiarCantidad(2);
        when(lineas.findByIdUsuarioAndIdPrendaAndTalla(9L, 7L, "M")).thenReturn(Optional.of(existente));

        mvc.perform(get("/carrito/compra").session(sesion)).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/login").session(sesion).with(csrf())
                .param("correo", usuario.getCorreo()).param("contrasena", "contrasena-prueba"))
                .andExpect(header().string("Location", endsWith("/carrito/compra")));
        mvc.perform(get("/carrito/compra").session(sesion)).andExpect(status().isOk())
                .andExpect(model().attribute("revisandoCompra", true)).andExpect(model().attributeExists("error"));
        mvc.perform(get("/carrito/compra").session(sesion)).andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"));
        verify(lineas).save(argThat(linea -> linea == existente && linea.getCantidad() == 3));
        verify(usuarios).bloquearPorId(9L);
    }

    @Test
    void cuponPrivadoSoloDescuentaEnElCarritoDelTitular() throws Exception {
        var cupon = new Cupon();
        cupon.actualizar("CLIENTE25", new BigDecimal("25.00"), java.time.LocalDate.now().plusDays(30), true);
        ReflectionTestUtils.setField(cupon, "idUsuario", 9L);
        var linea = new LineaCarritoVista(1L, 7L, "Camiseta", "", null, "M",
                new BigDecimal("7500.00"), 1, 3);
        when(lineas.listarPorUsuario(9L)).thenReturn(List.of(linea));
        when(lineas.listarPorUsuario(10L)).thenReturn(List.of(linea));
        when(cupones.findByCodigoAndActivoTrueAndFechaVencimientoGreaterThanEqual(eq("CLIENTE25"), any()))
                .thenReturn(Optional.of(cupon));
        when(cupones.findByIdUsuarioAndActivoTrueAndFechaVencimientoGreaterThanEqualOrderByCodigoAsc(eq(9L), any()))
                .thenReturn(List.of(cupon));
        var sesion = new MockHttpSession();
        var titular = user(cliente(9L));
        var otro = user(cliente(10L));

        mvc.perform(get("/carrito/cupon")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/carrito/listado").with(otro))
                .andExpect(content().string(not(containsString("CLIENTE25"))));
        mvc.perform(post("/carrito/cupon/aplicar").with(otro).with(csrf()).param("codigo", "CLIENTE25"))
                .andExpect(redirectedUrl("/carrito/listado")).andExpect(flash().attributeExists("error"));
        mvc.perform(get("/carrito/listado").session(sesion).with(titular))
                .andExpect(content().string(containsString("CLIENTE25")));
        mvc.perform(post("/carrito/cupon/aplicar").session(sesion).with(titular).with(csrf())
                .param("codigo", " cliente25 "))
                .andExpect(redirectedUrl("/carrito/listado")).andExpect(flash().attributeExists("todoOk"));
        mvc.perform(get("/carrito/compra").session(sesion).with(titular))
                .andExpect(model().attribute("descuento", new BigDecimal("1875.00")))
                .andExpect(model().attribute("total", new BigDecimal("5625.00")));
        mvc.perform(post("/carrito/cupon/quitar").session(sesion).with(titular).with(csrf()))
                .andExpect(redirectedUrl("/carrito/listado"));
        mvc.perform(get("/carrito/listado").session(sesion).with(titular))
                .andExpect(model().attribute("total", new BigDecimal("7500.00")));
    }

    private UsuarioAutenticado cliente(long id) {
        var usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setCorreo("cliente" + id + "@example.com");
        usuario.setContrasena("prueba");
        usuario.setRol(Rol.CLIENTE);
        return new UsuarioAutenticado(usuario);
    }

    private void agregar(MockHttpSession sesion) throws Exception {
        mvc.perform(post("/carrito/agregar/7").session(sesion).with(csrf()).param("talla", "M"))
                .andExpect(redirectedUrl("/carrito/listado")).andExpect(flash().attributeExists("todoOk"));
    }
}
