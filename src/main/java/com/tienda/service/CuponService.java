package com.tienda.service;

import com.tienda.domain.Cupon;
import com.tienda.dto.CuponFormulario;
import com.tienda.repository.CuponRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CuponService {
    private final CuponRepository repositorio;

    public List<Cupon> listar() {
        return repositorio.findAllByOrderByCodigoAsc();
    }

    public Cupon consultar(Long idCupon) {
        return repositorio.findById(idCupon).orElseThrow(CuponNoEncontradoException::new);
    }

    @Transactional
    public void guardar(Long idCupon, CuponFormulario formulario) {
        Cupon cupon = idCupon == null ? new Cupon() : consultar(idCupon);
        cupon.actualizar(formulario.codigo().toUpperCase(Locale.ROOT), formulario.porcentaje(),
                formulario.fechaVencimiento(), formulario.activo());
        repositorio.save(cupon);
    }

    @Transactional
    public void eliminar(Long idCupon) {
        repositorio.delete(consultar(idCupon));
    }

    public static class CuponNoEncontradoException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
