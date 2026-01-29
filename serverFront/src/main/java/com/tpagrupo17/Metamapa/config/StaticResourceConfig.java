package com.tpagrupo17.Metamapa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String basePath = System.getProperty("user.dir");

        String uploadPath = "file:///" + basePath.replace("\\", "/") + "/uploads/";

        

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}


