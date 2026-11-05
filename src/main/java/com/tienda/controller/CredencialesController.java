package com.tienda.controller;

import com.tienda.dto.*;
import com.tienda.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CredencialesController {
    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    @GetMapping("/registro/nuevo")
    public String registro() {
        return "registro/nuevo";
    }

    @PostMapping("/registro/guardar")
    public String registrar(@Valid @ModelAttribute RegistroFormulario formulario, BindingResult resultado,
            RedirectAttributes mensajes, Locale idioma) {
        if (resultado.hasErrors()) {
            mensaje(mensajes, "error", "credenciales.formulario.invalido", idioma);
            return "redirect:/registro/nuevo";
        }
        try {
            usuarioService.registrar(formulario);
            mensaje(mensajes, "todoOk", "credenciales.registro.ok", idioma);
            return "redirect:/login";
        } catch (CredencialesException ex) {
            mensaje(mensajes, "error", ex.getMessage(), idioma);
        } catch (DataIntegrityViolationException ex) {
            mensaje(mensajes, "error", "credenciales.correo.duplicado", idioma);
        }
        return "redirect:/registro/nuevo";
    }

    @GetMapping("/perfil")
    public String perfil(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("perfil", usuarioService.consultar(usuario.getIdUsuario()));
        return "usuario/listado";
    }

    @GetMapping("/perfil/modifica")
    public String editar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("perfil", usuarioService.consultar(usuario.getIdUsuario()));
        return "usuario/modifica";
    }

    @GetMapping("/perfil/eliminar")
    public String confirmarEliminacion() {
        return "usuario/eliminar";
    }

    @PostMapping("/perfil/guardar")
    public String modificar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @ModelAttribute PerfilFormulario formulario, BindingResult resultado,
            RedirectAttributes mensajes, Locale idioma, Authentication autenticacion,
            HttpServletRequest solicitud, HttpServletResponse respuesta) {
        if (resultado.hasErrors()) {
            mensaje(mensajes, "error", "credenciales.formulario.invalido", idioma);
            return "redirect:/perfil/modifica";
        }
        try {
            usuarioService.modificar(usuario.getIdUsuario(), formulario);
            new SecurityContextLogoutHandler().logout(solicitud, respuesta, autenticacion);
            // Parámetro de estado: no se conserva información sensible en la sesión.
            return "redirect:/login?actualizado";
        } catch (CredencialesException ex) {
            mensaje(mensajes, "error", ex.getMessage(), idioma);
        } catch (DataIntegrityViolationException ex) {
            mensaje(mensajes, "error", "credenciales.correo.duplicado", idioma);
        }
        return "redirect:/perfil/modifica";
    }

    @PostMapping("/perfil/eliminar")
    public String eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            Authentication autenticacion, HttpServletRequest solicitud, HttpServletResponse respuesta,
            RedirectAttributes mensajes, Locale idioma) {
        try {
            usuarioService.eliminar(usuario.getIdUsuario());
            new SecurityContextLogoutHandler().logout(solicitud, respuesta, autenticacion);
            return "redirect:/login?eliminado";
        } catch (DataIntegrityViolationException ex) {
            mensaje(mensajes, "error", "credenciales.eliminar.asociaciones", idioma);
        } catch (CredencialesException ex) {
            mensaje(mensajes, "error", ex.getMessage(), idioma);
        }
        return "redirect:/perfil";
    }

    private void mensaje(RedirectAttributes mensajes, String tipo, String clave, Locale idioma) {
        mensajes.addFlashAttribute(tipo, messageSource.getMessage(clave, null, idioma));
    }
}
