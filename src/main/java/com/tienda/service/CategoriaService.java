
package com.tienda.service;

import com.tienda.domain.Categoria;
import com.tienda.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<Categoria> getCategorias() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria getCategoria(Categoria categoria) {
        if (categoria != null && categoria.getIdCategoria() != null) {
            return categoriaRepository.findById(categoria.getIdCategoria()).orElse(null);
        }
        return null;
    }

    @Transactional
    public void save(Categoria categoria) {
        categoriaRepository.save(categoria);
    }

    @Transactional
    public void delete(Categoria categoria) {
        if (categoria != null && categoria.getIdCategoria() != null) {
            categoriaRepository.delete(categoria);
        }
    }
}