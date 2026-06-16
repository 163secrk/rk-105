package com.talentbridge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.talentbridge.mapper")
public class TalentBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalentBridgeApplication.class, args);
    }
}
