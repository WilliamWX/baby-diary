package com.babydiary;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.babydiary.mapper")
public class BabyDiaryApplication {
    public static void main(String[] args) {
        SpringApplication.run(BabyDiaryApplication.class, args);
    }
}
