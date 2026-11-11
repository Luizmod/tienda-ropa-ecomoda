package com.tienda.repository;

import com.tienda.domain.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {

    List<MetodoPago> findByUsuarioIdUsuario(Long idUsuario);

    List<MetodoPago> findByTipo(String tipo);

}
