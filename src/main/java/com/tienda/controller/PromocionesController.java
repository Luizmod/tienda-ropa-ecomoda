
package com.tienda.controller;

import com.tienda.domain.Promociones;
import com.tienda.service.PromocionesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/promocion")
public class PromocionesController {

    @Autowired
    private PromocionesService promocionesService;

    @GetMapping("/listado")
    public String listado(Model model) {
        var promociones = promocionesService.getPromociones();
        model.addAttribute("promociones", promociones);
        return "promociones/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("promocion", new Promociones());
        return "promociones/modifica"; 
    }

    @PostMapping("/guardar")
    public String guardar(Promociones promociones) {
        promocionesService.save(promociones);
        return "redirect:/promocion/listado";
    }

    @GetMapping("/modificar/{idPromocion}")
    public String modificar(@PathVariable("idPromocion") Long idPromocion, Model model) {
        Promociones promocion = promocionesService.getPromocion(idPromocion);
        model.addAttribute("promocion", promocion);
        return "promociones/modifica";
    }

    @GetMapping("/eliminar/{idPromocion}")
    public String eliminar(Promociones promociones) {
        promocionesService.delete(promociones);
        return "redirect:/promocion/listado";
    }
}
