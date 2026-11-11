package com.tienda.controller;

import com.tienda.service.PromocionesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class IndexController {
    private final PromocionesService promociones;

    @GetMapping("/")
    public String listado(Model modelo) {
        modelo.addAttribute("promociones", promociones.listarVigentes());
        return "index";
    }

}
