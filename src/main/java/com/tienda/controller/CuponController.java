package com.tienda.controller;

import com.tienda.domain.Cupon;
import com.tienda.dto.CuponFormulario;
import com.tienda.service.CuponService;
import com.tienda.service.CuponService.CuponNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.servlet.support.RequestContextUtils;

@Controller
@RequestMapping("/cupon")
@RequiredArgsConstructor
public class CuponController {
    private static final String LISTADO = "redirect:/cupon/listado";
    private final CuponService servicio;
    private final MessageSource mensajes;

    @GetMapping("/listado")
    public String listar(Model modelo) {
        modelo.addAttribute("cupones", servicio.listar());
        return "cupon/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("formulario", new CuponFormulario(null, null, null, true));
        return "cupon/modifica";
    }

    @GetMapping("/modifica/{id}")
    public String editar(@PathVariable Long id, Model modelo) {
        Cupon cupon = servicio.consultar(id);
        modelo.addAttribute("idCupon", id);
        modelo.addAttribute("formulario", new CuponFormulario(cupon.getCodigo(), cupon.getPorcentaje(),
                cupon.getFechaVencimiento(), cupon.isActivo()));
        return "cupon/modifica";
    }

    @PostMapping({"/nuevo", "/modifica/{id}"})
    public String guardar(@PathVariable(required = false) Long id,
            @Valid @ModelAttribute("formulario") CuponFormulario formulario, BindingResult resultado,
            Model modelo, RedirectAttributes atributos, Locale idioma) {
        if (resultado.hasErrors()) {
            modelo.addAttribute("idCupon", id);
            modelo.addAttribute("error", mensajes.getMessage("cupon.invalido", null, idioma));
            return "cupon/modifica";
        }
        try {
            servicio.guardar(id, formulario);
        } catch (DataIntegrityViolationException ex) {
            modelo.addAttribute("idCupon", id);
            modelo.addAttribute("error", mensajes.getMessage("cupon.duplicado", null, idioma));
            return "cupon/modifica";
        }
        return redirigir(atributos, idioma, "todoOk", "cupon.guardado");
    }

    @GetMapping("/eliminar/{id}")
    public String confirmar(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("cupon", servicio.consultar(id));
        return "cupon/eliminar";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes atributos, Locale idioma) {
        try {
            servicio.eliminar(id);
        } catch (DataIntegrityViolationException ex) {
            return redirigir(atributos, idioma, "error", "cupon.asociado");
        }
        return redirigir(atributos, idioma, "todoOk", "cupon.eliminado");
    }

    @ExceptionHandler(CuponNoEncontradoException.class)
    public String noEncontrado(HttpServletRequest solicitud, Locale idioma) {
        RequestContextUtils.getOutputFlashMap(solicitud)
                .put("error", mensajes.getMessage("cupon.noEncontrado", null, idioma));
        return LISTADO;
    }

    private String redirigir(RedirectAttributes atributos, Locale idioma, String tipo, String clave) {
        atributos.addFlashAttribute(tipo, mensajes.getMessage(clave, null, idioma));
        return LISTADO;
    }
}
