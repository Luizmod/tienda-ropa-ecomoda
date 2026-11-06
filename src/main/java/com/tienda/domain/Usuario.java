package com.tienda.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false, unique = true, length = 254)
    private String correo;

    // Solo se almacena el hash; la entidad nunca se envía a una vista.
    @Column(nullable = false, length = 60)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 7)
    private Rol rol;
}
