package com.tienda.controller;

import com.tienda.dto.DireccionFormulario;
import com.tienda.service.DireccionService;
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
@RequestMapping("/direccion")
@RequiredArgsConstructor
public class DireccionController {
    private static final String LISTADO = "redirect:/direccion/listado";
    private final DireccionService servicio;
    private final MessageSource mensajes;

    @GetMapping("/listado")
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model modelo) {
        modelo.addAttribute("registros", servicio.listar(usuario.getIdUsuario()));
        return "direccion/listado";
    }

    @GetMapping({"/nuevo", "/modificar/{id}"})
    public String editar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable(required = false) Long id, Model modelo) {
        modelo.addAttribute("formulario", id == null ? new DireccionFormulario(null, null, null, null, null)
                : new DireccionFormulario(servicio.consultar(usuario.getIdUsuario(), id)));
        return formulario(id, modelo);
    }

    @PostMapping({"/nuevo", "/modificar/{id}"})
    public String guardar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable(required = false) Long id, @Valid @ModelAttribute("formulario") DireccionFormulario formulario,
            BindingResult resultado, Model modelo, RedirectAttributes atributos, Locale idioma) {
        if (resultado.hasErrors()) {
            modelo.addAttribute("error", mensajes.getMessage("direccion.invalido", null, idioma));
            return formulario(id, modelo);
        }
        servicio.guardar(usuario.getIdUsuario(), id, formulario);
        atributos.addFlashAttribute("todoOk", mensajes.getMessage("direccion.guardado", null, idioma));
        return LISTADO;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
            RedirectAttributes atributos, Locale idioma) {
        try {
            servicio.eliminar(usuario.getIdUsuario(), id);
            atributos.addFlashAttribute("todoOk", mensajes.getMessage("direccion.eliminado", null, idioma));
        } catch (DataIntegrityViolationException ex) {
            atributos.addFlashAttribute("error", mensajes.getMessage("direccion.asociado", null, idioma));
        }
        return LISTADO;
    }

    private String formulario(Long id, Model modelo) {
        modelo.addAttribute("id", id);
        return "direccion/modifica";
    }
}
