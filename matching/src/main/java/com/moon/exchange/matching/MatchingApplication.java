package com.moon.exchange.matching;

import com.moon.exchange.matching.config.MatchingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * @author Chanmoey
 * @date 2023年01月25日
 */
@SpringBootApplication
public class MatchingApplication {

    @Qualifier("myMatchingConfig")
    @Autowired()
    private  MatchingConfig config;

    @PostConstruct
    private void startUp() throws Exception {
        config.startUp();
    }

    public static void main(String[] args) {
        SpringApplication.run(MatchingApplication.class, args);
    }
}
