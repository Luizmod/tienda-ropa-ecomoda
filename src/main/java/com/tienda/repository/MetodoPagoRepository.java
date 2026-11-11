package com.tienda.repository;

import com.tienda.domain.MetodoPago;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {
    List<MetodoPago> findByUsuarioIdUsuarioOrderByIdMetodoPagoAsc(Long idUsuario);
    Optional<MetodoPago> findByIdMetodoPagoAndUsuarioIdUsuario(Long idMetodoPago, Long idUsuario);
}
