package com.tienda.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class ImagenLocalService {
    private static final String RUTA_IMAGEN = "/img/prendas/";
    private static final Pattern NOMBRE = Pattern.compile("[0-9a-f-]{36}\\.(jpg|png|webp)");
    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private static final int MAX_DIMENSION = 4096;
    private final Path carpeta;

    public ImagenLocalService(@Value("${prendas.imagenes.directorio}") String directorio) {
        this.carpeta = Path.of(directorio).toAbsolutePath().normalize();
    }

    public String subirImagen(MultipartFile archivo) {
        if (archivo.isEmpty() || archivo.getSize() > MAX_BYTES) {
            throw new ImagenException("prenda.imagen.invalida");
        }
        byte[] contenido;
        String extension;
        try {
            contenido = archivo.getBytes();
            extension = detectarExtension(contenido);
        } catch (IOException ex) {
            throw new ImagenException("prenda.imagen.invalida");
        }

        String nombre = UUID.randomUUID() + "." + extension;
        Path destino = carpeta.resolve(nombre);
        try {
            Files.createDirectories(carpeta);
            var salida = Files.newOutputStream(destino, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            // La ruta se publica en MySQL solo después de escribir el archivo completo.
            try (salida) {
                salida.write(contenido);
            } catch (IOException ex) {
                eliminarImagen(RUTA_IMAGEN + nombre);
                throw ex;
            }
        } catch (IOException ex) {
            log.warn("No se pudo guardar una imagen de prenda en el directorio local.", ex);
            throw new ImagenException("prenda.imagen.error");
        }
        return RUTA_IMAGEN + nombre;
    }

    public Resource leerImagen(String nombre) {
        if (!NOMBRE.matcher(nombre).matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Path archivo = carpeta.resolve(nombre);
        if (!Files.isRegularFile(archivo, LinkOption.NOFOLLOW_LINKS)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return new FileSystemResource(archivo);
    }

    public void eliminarImagen(String ruta) {
        if (ruta == null || !ruta.startsWith(RUTA_IMAGEN)) {
            return;
        }
        String nombre = ruta.substring(RUTA_IMAGEN.length());
        if (!NOMBRE.matcher(nombre).matches()) {
            return;
        }
        try {
            Files.deleteIfExists(carpeta.resolve(nombre));
        } catch (IOException ex) {
            log.warn("No se pudo retirar una imagen de prenda del directorio local.", ex);
        }
    }

    private String detectarExtension(byte[] contenido) throws IOException {
        try (var entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(contenido))) {
            var lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) {
                throw new ImagenException("prenda.imagen.invalida");
            }
            var lector = lectores.next();
            try {
                String extension = switch (lector.getFormatName().toLowerCase(Locale.ROOT)) {
                    case "jpeg" -> "jpg";
                    case "png" -> "png";
                    case "webp" -> "webp";
                    default -> throw new ImagenException("prenda.imagen.invalida");
                };
                lector.setInput(entrada, true, true);
                if (lector.getWidth(0) > MAX_DIMENSION || lector.getHeight(0) > MAX_DIMENSION) {
                    throw new ImagenException("prenda.imagen.invalida");
                }
                lector.read(0);
                return extension;
            } finally {
                lector.dispose();
            }
        }
    }

    public static class ImagenException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ImagenException(String clave) {
            super(clave);
        }
    }
}
