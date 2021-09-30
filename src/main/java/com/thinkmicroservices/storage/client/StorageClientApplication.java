package com.thinkmicroservices.storage.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.thinkmicroservices.storage.client")
public class StorageClientApplication {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(StorageClientApplication.class, args);
    }

}
