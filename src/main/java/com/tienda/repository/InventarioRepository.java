package com.tienda.repository;

import com.tienda.domain.Inventario;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    @EntityGraph(attributePaths = "prenda")
    List<Inventario> findAllByOrderByPrendaNombreAscTallaAsc();

    Optional<Inventario> findByPrendaIdPrendaAndTalla(Long idPrenda, String talla);

    @EntityGraph(attributePaths = "prenda")
    List<Inventario> findByIdInventarioInOrderByIdInventarioAsc(Collection<Long> ids);

    @EntityGraph(attributePaths = "prenda")
    Optional<Inventario> findByIdInventario(Long idInventario);

    List<Inventario> findByPrendaIdPrendaOrderByTallaAsc(Long idPrenda);
}
