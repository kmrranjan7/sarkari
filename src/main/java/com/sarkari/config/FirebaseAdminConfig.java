package com.sarkari.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.firebase", name = "enabled", havingValue = "true")
public class FirebaseAdminConfig {

    @Bean
    FirebaseApp firebaseApp(@Value("${app.firebase.service-account-file}") String serviceAccountFile)
            throws IOException {
        if (serviceAccountFile.isBlank()) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT_FILE must be configured when Firebase is enabled.");
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        try (InputStream credentials = Files.newInputStream(Path.of(serviceAccountFile))) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build();
            return FirebaseApp.initializeApp(options);
        }
    }
}
