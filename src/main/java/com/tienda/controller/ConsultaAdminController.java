package com.tienda.controller;

import com.tienda.service.ConsultaService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/consulta")
@RequiredArgsConstructor
public class ConsultaAdminController {
    private final ConsultaService servicio;
    private final MessageSource mensajes;

    @GetMapping("/listado")
    public String listar(Model modelo) {
        modelo.addAttribute("consultas", servicio.listarParaAdministracion());
        return "consulta/admin";
    }

    @GetMapping("/responder/{id}")
    public String editar(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("consulta", servicio.consultarParaAdministracion(id));
        return "consulta/responder";
    }

    @PostMapping("/responder/{id}")
    public String responder(@PathVariable Long id, @RequestParam String respuesta,
            Model modelo, RedirectAttributes atributos, Locale idioma) {
        if (respuesta.isBlank() || respuesta.length() > 8000) {
            modelo.addAttribute("error", mensajes.getMessage("consulta.respuestaInvalida", null, idioma));
            return editar(id, modelo);
        }
        servicio.responder(id, respuesta.strip());
        atributos.addFlashAttribute("todoOk", mensajes.getMessage("consulta.respuestaGuardada", null, idioma));
        return "redirect:/admin/consulta/listado";
    }
}
