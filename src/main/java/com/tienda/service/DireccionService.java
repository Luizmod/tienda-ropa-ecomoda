package com.tienda.service;

import com.tienda.domain.DireccionEnvio;
import com.tienda.dto.DireccionFormulario;
import com.tienda.repository.DireccionEnvioRepository;
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
public class DireccionService {
    private final DireccionEnvioRepository repositorio;
    private final UsuarioRepository usuarios;

    public List<DireccionEnvio> listar(Long idUsuario) {
        return repositorio.findByUsuarioIdUsuarioOrderByIdDireccionAsc(idUsuario);
    }

    public DireccionEnvio consultar(Long idUsuario, Long id) {
        return repositorio.findByIdDireccionAndUsuarioIdUsuario(id, idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void guardar(Long idUsuario, Long id, DireccionFormulario formulario) {
        var usuario = usuarios.bloquearPorId(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var registro = id == null ? new DireccionEnvio(usuario) : consultar(idUsuario, id);
        registro.actualizar(formulario.provincia().strip(), formulario.canton().strip(),
                formulario.distrito().strip(), formulario.direccionExacta().strip(), formulario.telefono().strip());
        repositorio.save(registro);
    }

    @Transactional
    public void eliminar(Long idUsuario, Long id) {
        usuarios.bloquearPorId(idUsuario).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        repositorio.delete(consultar(idUsuario, id));
    }
}
