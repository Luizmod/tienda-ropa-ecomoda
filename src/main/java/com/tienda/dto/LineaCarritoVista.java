package com.tienda.dto;

import java.math.BigDecimal;

public record LineaCarritoVista(Long idLinea, Long idPrenda, String nombre, String descripcion,
        String rutaImagen, String talla, BigDecimal precio, int cantidad, int disponibles) {
    public BigDecimal getSubtotal() {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }
}
