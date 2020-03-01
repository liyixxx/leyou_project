package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author 柒
 * @date 2020-02-17 14:24
 */
@SpringBootApplication
@EnableEurekaServer
public class LeyouRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeyouRegistryApplication.class,args);
    }
}
