package com.mastery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.mastery.mapper")
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableScheduling
public class MasteryApplication {
    public static void main(String[] args) {
        SpringApplication.run(MasteryApplication.class, args);
        System.out.println("\n=================================");
        System.out.println("  Java Mastery Pro v2.0");
        System.out.println("  http://localhost:8080");
        System.out.println("  Docs: http://localhost:8080/doc.html");
        System.out.println("=================================\n");
    }
}
