
package com.tienda.repository;

import com.tienda.domain.Promociones;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromocionesRepository extends JpaRepository<Promociones, Long> {
    List<Promociones> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(LocalDate inicio, LocalDate fin);
}
