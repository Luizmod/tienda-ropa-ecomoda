package com.tienda.controller;

import com.tienda.dto.MetodoPagoFormulario;
import com.tienda.domain.MetodoPago.Tipo;
import com.tienda.service.MetodoPagoService;
import com.tienda.service.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/metodopago")
@RequiredArgsConstructor
public class MetodoPagoController {
    private static final String LISTADO = "redirect:/metodopago/listado";
    private final MetodoPagoService servicio;
    private final MessageSource mensajes;

    @GetMapping("/listado")
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model modelo) {
        modelo.addAttribute("registros", servicio.listar(usuario.getIdUsuario()));
        return "metodopago/listado";
    }

    @GetMapping({"/nuevo", "/modificar/{id}"})
    public String editar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable(required = false) Long id, Model modelo) {
        modelo.addAttribute("formulario", id == null ? new MetodoPagoFormulario(null, null, null, null)
                : new MetodoPagoFormulario(servicio.consultar(usuario.getIdUsuario(), id)));
        return formulario(id, modelo);
    }

    @PostMapping({"/nuevo", "/modificar/{id}"})
    public String guardar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable(required = false) Long id, @Valid @ModelAttribute("formulario") MetodoPagoFormulario formulario,
            BindingResult resultado, Model modelo, RedirectAttributes atributos, Locale idioma) {
        if (resultado.hasErrors() || !formulario.datosValidos()) {
            modelo.addAttribute("error", mensajes.getMessage("metodopago.invalido", null, idioma));
            return formulario(id, modelo);
        }
        servicio.guardar(usuario.getIdUsuario(), id, formulario);
        atributos.addFlashAttribute("todoOk", mensajes.getMessage("metodopago.guardado", null, idioma));
        return LISTADO;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
            RedirectAttributes atributos, Locale idioma) {
        try {
            servicio.eliminar(usuario.getIdUsuario(), id);
            atributos.addFlashAttribute("todoOk", mensajes.getMessage("metodopago.eliminado", null, idioma));
        } catch (DataIntegrityViolationException ex) {
            atributos.addFlashAttribute("error", mensajes.getMessage("metodopago.asociado", null, idioma));
        }
        return LISTADO;
    }

    private String formulario(Long id, Model modelo) {
        modelo.addAttribute("id", id);
        modelo.addAttribute("tipos", Tipo.values());
        return "metodopago/modifica";
    }
}
