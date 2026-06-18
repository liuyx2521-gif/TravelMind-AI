package com.travelmind;

import com.travelmind.config.AppProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@MapperScan("com.travelmind.mapper")
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class TravelMindApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelMindApplication.class, args);
    }
}
