package com.tienda.service;

import com.tienda.domain.Inventario;
import com.tienda.domain.LineaCarrito;
import com.tienda.dto.LineaCarritoVista;
import com.tienda.repository.InventarioRepository;
import com.tienda.repository.LineaCarritoRepository;
import com.tienda.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarritoService {
    private final LineaCarritoRepository lineas;
    private final InventarioRepository inventario;
    private final UsuarioRepository usuarios;

    public List<Inventario> catalogo() {
        return inventario.findAllByOrderByPrendaNombreAscTallaAsc();
    }

    public List<LineaCarritoVista> listar(Long idUsuario) {
        return lineas.listarPorUsuario(idUsuario);
    }

    public LineaCarritoVista consultar(Long idUsuario, Long idLinea) {
        LineaCarrito linea = buscarLinea(idUsuario, idLinea);
        Inventario variante = buscarVariante(linea.getIdPrenda(), linea.getTalla());
        var prenda = variante.getPrenda();
        return new LineaCarritoVista(linea.getIdLinea(), prenda.getIdPrenda(), prenda.getNombre(),
                prenda.getDescripcion(), prenda.getRutaImagen(), variante.getTalla(), prenda.getPrecio(),
                linea.getCantidad(), variante.getCantidad());
    }

    @Transactional
    public void agregar(Long idUsuario, Long idPrenda, String talla) {
        bloquearUsuario(idUsuario);
        Inventario variante = buscarVariante(idPrenda, talla);
        String tallaRegistrada = variante.getTalla();
        LineaCarrito linea = lineas.findByIdUsuarioAndIdPrendaAndTalla(idUsuario, idPrenda, tallaRegistrada)
                .orElseGet(() -> new LineaCarrito(idUsuario, idPrenda, tallaRegistrada));
        long cantidad = (long) linea.getCantidad() + 1;
        validarExistencias(cantidad, variante);
        linea.cambiarCantidad((int) cantidad);
        lineas.save(linea);
    }

    @Transactional
    public void modificar(Long idUsuario, Long idLinea, int cantidad) {
        if (cantidad < 1) {
            throw new CarritoException("carrito.cantidad.invalida");
        }
        bloquearUsuario(idUsuario);
        LineaCarrito linea = buscarLinea(idUsuario, idLinea);
        Inventario variante = buscarVariante(linea.getIdPrenda(), linea.getTalla());
        validarExistencias(cantidad, variante);
        linea.cambiarCantidad(cantidad);
    }

    @Transactional
    public void eliminar(Long idUsuario, Long idLinea) {
        bloquearUsuario(idUsuario);
        lineas.delete(buscarLinea(idUsuario, idLinea));
    }

    private LineaCarrito buscarLinea(Long idUsuario, Long idLinea) {
        return lineas.findByIdLineaAndIdUsuario(idLinea, idUsuario)
                .orElseThrow(() -> new CarritoException("carrito.linea.inexistente"));
    }

    private Inventario buscarVariante(Long idPrenda, String talla) {
        return inventario.findByPrendaIdPrendaAndTalla(idPrenda, talla)
                .orElseThrow(() -> new CarritoException("carrito.variante.inexistente"));
    }

    private void validarExistencias(long cantidad, Inventario variante) {
        if (cantidad > variante.getCantidad()) {
            throw new CarritoException("carrito.stock.insuficiente");
        }
    }

    private void bloquearUsuario(Long idUsuario) {
        // Serializa las modificaciones del carrito de un mismo cliente.
        usuarios.bloquearPorId(idUsuario)
                .orElseThrow(() -> new CarritoException("carrito.usuario.inexistente"));
    }

    public static class CarritoException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CarritoException(String clave) {
            super(clave);
        }
    }
}
