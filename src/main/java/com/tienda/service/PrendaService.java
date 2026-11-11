package com.tienda.service;

import com.tienda.domain.Inventario;
import com.tienda.domain.Prenda;
import com.tienda.dto.PrendaFormulario;
import com.tienda.repository.CategoriaRepository;
import com.tienda.repository.InventarioRepository;
import com.tienda.repository.PrendaRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrendaService {
    private final PrendaRepository prendas;
    private final CategoriaRepository categorias;
    private final InventarioRepository inventario;
    private final ImagenLocalService imagenes;

    public List<Prenda> listar() {
        return prendas.findAllByOrderByNombreAsc();
    }

    public PrendaFormulario formulario(Long id) {
        return prendas.findById(id).map(PrendaFormulario::new).orElseThrow(PrendaService::noEncontrada);
    }

    public List<Inventario> variantes(Long id) {
        return inventario.findByPrendaIdPrendaOrderByTallaAsc(id);
    }

    public String imagenActual(Long id) {
        return prendas.findById(id).orElseThrow(PrendaService::noEncontrada).getRutaImagen();
    }

    @Transactional
    public void guardar(Long id, PrendaFormulario formulario, MultipartFile archivo) {
        var prenda = id == null ? new Prenda() : prendas.bloquearPorId(id).orElseThrow(PrendaService::noEncontrada);
        var categoria = formulario.idCategoria() == null ? null
                : categorias.findById(formulario.idCategoria()).orElseThrow(PrendaService::noEncontrada);
        String imagen = prenda.getRutaImagen();
        if (archivo != null && (!archivo.isEmpty() || StringUtils.hasText(archivo.getOriginalFilename()))) {
            String nueva = imagenes.subirImagen(archivo);
            retirarAlConfirmar(imagen, nueva);
            imagen = nueva;
        }
        prenda.actualizar(categoria, formulario.nombre().strip(), formulario.descripcion().strip(),
                formulario.precio(), imagen);
        prendas.save(prenda);
        if (formulario.cantidad() != null) {
            String talla = formulario.talla().strip().toUpperCase(Locale.ROOT);
            var variante = inventario.findByPrendaIdPrendaAndTalla(prenda.getIdPrenda(), talla)
                    .orElseGet(() -> new Inventario(prenda, talla));
            variante.actualizarCantidad(formulario.cantidad());
            inventario.save(variante);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        var prenda = prendas.bloquearPorId(id).orElseThrow(PrendaService::noEncontrada);
        inventario.deleteAll(variantes(id));
        inventario.flush();
        prendas.delete(prenda);
        if (prenda.getRutaImagen() != null) {
            retirarAlConfirmar(prenda.getRutaImagen(), null);
        }
    }

    private void retirarAlConfirmar(String anterior, String nueva) {
        // La imagen anterior sigue disponible mientras el cambio en MySQL no se confirme.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
                if (estado == STATUS_COMMITTED) {
                    imagenes.eliminarImagen(anterior);
                } else if (estado == STATUS_ROLLED_BACK) {
                    imagenes.eliminarImagen(nueva);
                }
            }
        });
    }

    private static ResponseStatusException noEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
