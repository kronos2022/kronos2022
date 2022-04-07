package com.documentprocessing.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@Slf4j
public class FirebaseInitializer {

    @PostConstruct
    public void initialize() {
        try {

            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/service-account-key.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
