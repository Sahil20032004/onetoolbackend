package com.etms.worldline.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FireBaseConfig {

    @PostConstruct
    public void initializeFirebase() throws IOException {
        // Specify the path to your service account key
        FileInputStream serviceAccount = new FileInputStream("/home/gcpticketingsystem_gmail_com/service_account.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        // Initialize Firebase only if no app has been initialized
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            System.out.println("FirebaseApp initialized successfully.");
        }
    }
}
