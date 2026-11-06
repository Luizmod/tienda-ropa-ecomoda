package com.tienda.repository;

import com.tienda.domain.LineaCarrito;
import com.tienda.dto.LineaCarritoVista;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LineaCarritoRepository extends JpaRepository<LineaCarrito, Long> {
    @Query("""
            select new com.tienda.dto.LineaCarritoVista(l.idLinea, p.idPrenda, p.nombre, p.descripcion,
                    p.rutaImagen, i.talla, p.precio, l.cantidad, i.cantidad)
            from LineaCarrito l
            join Inventario i on i.prenda.idPrenda = l.idPrenda and i.talla = l.talla
            join i.prenda p
            where l.idUsuario = :idUsuario
            order by l.idLinea
            """)
    List<LineaCarritoVista> listarPorUsuario(@Param("idUsuario") Long idUsuario);

    Optional<LineaCarrito> findByIdLineaAndIdUsuario(Long idLinea, Long idUsuario);
    Optional<LineaCarrito> findByIdUsuarioAndIdPrendaAndTalla(Long idUsuario, Long idPrenda, String talla);
}
