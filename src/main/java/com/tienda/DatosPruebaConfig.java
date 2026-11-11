package com.tienda;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Profile("demo")
public class DatosPruebaConfig {
    @Bean
    ApplicationRunner cargarDatosPrueba(JdbcTemplate jdbc, TransactionTemplate transaccion, PasswordEncoder claves,
            @Value("${ECOMODA_DEMO_PASSWORD:}") String clave) {
        return argumentos -> {
            if (clave.isBlank() || clave.length() < 8 || clave.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalArgumentException("Define ECOMODA_DEMO_PASSWORD con entre 8 caracteres y 72 bytes.");
            }
            String hash = claves.encode(clave);
            transaccion.executeWithoutResult(estado -> jdbc.execute((ConnectionCallback<Void>) conexion -> {
                try (var variable = conexion.prepareStatement("SET @demo_hash = ?")) {
                    variable.setString(1, hash);
                    variable.execute();
                    try {
                        ScriptUtils.executeSqlScript(conexion, new EncodedResource(
                                new ClassPathResource("sql/renombrar_datos_prueba.sql"), StandardCharsets.UTF_8));
                        ScriptUtils.executeSqlScript(conexion, new EncodedResource(
                                new ClassPathResource("sql/catalogo_prueba.sql"), StandardCharsets.UTF_8));
                        ScriptUtils.executeSqlScript(conexion, new EncodedResource(
                                new ClassPathResource("sql/datos_prueba.sql"), StandardCharsets.UTF_8));
                    } finally {
                        variable.setNull(1, java.sql.Types.VARCHAR);
                        variable.execute();
                    }
                }
                return null;
            }));
        };
    }
}
