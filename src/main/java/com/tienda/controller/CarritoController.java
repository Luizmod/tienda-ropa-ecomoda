package com.tienda.controller;

import com.tienda.dto.LineaCarritoVista;
import com.tienda.service.CarritoService;
import com.tienda.service.CarritoService.CarritoException;
import com.tienda.service.CarritoService.CarritoInvitado;
import com.tienda.service.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.WebUtils;

@Controller
@RequiredArgsConstructor
public class CarritoController {
    private static final String LISTADO = "redirect:/carrito/listado";
    private static final String CARRITO_INVITADO = "carritoInvitado";
    private final CarritoService servicio;
    private final MessageSource mensajes;

    @GetMapping("/catalogo/listado")
    public String catalogo(Model modelo) {
        modelo.addAttribute("variantes", servicio.catalogo());
        return "carrito/catalogo";
    }

    @GetMapping("/carrito/listado")
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario, HttpSession sesion,
            Model modelo, Locale idioma) {
        return mostrar(usuario, sesion, modelo, idioma, false);
    }

    @GetMapping("/carrito/compra")
    public String revisarCompra(@AuthenticationPrincipal UsuarioAutenticado usuario, HttpSession sesion,
            Model modelo, Locale idioma) {
        return mostrar(usuario, sesion, modelo, idioma, true);
    }

    private String mostrar(UsuarioAutenticado usuario, HttpSession sesion, Model modelo,
            Locale idioma, boolean revisandoCompra) {
        List<LineaCarritoVista> lineas;
        if (usuario == null) {
            lineas = conCarritoInvitado(sesion, servicio::listarInvitado);
        } else {
            incorporarInvitado(usuario.getIdUsuario(), sesion, modelo, idioma);
            lineas = servicio.listar(usuario.getIdUsuario());
        }
        BigDecimal total = lineas.stream().map(LineaCarritoVista::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        modelo.addAttribute("lineas", lineas);
        modelo.addAttribute("total", total);
        modelo.addAttribute("revisandoCompra", revisandoCompra);
        return "carrito/listado";
    }

    @PostMapping("/carrito/agregar/{idPrenda}")
    public String agregar(@AuthenticationPrincipal UsuarioAutenticado usuario, HttpSession sesion,
            @PathVariable Long idPrenda,
            @RequestParam String talla, RedirectAttributes atributos, Locale idioma) {
        if (usuario == null) {
            cambiarInvitado(sesion, carrito -> servicio.agregarInvitado(carrito, idPrenda, talla));
        } else {
            servicio.agregar(usuario.getIdUsuario(), idPrenda, talla);
        }
        return exito(atributos, idioma, "carrito.agregado");
    }

    @GetMapping("/carrito/modifica/{idLinea}")
    public String editar(@AuthenticationPrincipal UsuarioAutenticado usuario, HttpSession sesion, @PathVariable Long idLinea,
            Model modelo) {
        modelo.addAttribute("linea", consultar(usuario, sesion, idLinea));
        return "carrito/modifica";
    }

    @PostMapping("/carrito/modifica/{idLinea}")
    public String modificar(@AuthenticationPrincipal UsuarioAutenticado usuario, HttpSession sesion,
            @PathVariable Long idLinea,
            @RequestParam int cantidad, RedirectAttributes atributos, Locale idioma) {
        if (usuario == null) {
            cambiarInvitado(sesion, carrito -> servicio.modificarInvitado(carrito, idLinea, cantidad));
        } else {
            servicio.modificar(usuario.getIdUsuario(), idLinea, cantidad);
        }
        return exito(atributos, idioma, "carrito.actualizado");
    }

    @GetMapping("/carrito/eliminar/{idLinea}")
    public String confirmar(@AuthenticationPrincipal UsuarioAutenticado usuario, HttpSession sesion,
            @PathVariable Long idLinea,
            Model modelo) {
        modelo.addAttribute("linea", consultar(usuario, sesion, idLinea));
        return "carrito/eliminar";
    }

    @PostMapping("/carrito/eliminar/{idLinea}")
    public String eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario, HttpSession sesion,
            @PathVariable Long idLinea,
            RedirectAttributes atributos, Locale idioma) {
        try {
            if (usuario == null) {
                cambiarInvitado(sesion, carrito -> servicio.eliminarInvitado(carrito, idLinea));
            } else {
                servicio.eliminar(usuario.getIdUsuario(), idLinea);
            }
        } catch (DataIntegrityViolationException ex) {
            atributos.addFlashAttribute("error", mensajes.getMessage("carrito.linea.asociada", null, idioma));
            return LISTADO;
        }
        return exito(atributos, idioma, "carrito.eliminado");
    }

    private LineaCarritoVista consultar(UsuarioAutenticado usuario, HttpSession sesion, Long idLinea) {
        return usuario == null
                ? conCarritoInvitado(sesion, carrito -> servicio.consultarInvitado(carrito, idLinea))
                : servicio.consultar(usuario.getIdUsuario(), idLinea);
    }

    private <T> T conCarritoInvitado(HttpSession sesion, Function<CarritoInvitado, T> operacion) {
        synchronized (WebUtils.getSessionMutex(sesion)) {
            var carrito = (CarritoInvitado) sesion.getAttribute(CARRITO_INVITADO);
            if (carrito == null) {
                carrito = new CarritoInvitado();
                sesion.setAttribute(CARRITO_INVITADO, carrito);
            }
            return operacion.apply(carrito);
        }
    }

    private void cambiarInvitado(HttpSession sesion, Consumer<CarritoInvitado> operacion) {
        conCarritoInvitado(sesion, carrito -> {
            operacion.accept(carrito);
            return null;
        });
    }

    private void incorporarInvitado(Long idUsuario, HttpSession sesion, Model modelo, Locale idioma) {
        synchronized (WebUtils.getSessionMutex(sesion)) {
            var carrito = (CarritoInvitado) sesion.getAttribute(CARRITO_INVITADO);
            if (carrito == null) {
                return;
            }
            boolean completo = servicio.incorporarInvitado(idUsuario, carrito);
            sesion.removeAttribute(CARRITO_INVITADO);
            if (!completo) {
                modelo.addAttribute("error", mensajes.getMessage("carrito.invitado.ajustado", null, idioma));
            }
        }
    }

    @ExceptionHandler(CarritoException.class)
    public String error(CarritoException ex, HttpServletRequest solicitud, Locale idioma) {
        RequestContextUtils.getOutputFlashMap(solicitud)
                .put("error", mensajes.getMessage(ex.getMessage(), null, idioma));
        return LISTADO;
    }

    private String exito(RedirectAttributes atributos, Locale idioma, String clave) {
        atributos.addFlashAttribute("todoOk", mensajes.getMessage(clave, null, idioma));
        return LISTADO;
    }
}
