package com.tienda.repository;

import com.tienda.domain.Prenda;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PrendaRepository extends JpaRepository<Prenda, Long> {
    @EntityGraph(attributePaths = "categoria")
    List<Prenda> findAllByOrderByNombreAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Prenda p where p.idPrenda = :id")
    Optional<Prenda> bloquearPorId(Long id);
}
