package com.tienda.controller;

import com.tienda.domain.Categoria;
import com.tienda.service.CategoriaService;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categoria")
@RequiredArgsConstructor
public class CategoriaController {
    private static final String LISTADO = "redirect:/categoria/listado";
    private final CategoriaService servicio;
    private final MessageSource mensajes;

    @GetMapping("/listado")
    public String listado(Model modelo) {
        modelo.addAttribute("registros", servicio.getCategorias());
        return "categoria/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("categoria", new Categoria());
        return "categoria/modifica";
    }

    @GetMapping("/modificar/{id}")
    public String editar(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("categoria", servicio.consultar(id));
        return "categoria/modifica";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Categoria categoria, BindingResult resultado, Model modelo,
            RedirectAttributes atributos, Locale idioma) {
        if (resultado.hasErrors()) {
            modelo.addAttribute("error", mensajes.getMessage("categoria.invalida", null, idioma));
            return "categoria/modifica";
        }
        try {
            servicio.save(categoria);
        } catch (DataIntegrityViolationException ex) {
            modelo.addAttribute("error", mensajes.getMessage("categoria.duplicada", null, idioma));
            return "categoria/modifica";
        }
        atributos.addFlashAttribute("todoOk", mensajes.getMessage("categoria.guardada", null, idioma));
        return LISTADO;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes atributos, Locale idioma) {
        try {
            servicio.delete(id);
            atributos.addFlashAttribute("todoOk", mensajes.getMessage("categoria.eliminada", null, idioma));
        } catch (DataIntegrityViolationException ex) {
            atributos.addFlashAttribute("error", mensajes.getMessage("categoria.error02", null, idioma));
        }
        return LISTADO;
    }
}
