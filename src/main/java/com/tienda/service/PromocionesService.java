
package com.tienda.service;

import com.tienda.domain.Promociones;
import com.tienda.repository.PromocionesRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromocionesService {

    private final PromocionesRepository promocionesRepository;

    @Transactional(readOnly = true)
    public List<Promociones> getPromociones() {
        return promocionesRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Promociones> listarVigentes() {
        LocalDate hoy = LocalDate.now();
        return promocionesRepository.findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(hoy, hoy);
    }

    @Transactional(readOnly = true)
    public Promociones getPromocion(Long idPromocion) {
        return promocionesRepository.findById(idPromocion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void save(Promociones formulario) {
        var promocion = formulario.getIdPromocion() == null ? new Promociones() : getPromocion(formulario.getIdPromocion());
        promocion.setNombre(formulario.getNombre().strip());
        promocion.setDescripcion(formulario.getDescripcion().strip());
        promocion.setPorcentaje(formulario.getPorcentaje());
        promocion.setFechaInicio(formulario.getFechaInicio());
        promocion.setFechaFin(formulario.getFechaFin());
        promocionesRepository.save(promocion);
    }

    @Transactional
    public void delete(Long idPromocion) {
        promocionesRepository.deleteById(idPromocion);
    }
}
