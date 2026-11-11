
package com.tienda.controller;

import com.tienda.dto.ConsultaFormulario;
import com.tienda.service.ConsultaService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/consulta")
@RequiredArgsConstructor
public class ConsultaController {

    private final ConsultaService consultaService;
    private final MessageSource mensajes;

    @GetMapping("/listado")
    public String listado(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("consultas", consultaService.getConsultas(usuario.getIdUsuario()));
        model.addAttribute("formulario", new ConsultaFormulario(null, usuario.getUsername(), null, null));
        return "consulta/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @ModelAttribute("formulario") ConsultaFormulario formulario, BindingResult resultado,
            Model modelo, RedirectAttributes atributos, Locale idioma) {
        if (resultado.hasErrors()) {
            modelo.addAttribute("error", mensajes.getMessage("consulta.invalida", null, idioma));
            return "consulta/modifica";
        }
        consultaService.save(usuario.getIdUsuario(), formulario.idConsulta(),
                formulario.correo(), formulario.asunto().strip(), formulario.mensaje().strip());
        atributos.addFlashAttribute("todoOk", mensajes.getMessage("consulta.guardada", null, idioma));
        return "redirect:/consulta/listado";
    }

    @GetMapping("/modificar/{idConsulta}")
    public String modificar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long idConsulta, Model model) {
        var consulta = consultaService.getConsulta(usuario.getIdUsuario(), idConsulta);
        model.addAttribute("formulario", new ConsultaFormulario(consulta));
        return "consulta/modifica";
    }

    @PostMapping("/eliminar/{idConsulta}")
    public String eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long idConsulta, RedirectAttributes atributos, Locale idioma) {
        try {
            consultaService.delete(usuario.getIdUsuario(), idConsulta);
            atributos.addFlashAttribute("todoOk", mensajes.getMessage("consulta.eliminada", null, idioma));
        } catch (DataIntegrityViolationException ex) {
            atributos.addFlashAttribute("error", mensajes.getMessage("consulta.asociada", null, idioma));
        }
        return "redirect:/consulta/listado";
    }
}
