package com.tienda.service;

import com.tienda.domain.Inventario;
import com.tienda.domain.LineaCarrito;
import com.tienda.dto.LineaCarritoVista;
import com.tienda.repository.InventarioRepository;
import com.tienda.repository.LineaCarritoRepository;
import com.tienda.repository.UsuarioRepository;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return vista(linea.getIdLinea(), variante, linea.getCantidad());
    }

    private LineaCarritoVista vista(Long idLinea, Inventario variante, int cantidad) {
        var prenda = variante.getPrenda();
        return new LineaCarritoVista(idLinea, prenda.getIdPrenda(), prenda.getNombre(),
                prenda.getDescripcion(), prenda.getRutaImagen(), variante.getTalla(), prenda.getPrecio(),
                cantidad, variante.getCantidad());
    }

    @Transactional
    public void agregar(Long idUsuario, Long idPrenda, String talla) {
        bloquearUsuario(idUsuario);
        Inventario variante = buscarVariante(idPrenda, talla);
        LineaCarrito linea = buscarOCrearLinea(idUsuario, variante);
        long cantidad = (long) linea.getCantidad() + 1;
        validarExistencias(cantidad, variante);
        linea.cambiarCantidad((int) cantidad);
        lineas.save(linea);
    }

    @Transactional
    public void modificar(Long idUsuario, Long idLinea, int cantidad) {
        validarCantidad(cantidad);
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

    public List<LineaCarritoVista> listarInvitado(CarritoInvitado carrito) {
        return variantesInvitado(carrito).stream().map(variante -> vista(-variante.getIdInventario(), variante,
                carrito.cantidades.get(variante.getIdInventario()))).toList();
    }

    public LineaCarritoVista consultarInvitado(CarritoInvitado carrito, Long idLinea) {
        int cantidad = cantidadInvitado(carrito, idLinea);
        return vista(idLinea, buscarVarianteInvitado(-idLinea), cantidad);
    }

    public void agregarInvitado(CarritoInvitado carrito, Long idPrenda, String talla) {
        Inventario variante = buscarVariante(idPrenda, talla);
        Long idInventario = variante.getIdInventario();
        long cantidad = (long) carrito.cantidades.getOrDefault(idInventario, 0) + 1;
        validarExistencias(cantidad, variante);
        carrito.cantidades.put(idInventario, (int) cantidad);
    }

    public void modificarInvitado(CarritoInvitado carrito, Long idLinea, int cantidad) {
        validarCantidad(cantidad);
        cantidadInvitado(carrito, idLinea);
        validarExistencias(cantidad, buscarVarianteInvitado(-idLinea));
        carrito.cantidades.put(-idLinea, cantidad);
    }

    public void eliminarInvitado(CarritoInvitado carrito, Long idLinea) {
        cantidadInvitado(carrito, idLinea);
        carrito.cantidades.remove(-idLinea);
    }

    @Transactional
    public boolean incorporarInvitado(Long idUsuario, CarritoInvitado carrito) {
        if (carrito.cantidades.isEmpty()) {
            return true;
        }
        bloquearUsuario(idUsuario);
        var variantes = variantesInvitado(carrito);
        boolean completo = variantes.size() == carrito.cantidades.size();
        for (Inventario variante : variantes) {
            LineaCarrito linea = buscarOCrearLinea(idUsuario, variante);
            int solicitadas = carrito.cantidades.get(variante.getIdInventario());
            int agregadas = Math.min(solicitadas, Math.max(0, variante.getCantidad() - linea.getCantidad()));
            if (agregadas > 0) {
                linea.cambiarCantidad(linea.getCantidad() + agregadas);
                lineas.save(linea);
            }
            completo &= agregadas == solicitadas;
        }
        return completo;
    }

    private List<Inventario> variantesInvitado(CarritoInvitado carrito) {
        return carrito.cantidades.isEmpty() ? List.of()
                : inventario.findByIdInventarioInOrderByIdInventarioAsc(carrito.cantidades.keySet());
    }

    private int cantidadInvitado(CarritoInvitado carrito, Long idLinea) {
        Integer cantidad = carrito.cantidades.get(-idLinea);
        if (cantidad == null) {
            throw new CarritoException("carrito.linea.inexistente");
        }
        return cantidad;
    }

    private Inventario buscarVarianteInvitado(Long idInventario) {
        return inventario.findByIdInventario(idInventario)
                .orElseThrow(() -> new CarritoException("carrito.variante.inexistente"));
    }

    private LineaCarrito buscarOCrearLinea(Long idUsuario, Inventario variante) {
        Long idPrenda = variante.getPrenda().getIdPrenda();
        String talla = variante.getTalla();
        return lineas.findByIdUsuarioAndIdPrendaAndTalla(idUsuario, idPrenda, talla)
                .orElseGet(() -> new LineaCarrito(idUsuario, idPrenda, talla));
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

    private void validarCantidad(int cantidad) {
        if (cantidad < 1) {
            throw new CarritoException("carrito.cantidad.invalida");
        }
    }

    private void bloquearUsuario(Long idUsuario) {
        // Serializa las modificaciones del carrito de un mismo cliente.
        usuarios.bloquearPorId(idUsuario)
                .orElseThrow(() -> new CarritoException("carrito.usuario.inexistente"));
    }

    public static final class CarritoInvitado implements Serializable {
        private static final long serialVersionUID = 1L;
        // Solo se conservan identificadores de inventario y cantidades en la sesión.
        private final Map<Long, Integer> cantidades = new HashMap<>();
    }

    public static class CarritoException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CarritoException(String clave) {
            super(clave);
        }
    }
}
