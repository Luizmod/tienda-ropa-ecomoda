package com.tienda.service;

import com.tienda.repository.ConsultaRepository;
import com.tienda.domain.Consulta;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Transactional(readOnly = true)
    public List<Consulta> getConsultas() {
        return consultaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Consulta getConsulta(Long idConsulta) {
        return consultaRepository.findById(idConsulta).orElse(null);
    }

    @Transactional
    public void save(Consulta consulta) {
        if (consulta.getFechaCreacion() == null) {
            consulta.setFechaCreacion(LocalDate.now());
        }
        if (consulta.getIdUsuario() == null) {
            consulta.setIdUsuario(1L); 
        }
        if (consulta.getEstado() == null || consulta.getEstado().isEmpty()) {
            consulta.setEstado("PENDIENTE");
        }
        consultaRepository.save(consulta);
    }

    @Transactional
    public void delete(Consulta consulta) {
        consultaRepository.delete(consulta);
    }
}