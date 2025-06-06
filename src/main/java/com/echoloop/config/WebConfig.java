package com.echoloop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Handle static resources from classpath
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        
        // Handle uploaded files from both old and new locations
        Path uploadPath = Paths.get(uploadDir);
        String uploadAbsolutePath = uploadPath.toFile().getAbsolutePath();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                    "file:" + uploadAbsolutePath + "/",
                    "classpath:/static/uploads/"
                );
    }
} 