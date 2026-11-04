package com.tienda;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class StorageConfig {

    private final String credentialsPath;

    public StorageConfig(@Value("${firebase.credentials-path:}") String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    @Bean
    public Storage storage() throws IOException {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalStateException(
                    "Firebase está habilitado: configure FIREBASE_CREDENTIALS_PATH con un JSON externo.");
        }

        // Las credenciales solo se leen de una ruta externa y el flujo se cierra.
        try (InputStream inputStream = Files.newInputStream(Path.of(credentialsPath))) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
            return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        }
    }
}
