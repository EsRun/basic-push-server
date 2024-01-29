package com.push.www.fcm.config;



import java.io.FileInputStream;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;


import jakarta.annotation.PostConstruct;

@Configuration
public class fcmConfig {

	@PostConstruct
	public void init() {
		try{
			//System.out.println(GoogleCredentials.getApplicationDefault());
			//FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.getApplicationDefault()).build();
			Resource resource = new ClassPathResource("config/mungyeong-firebase-adminsdk.json");
			InputStream serviceAccount = resource.getInputStream();
			//FileInputStream serviceAccount = new FileInputStream("config/fcm.json");

					FirebaseOptions options = new FirebaseOptions.Builder()
					  .setCredentials(GoogleCredentials.fromStream(serviceAccount))
					  .build();
            FirebaseApp.initializeApp(options);
        }catch (Exception e){
            e.printStackTrace();
        }
	}
}
