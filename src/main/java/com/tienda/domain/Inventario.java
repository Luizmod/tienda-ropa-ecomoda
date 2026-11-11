package com.tienda.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "inventario")
@Getter
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Long idInventario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prenda")
    private Prenda prenda;

    private String talla;
    private int cantidad;

    public Inventario() {
    }

    public Inventario(Prenda prenda, String talla) {
        this.prenda = prenda;
        this.talla = talla;
    }

    public void actualizarCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
