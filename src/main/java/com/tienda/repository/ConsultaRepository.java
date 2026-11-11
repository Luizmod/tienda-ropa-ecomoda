
package com.tienda.repository;

import com.tienda.domain.Consulta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByIdUsuario(Long idUsuario);

    Optional<Consulta> findByIdConsultaAndIdUsuario(Long idConsulta, Long idUsuario);

    List<Consulta> findAllByOrderByIdConsultaDesc();
}
