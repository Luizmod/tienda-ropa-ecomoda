
package com.tienda.controller;

import com.tienda.domain.Promociones;
import com.tienda.service.PromocionesService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/promocion")
@RequiredArgsConstructor
public class PromocionesController {

    private final PromocionesService promocionesService;
    private final MessageSource mensajes;

    @GetMapping("/listado")
    public String listado(Model model, HttpServletRequest solicitud) {
        var promociones = solicitud.isUserInRole("ADMIN")
                ? promocionesService.getPromociones() : promocionesService.listarVigentes();
        model.addAttribute("promociones", promociones);
        return "promociones/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("promocion", new Promociones());
        return "promociones/modifica";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("promocion") Promociones promocion, BindingResult resultado,
            Model modelo, RedirectAttributes atributos, Locale idioma) {
        if (resultado.hasErrors() || promocion.getFechaFin().isBefore(promocion.getFechaInicio())) {
            modelo.addAttribute("error", mensajes.getMessage("promocion.invalida", null, idioma));
            return "promociones/modifica";
        }
        promocionesService.save(promocion);
        atributos.addFlashAttribute("todoOk", mensajes.getMessage("promocion.guardada", null, idioma));
        return "redirect:/promocion/listado";
    }

    @GetMapping("/modificar/{idPromocion}")
    public String modificar(@PathVariable("idPromocion") Long idPromocion, Model model) {
        Promociones promocion = promocionesService.getPromocion(idPromocion);
        model.addAttribute("promocion", promocion);
        return "promociones/modifica";
    }

    @PostMapping("/eliminar/{idPromocion}")
    public String eliminar(@PathVariable Long idPromocion, RedirectAttributes atributos, Locale idioma) {
        try {
            promocionesService.delete(idPromocion);
            atributos.addFlashAttribute("todoOk", mensajes.getMessage("promocion.eliminada", null, idioma));
        } catch (DataIntegrityViolationException ex) {
            atributos.addFlashAttribute("error", mensajes.getMessage("promocion.asociada", null, idioma));
        }
        return "redirect:/promocion/listado";
    }
}
