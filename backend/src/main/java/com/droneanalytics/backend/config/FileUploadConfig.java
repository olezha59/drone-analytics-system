package com.droneanalytics.backend.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class FileUploadConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Максимальный размер файла: 50MB
        factory.setMaxFileSize(DataSize.ofMegabytes(50));
        
        // Максимальный размер запроса: 50MB  
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        
        return factory.createMultipartConfig();
    }
}
