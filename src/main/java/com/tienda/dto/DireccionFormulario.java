package com.tienda.dto;

import com.tienda.domain.DireccionEnvio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DireccionFormulario(
        @NotBlank @Size(max = 80) String provincia,
        @NotBlank @Size(max = 80) String canton,
        @NotBlank @Size(max = 80) String distrito,
        @NotBlank @Size(max = 500) String direccionExacta,
        @NotBlank @Pattern(regexp = "[+0-9][0-9 ()-]{6,19}") String telefono) {

    public DireccionFormulario(DireccionEnvio direccion) {
        this(direccion.getProvincia(), direccion.getCanton(), direccion.getDistrito(),
                direccion.getDireccionExacta(), direccion.getTelefono());
    }
}
