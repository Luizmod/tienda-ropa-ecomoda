
package com.tienda.service;

import com.tienda.domain.Promociones;
import com.tienda.repository.PromocionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PromocionesService {

    @Autowired
    private PromocionesRepository promocionesRepository;

    @Transactional(readOnly = true)
    public List<Promociones> getPromociones() {
        return promocionesRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Promociones getPromocion(Long idPromocion) {
        return promocionesRepository.findById(idPromocion).orElse(null);
    }

    @Transactional
    public void save(Promociones promociones) {
        promocionesRepository.save(promociones);
    }

    @Transactional
    public void delete(Promociones promociones) {
        promocionesRepository.delete(promociones);
    }
}