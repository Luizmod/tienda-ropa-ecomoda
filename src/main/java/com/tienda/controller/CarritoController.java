package com.tienda.controller;

import com.tienda.dto.LineaCarritoVista;
import com.tienda.service.CarritoService;
import com.tienda.service.CarritoService.CarritoException;
import com.tienda.service.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

@Controller
@RequiredArgsConstructor
public class CarritoController {
    private static final String LISTADO = "redirect:/carrito/listado";
    private final CarritoService servicio;
    private final MessageSource mensajes;

    @GetMapping("/catalogo/listado")
    public String catalogo(Model modelo) {
        modelo.addAttribute("variantes", servicio.catalogo());
        return "carrito/catalogo";
    }

    @GetMapping("/carrito/listado")
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model modelo) {
        var lineas = servicio.listar(usuario.getIdUsuario());
        BigDecimal total = lineas.stream().map(LineaCarritoVista::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        modelo.addAttribute("lineas", lineas);
        modelo.addAttribute("total", total);
        return "carrito/listado";
    }

    @PostMapping("/carrito/agregar/{idPrenda}")
    public String agregar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long idPrenda,
            @RequestParam String talla, RedirectAttributes atributos, Locale idioma) {
        servicio.agregar(usuario.getIdUsuario(), idPrenda, talla);
        return exito(atributos, idioma, "carrito.agregado");
    }

    @GetMapping("/carrito/modifica/{idLinea}")
    public String editar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long idLinea,
            Model modelo) {
        modelo.addAttribute("linea", servicio.consultar(usuario.getIdUsuario(), idLinea));
        return "carrito/modifica";
    }

    @PostMapping("/carrito/modifica/{idLinea}")
    public String modificar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long idLinea,
            @RequestParam int cantidad, RedirectAttributes atributos, Locale idioma) {
        servicio.modificar(usuario.getIdUsuario(), idLinea, cantidad);
        return exito(atributos, idioma, "carrito.actualizado");
    }

    @GetMapping("/carrito/eliminar/{idLinea}")
    public String confirmar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long idLinea,
            Model modelo) {
        modelo.addAttribute("linea", servicio.consultar(usuario.getIdUsuario(), idLinea));
        return "carrito/eliminar";
    }

    @PostMapping("/carrito/eliminar/{idLinea}")
    public String eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long idLinea,
            RedirectAttributes atributos, Locale idioma) {
        try {
            servicio.eliminar(usuario.getIdUsuario(), idLinea);
        } catch (DataIntegrityViolationException ex) {
            atributos.addFlashAttribute("error", mensajes.getMessage("carrito.linea.asociada", null, idioma));
            return LISTADO;
        }
        return exito(atributos, idioma, "carrito.eliminado");
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
