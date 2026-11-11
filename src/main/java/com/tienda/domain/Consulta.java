
package com.tienda.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "consulta")
@DynamicUpdate
public class Consulta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Long idConsulta;

    private Long idUsuario;
    private String correo;
    private String asunto;
    private String mensaje;
    private String estado;
    private String respuesta;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    public Consulta() {
    }

    public Consulta(Long idUsuario, String correo, String asunto, String mensaje, String estado, String respuesta, LocalDate fechaCreacion) {
        this.idUsuario = idUsuario;
        this.correo = correo;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.estado = estado;
        this.respuesta = respuesta;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(Long idConsulta) {
        this.idConsulta = idConsulta;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
