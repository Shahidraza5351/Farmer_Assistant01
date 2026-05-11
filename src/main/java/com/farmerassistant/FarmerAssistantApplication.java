package com.farmerassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Farmer Assistant AI Backend.
 * This application provides AI-powered crop disease detection
 * and farming question answering via secure REST APIs.
 */
@SpringBootApplication
public class FarmerAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmerAssistantApplication.class, args);
    }
}
