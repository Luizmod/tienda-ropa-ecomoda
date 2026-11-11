package com.tienda.controller;

import com.tienda.dto.PrendaFormulario;
import com.tienda.service.CategoriaService;
import com.tienda.service.ImagenLocalService.ImagenException;
import com.tienda.service.PrendaService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prenda")
@RequiredArgsConstructor
public class PrendaController {
    private static final String LISTADO = "redirect:/prenda/listado";
    private final PrendaService servicio;
    private final CategoriaService categorias;
    private final MessageSource mensajes;

    @GetMapping("/listado")
    public String listar(Model modelo) {
        modelo.addAttribute("registros", servicio.listar());
        return "prenda/listado";
    }

    @GetMapping({"/nuevo", "/modificar/{id}"})
    public String editar(@PathVariable(required = false) Long id, Model modelo) {
        modelo.addAttribute("formulario", id == null ? new PrendaFormulario(null, null, null, null, null, null)
                : servicio.formulario(id));
        return formulario(id, modelo);
    }

    @PostMapping({"/nuevo", "/modificar/{id}"})
    public String guardar(@PathVariable(required = false) Long id,
            @Valid @ModelAttribute("formulario") PrendaFormulario formulario, BindingResult resultado,
            @RequestParam(required = false) MultipartFile imagen, Model modelo, RedirectAttributes atributos, Locale idioma) {
        if (resultado.hasErrors() || !formulario.varianteValida() || (id == null && formulario.cantidad() == null)) {
            modelo.addAttribute("error", mensajes.getMessage("prenda.invalida", null, idioma));
            return formulario(id, modelo);
        }
        try {
            servicio.guardar(id, formulario, imagen);
        } catch (ImagenException ex) {
            modelo.addAttribute("error", mensajes.getMessage(ex.getMessage(), null, idioma));
            return formulario(id, modelo);
        }
        atributos.addFlashAttribute("todoOk", mensajes.getMessage("prenda.guardada", null, idioma));
        return LISTADO;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes atributos, Locale idioma) {
        try {
            servicio.eliminar(id);
            atributos.addFlashAttribute("todoOk", mensajes.getMessage("prenda.eliminada", null, idioma));
        } catch (DataIntegrityViolationException ex) {
            atributos.addFlashAttribute("error", mensajes.getMessage("prenda.asociada", null, idioma));
        }
        return LISTADO;
    }

    private String formulario(Long id, Model modelo) {
        modelo.addAttribute("id", id);
        modelo.addAttribute("categorias", categorias.getCategorias());
        modelo.addAttribute("variantes", id == null ? List.of() : servicio.variantes(id));
        modelo.addAttribute("imagenActual", id == null ? null : servicio.imagenActual(id));
        return "prenda/modifica";
    }
}
