package com.documentprocessing.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;

/**
 * Please check and modify the json path from application.properties, if needed.
 */

@Service
@Slf4j
public class FirebaseInitializer {

    final String serviceAccountKeyPath;

    public FirebaseInitializer(@Value("${service-account-json.path}") final String serviceAccountKeyPath) {
        this.serviceAccountKeyPath = serviceAccountKeyPath;
        log.debug("Check if path is populated : {}", serviceAccountKeyPath);
    }

    @PostConstruct
    public void initialize() {
        try {

            FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyPath);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

        } catch (Exception exception) {
            log.error("Error occurred while initializing firebase : {}", exception.getMessage());
        }
    }

}
