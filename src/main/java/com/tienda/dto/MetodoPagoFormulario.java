package com.tienda.dto;

import com.tienda.domain.MetodoPago;
import com.tienda.domain.MetodoPago.Tipo;
import jakarta.validation.constraints.NotNull;

public record MetodoPagoFormulario(@NotNull Tipo tipo, String titular, String ultimosCuatro, String telefonoSinpe) {
    public MetodoPagoFormulario(MetodoPago metodo) {
        this(metodo.getTipo(), metodo.getTitular(), metodo.getUltimosCuatro(), metodo.getTelefonoSinpe());
    }

    public boolean datosValidos() {
        if (tipo == null) {
            return false;
        }
        return switch (tipo) {
            case TARJETA -> titular != null && !titular.isBlank() && titular.length() <= 120
                    && ultimosCuatro != null && ultimosCuatro.matches("[0-9]{4}");
            case SINPE_MOVIL -> telefonoSinpe != null && telefonoSinpe.matches("[+0-9][0-9 ()-]{6,19}");
            case CONTRA_ENTREGA -> true;
        };
    }
}
