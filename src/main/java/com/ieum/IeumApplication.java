package com.ieum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IeumApplication {

    public static void main(String[] args) {
        SpringApplication.run(IeumApplication.class, args);
    }

}
