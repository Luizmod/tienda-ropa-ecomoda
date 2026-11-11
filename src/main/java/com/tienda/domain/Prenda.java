package com.tienda.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;

@Entity
@Table(name = "prenda")
@Getter
public class Prenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prenda")
    private Long idPrenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    private String nombre;
    private String descripcion;
    private BigDecimal precio;

    @Column(name = "ruta_imagen")
    private String rutaImagen;

    public void actualizar(Categoria categoria, String nombre, String descripcion, BigDecimal precio,
            String rutaImagen) {
        this.categoria = categoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.rutaImagen = rutaImagen;
    }
}
