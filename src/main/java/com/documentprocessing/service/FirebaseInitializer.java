package com.documentprocessing.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class FirebaseInitializer {

    final String projectId;

    public FirebaseInitializer(@Value("${project-id}") final String projectId) {
        this.projectId = projectId;
        log.debug("Check if projectId is populated : {}", projectId);
    }

    @PostConstruct
    public void initialize() {
        try {

            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .setStorageBucket("psyched-signal-345109.appspot.com")
                    .build();
            FirebaseApp.initializeApp(options);

        } catch (Exception exception) {
            log.error("Error occurred while initializing firebase : {}", exception.getMessage());
        }
    }

}
