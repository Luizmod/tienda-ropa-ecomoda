package com.tienda.service;

import com.tienda.repository.ConsultaRepository;
import com.tienda.domain.Consulta;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;


    @Transactional(readOnly = true)
    public List<Consulta> getConsultas(Long idUsuario) {
        return consultaRepository.findByIdUsuario(idUsuario);
    }

    @Transactional(readOnly = true)
    public Consulta getConsulta(Long idUsuario, Long idConsulta) {
        return consultaRepository.findByIdConsultaAndIdUsuario(idConsulta, idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void save(Long idUsuario, Long idConsulta, String correo, String asunto, String mensaje) {
        Consulta consulta = idConsulta == null ? new Consulta() : getConsulta(idUsuario, idConsulta);
        if (idConsulta == null) {
            consulta.setIdUsuario(idUsuario);
            consulta.setFechaCreacion(LocalDate.now());
            consulta.setEstado("PENDIENTE");
        }
        consulta.setCorreo(correo);
        consulta.setAsunto(asunto);
        consulta.setMensaje(mensaje);
        consultaRepository.save(consulta);
    }

    @Transactional
    public void delete(Long idUsuario, Long idConsulta) {
        consultaRepository.delete(getConsulta(idUsuario, idConsulta));
    }

    @Transactional(readOnly = true)
    public List<Consulta> listarParaAdministracion() {
        return consultaRepository.findAllByOrderByIdConsultaDesc();
    }

    @Transactional(readOnly = true)
    public Consulta consultarParaAdministracion(Long idConsulta) {
        return consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void responder(Long idConsulta, String respuesta) {
        var consulta = consultarParaAdministracion(idConsulta);
        consulta.setRespuesta(respuesta);
        consulta.setEstado("RESPONDIDA");
    }
}
