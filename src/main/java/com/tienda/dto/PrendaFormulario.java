package com.tienda.dto;

import com.tienda.domain.Prenda;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PrendaFormulario(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 500) String descripcion,
        @NotNull @Digits(integer = 10, fraction = 2) @DecimalMin("0") BigDecimal precio,
        Long idCategoria,
        @Size(max = 20) String talla,
        @PositiveOrZero Integer cantidad) {

    public PrendaFormulario(Prenda prenda) {
        this(prenda.getNombre(), prenda.getDescripcion(), prenda.getPrecio(),
                prenda.getCategoria() == null ? null : prenda.getCategoria().getIdCategoria(),
                null, null);
    }

    public boolean varianteValida() {
        boolean tieneTalla = talla != null && !talla.isBlank();
        return tieneTalla == (cantidad != null);
    }
}
