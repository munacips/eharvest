package com.munashechipanga.eharvest.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-path}")
    private String serviceAccountPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
                    log.warn("Firebase service-account path is not configured; skipping Firebase initialization");
                    return;
                }

                Path path = Paths.get(serviceAccountPath);
                if (!Files.exists(path)) {
                    log.warn("Firebase service-account file not found at '{}'; skipping Firebase initialization", path.toAbsolutePath());
                    return;
                }

                try (FileInputStream serviceAccount = new FileInputStream(path.toFile())) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();

                    FirebaseApp.initializeApp(options);
                    log.info("Firebase initialized successfully");
                }
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: ", e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}
