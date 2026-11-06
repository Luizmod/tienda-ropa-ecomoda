package com.tienda.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilFormulario {
    @NotBlank(message = "{credenciales.correo.invalido}")
    @Email(message = "{credenciales.correo.invalido}")
    @Size(max = 254, message = "{credenciales.correo.invalido}")
    private String correo;

    @NotBlank(message = "{credenciales.actual.incorrecta}")
    @Size(max = 72, message = "{credenciales.actual.incorrecta}")
    private String contrasenaActual;

    @Size(max = 72, message = "{credenciales.contrasena.invalida}")
    private String nuevaContrasena = "";

    private String confirmacion = "";
}
