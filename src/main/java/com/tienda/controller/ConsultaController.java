
package com.tienda.controller;

import com.tienda.domain.Consulta;
import com.tienda.service.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/consulta")
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;

    @GetMapping("/listado")
    public String listado(Model model) {
        model.addAttribute("consultas", consultaService.getConsultas());
        model.addAttribute("consulta", new Consulta());
        return "consulta/listado";
    }

    @PostMapping("/guardar")
    public String guardar(Consulta consulta) {
        consultaService.save(consulta);
        return "redirect:/consulta/listado";
    }

    @GetMapping("/modificar/{idConsulta}")
    public String modificar(@PathVariable("idConsulta") Long idConsulta, Model model) {
        Consulta consulta = consultaService.getConsulta(idConsulta);
        model.addAttribute("consultas", consultaService.getConsultas());
        model.addAttribute("consulta", consulta);
        return "consulta/listado";
    }

    @GetMapping("/eliminar/{idConsulta}")
    public String eliminar(Consulta consulta) {
        consultaService.delete(consulta);
        return "redirect:/consulta/listado";
    }
}