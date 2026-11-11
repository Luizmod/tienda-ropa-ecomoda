package com.tienda.repository;

import com.tienda.domain.Cupon;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuponRepository extends JpaRepository<Cupon, Long> {
    List<Cupon> findAllByOrderByCodigoAsc();

    List<Cupon> findByIdUsuarioIsNullAndActivoTrueAndFechaVencimientoGreaterThanEqualOrderByCodigoAsc(LocalDate hoy);

    List<Cupon> findByIdUsuarioAndActivoTrueAndFechaVencimientoGreaterThanEqualOrderByCodigoAsc(
            Long idUsuario, LocalDate hoy);

    Optional<Cupon> findByCodigoAndActivoTrueAndFechaVencimientoGreaterThanEqual(String codigo, LocalDate hoy);
}
