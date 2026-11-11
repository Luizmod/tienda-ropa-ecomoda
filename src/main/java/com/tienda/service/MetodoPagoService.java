package com.tienda.service;

import com.tienda.domain.MetodoPago;
import com.tienda.dto.MetodoPagoFormulario;
import com.tienda.repository.MetodoPagoRepository;
import com.tienda.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetodoPagoService {
    private final MetodoPagoRepository repositorio;
    private final UsuarioRepository usuarios;

    public List<MetodoPago> listar(Long idUsuario) {
        return repositorio.findByUsuarioIdUsuarioOrderByIdMetodoPagoAsc(idUsuario);
    }

    public MetodoPago consultar(Long idUsuario, Long id) {
        return repositorio.findByIdMetodoPagoAndUsuarioIdUsuario(id, idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void guardar(Long idUsuario, Long id, MetodoPagoFormulario formulario) {
        var usuario = usuarios.bloquearPorId(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var registro = id == null ? new MetodoPago(usuario) : consultar(idUsuario, id);
        registro.actualizar(formulario.tipo(), formulario.titular(), formulario.ultimosCuatro(),
                formulario.telefonoSinpe());
        repositorio.save(registro);
    }

    @Transactional
    public void eliminar(Long idUsuario, Long id) {
        usuarios.bloquearPorId(idUsuario).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        repositorio.delete(consultar(idUsuario, id));
    }
}
