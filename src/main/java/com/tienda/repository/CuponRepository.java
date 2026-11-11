package com.tienda.repository;

import com.tienda.domain.Cupon;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuponRepository extends JpaRepository<Cupon, Long> {
    List<Cupon> findAllByOrderByCodigoAsc();

    List<Cupon> findByActivoTrueAndFechaVencimientoGreaterThanEqualOrderByCodigoAsc(LocalDate hoy);
}
