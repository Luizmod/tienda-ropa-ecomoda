package com.tienda.service;

import com.tienda.domain.Categoria;
import com.tienda.repository.CategoriaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaService {
    private final CategoriaRepository categorias;

    public List<Categoria> getCategorias() {
        return categorias.findAll();
    }

    public Categoria consultar(Long id) {
        return categorias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void save(Categoria formulario) {
        Categoria categoria = formulario.getIdCategoria() == null ? new Categoria() : consultar(formulario.getIdCategoria());
        categoria.setNombre(formulario.getNombre().strip());
        categoria.setDescripcion(formulario.getDescripcion().strip());
        categorias.save(categoria);
    }

    @Transactional
    public void delete(Long id) {
        categorias.delete(consultar(id));
    }
}
