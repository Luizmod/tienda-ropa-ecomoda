package com.tienda.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "inventario")
@Getter
public class Inventario {
    @Id
    @Column(name = "id_inventario")
    private Long idInventario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prenda")
    private Prenda prenda;

    private String talla;
    private int cantidad;
}
