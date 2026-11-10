package com.tienda.repository;

import com.tienda.domain.Prenda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrendaRepository extends JpaRepository<Prenda, Long> {
}
