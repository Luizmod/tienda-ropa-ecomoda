package com.tienda.controller;

import com.tienda.SecurityConfig;
import com.tienda.service.CategoriaService;
import com.tienda.service.ImagenLocalService;
import com.tienda.service.ImagenLocalService.ImagenException;
import com.tienda.service.PrendaService;
import com.tienda.service.UsuarioDetailsService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({PrendaController.class, ImagenPrendaController.class})
@Import({SecurityConfig.class, ImagenLocalService.class})
class PrendaImagenTest {
    @TempDir private static Path carpeta;
    @Autowired private MockMvc mvc;
    @Autowired private ImagenLocalService imagenes;
    @MockitoBean private PrendaService prendas;
    @MockitoBean private CategoriaService categorias;
    @MockitoBean private UsuarioDetailsService detalles;

    @DynamicPropertySource
    static void configurarDirectorio(DynamicPropertyRegistry registro) {
        registro.add("prendas.imagenes.directorio", () -> carpeta.toString());
    }

    @Test
    void administradorEnviaUnaImagenDesdeElFormularioConCsrf() throws Exception {
        var admin = user("admin").roles("ADMIN");
        mvc.perform(get("/prenda/nuevo").with(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("multipart/form-data")))
                .andExpect(content().string(containsString("type=\"file\"")))
                .andExpect(content().string(not(containsString("name=\"rutaImagen\""))));

        var imagen = imagenPng();
        var solicitud = multipart("/prenda/nuevo").file(imagen).with(admin)
                .param("nombre", "Camiseta").param("descripcion", "Algodón")
                .param("precio", "8500").param("talla", "M").param("cantidad", "8");
        mvc.perform(solicitud).andExpect(status().isForbidden());
        mvc.perform(solicitud.with(csrf())).andExpect(redirectedUrl("/prenda/listado"));
        verify(prendas).guardar(isNull(), argThat(formulario -> formulario.nombre().equals("Camiseta")), eq(imagen));
    }

    @Test
    void guardaYSirveUnPngDesdeElDisco() throws Exception {
        var archivo = imagenPng();
        String ruta = imagenes.subirImagen(archivo);
        Path guardada = carpeta.resolve(ruta.substring("/img/prendas/".length()));
        assertArrayEquals(archivo.getBytes(), Files.readAllBytes(guardada));

        mvc.perform(get(ruta)).andExpect(status().isOk())
                .andExpect(content().contentType("image/png")).andExpect(content().bytes(archivo.getBytes()));

        imagenes.eliminarImagen(ruta);
        assertFalse(Files.exists(guardada));
        mvc.perform(get(ruta)).andExpect(status().isNotFound());
    }

    @Test
    void guardaYSirveUnWebpConSuTipoOriginal() throws Exception {
        byte[] contenido = Base64.getDecoder().decode("UklGRh4AAABXRUJQVlA4TBEAAAAvAAAAAAfQ//73v/+BiOh/AAA=");
        var archivo = new MockMultipartFile("imagen", "prenda.webp", "image/webp", contenido);
        String ruta = imagenes.subirImagen(archivo);

        assertTrue(ruta.endsWith(".webp"));
        mvc.perform(get(ruta)).andExpect(status().isOk())
                .andExpect(content().contentType("image/webp")).andExpect(content().bytes(contenido));
    }

    @Test
    void rechazaArchivosDisfrazadosDeImagen() {
        var archivo = new MockMultipartFile("imagen", "prenda.png", "image/png", new byte[] {1, 2, 3});
        assertThrows(ImagenException.class, () -> imagenes.subirImagen(archivo));
    }

    private MockMultipartFile imagenPng() throws Exception {
        try (var datos = new ByteArrayOutputStream()) {
            ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", datos);
            return new MockMultipartFile("imagen", "prenda.png", "image/png", datos.toByteArray());
        }
    }
}
