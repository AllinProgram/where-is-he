package com.ufan0.whereishe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class WhereIsHeApplication{

    public static void main(String[] args) {
        SpringApplication.run(WhereIsHeApplication.class, args);
    }

}
