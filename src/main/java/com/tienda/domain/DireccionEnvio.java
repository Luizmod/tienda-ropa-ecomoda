package com.tienda.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "direccion_envio")
@Getter
@Setter
@NoArgsConstructor
public class DireccionEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Long idDireccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 80)
    private String provincia;

    @Column(nullable = false, length = 80)
    private String canton;

    @Column(nullable = false, length = 80)
    private String distrito;

    @Column(name = "direccion_exacta", nullable = false, length = 500)
    private String direccionExacta;

    @Column(length = 20)
    private String telefono;
}
