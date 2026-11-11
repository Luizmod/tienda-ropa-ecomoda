package com.tienda.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "direccion_envio")
@Getter
@NoArgsConstructor
public class DireccionEnvio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Long idDireccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    private String provincia;
    private String canton;
    private String distrito;
    private String direccionExacta;
    private String telefono;

    public DireccionEnvio(Usuario usuario) {
        this.usuario = usuario;
    }

    public void actualizar(String provincia, String canton, String distrito, String direccionExacta, String telefono) {
        this.provincia = provincia;
        this.canton = canton;
        this.distrito = distrito;
        this.direccionExacta = direccionExacta;
        this.telefono = telefono;
    }
}
