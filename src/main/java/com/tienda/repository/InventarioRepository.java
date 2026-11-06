package com.tienda.repository;

import com.tienda.domain.Inventario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

public interface InventarioRepository extends Repository<Inventario, Long> {
    @EntityGraph(attributePaths = "prenda")
    List<Inventario> findAllByOrderByPrendaNombreAscTallaAsc();

    Optional<Inventario> findByPrendaIdPrendaAndTalla(Long idPrenda, String talla);
}
