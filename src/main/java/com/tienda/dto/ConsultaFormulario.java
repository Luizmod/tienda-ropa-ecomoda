package com.tienda.dto;

import com.tienda.domain.Consulta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsultaFormulario(Long idConsulta,
        @NotBlank @Email @Size(max = 254) String correo,
        @NotBlank @Size(max = 150) String asunto,
        @NotBlank @Size(max = 8000) String mensaje) {
    public ConsultaFormulario(Consulta consulta) {
        this(consulta.getIdConsulta(), consulta.getCorreo(), consulta.getAsunto(), consulta.getMensaje());
    }
}
