package com.tienda.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "metodo_pago")
@Getter
@NoArgsConstructor
public class MetodoPago {
    public enum Tipo {
        TARJETA, SINPE_MOVIL, CONTRA_ENTREGA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_pago")
    private Long idMetodoPago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;
    private String titular;
    private String ultimosCuatro;
    private String telefonoSinpe;

    public MetodoPago(Usuario usuario) {
        this.usuario = usuario;
    }

    public void actualizar(Tipo tipo, String titular, String ultimosCuatro, String telefonoSinpe) {
        this.tipo = tipo;
        this.titular = tipo == Tipo.TARJETA ? titular : null;
        this.ultimosCuatro = tipo == Tipo.TARJETA ? ultimosCuatro : null;
        this.telefonoSinpe = tipo == Tipo.SINPE_MOVIL ? telefonoSinpe : null;
    }
}
