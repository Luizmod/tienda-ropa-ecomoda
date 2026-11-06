package com.tienda.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;

@Entity
@Table(name = "prenda")
@Getter
public class Prenda {
    @Id
    @Column(name = "id_prenda")
    private Long idPrenda;

    private String nombre;
    private String descripcion;
    private BigDecimal precio;

    @Column(name = "ruta_imagen")
    private String rutaImagen;
}
