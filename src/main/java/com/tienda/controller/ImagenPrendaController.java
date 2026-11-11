package com.tienda.controller;

import com.tienda.service.ImagenLocalService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ImagenPrendaController {
    private final ImagenLocalService imagenes;

    @GetMapping("/img/prendas/{nombre}")
    public ResponseEntity<Resource> mostrar(@PathVariable String nombre) {
        Resource imagen = imagenes.leerImagen(nombre);
        MediaType tipo = switch (nombre.substring(nombre.lastIndexOf('.') + 1)) {
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.IMAGE_JPEG;
        };
        return ResponseEntity.ok().contentType(tipo).body(imagen);
    }
}
