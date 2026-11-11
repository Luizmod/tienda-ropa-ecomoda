package com.tienda.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;

@Entity
@Table(name = "cupon")
@Getter
public class Cupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cupon")
    private Long idCupon;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "id_usuario")
    private Long idUsuario;

    public Cupon() {
    }

    public void actualizar(String codigo, BigDecimal porcentaje, LocalDate fechaVencimiento, boolean activo) {
        this.codigo = codigo;
        this.porcentaje = porcentaje;
        this.fechaVencimiento = fechaVencimiento;
        this.activo = activo;
    }
}
