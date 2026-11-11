package com.tienda.repository;

import com.tienda.domain.DireccionEnvio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DireccionEnvioRepository extends JpaRepository<DireccionEnvio, Long> {
    List<DireccionEnvio> findByUsuarioIdUsuarioOrderByIdDireccionAsc(Long idUsuario);
    Optional<DireccionEnvio> findByIdDireccionAndUsuarioIdUsuario(Long idDireccion, Long idUsuario);
}
