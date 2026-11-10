package com.tienda.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "carrito_linea")
@Getter
public class LineaCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_linea")
    private Long idLinea;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_prenda", nullable = false)
    private Long idPrenda;

    @Column(nullable = false, length = 20)
    private String talla;

    @Column(nullable = false)
    private int cantidad;

    protected LineaCarrito() {
    }

    public LineaCarrito(Long idUsuario, Long idPrenda, String talla) {
        this.idUsuario = idUsuario;
        this.idPrenda = idPrenda;
        this.talla = talla;
    }

    public void cambiarCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
