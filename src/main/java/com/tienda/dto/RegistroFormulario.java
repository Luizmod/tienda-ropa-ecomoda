package com.tienda.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroFormulario {
    @NotBlank(message = "{credenciales.correo.invalido}")
    @Email(message = "{credenciales.correo.invalido}")
    @Size(max = 254, message = "{credenciales.correo.invalido}")
    private String correo;

    @NotBlank(message = "{credenciales.contrasena.invalida}")
    @Size(min = 8, max = 72, message = "{credenciales.contrasena.invalida}")
    private String contrasena;

    @NotBlank(message = "{credenciales.confirmacion.invalida}")
    private String confirmacion;
}
