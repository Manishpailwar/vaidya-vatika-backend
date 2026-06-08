package com.vaidyavatika;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VaidyaVatikaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaidyaVatikaApplication.class, args);
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   🌿  Vaidya Vatika Backend Started!     ║");
        System.out.println("║   Running at: http://localhost:8080       ║");
        System.out.println("║   API Base:   /api/v1                     ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }
}
