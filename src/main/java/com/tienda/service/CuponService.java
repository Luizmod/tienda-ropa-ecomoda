package com.tienda.service;

import com.tienda.domain.Cupon;
import com.tienda.dto.CuponFormulario;
import com.tienda.repository.CuponRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

    public List<Cupon> listarVigentes() {
        return repositorio.findByIdUsuarioIsNullAndActivoTrueAndFechaVencimientoGreaterThanEqualOrderByCodigoAsc(LocalDate.now());
    }

    public List<Cupon> listarAsignados(Long idUsuario) {
        return repositorio.findByIdUsuarioAndActivoTrueAndFechaVencimientoGreaterThanEqualOrderByCodigoAsc(
                idUsuario, LocalDate.now());
    }

    public Optional<Cupon> buscarAplicable(String codigo, Long idUsuario) {
        String normalizado = codigo.strip().toUpperCase(Locale.ROOT);
        if (normalizado.isEmpty() || normalizado.length() > 40) {
            return Optional.empty();
        }
        return repositorio.findByCodigoAndActivoTrueAndFechaVencimientoGreaterThanEqual(normalizado, LocalDate.now())
                .filter(cupon -> cupon.getIdUsuario() == null || cupon.getIdUsuario().equals(idUsuario));
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
