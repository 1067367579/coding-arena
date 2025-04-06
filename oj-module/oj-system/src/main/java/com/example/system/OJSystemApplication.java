package com.example.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.**.mapper")
public class OJSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(OJSystemApplication.class, args);
    }
}
