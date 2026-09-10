package com.aichainid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static assets from classpath:/static/ both at root and under /static/
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Direct root / and /app to /index.html
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/app").setViewName("forward:/index.html");
        registry.addViewController("/dashboard").setViewName("forward:/index.html");
    }
}
