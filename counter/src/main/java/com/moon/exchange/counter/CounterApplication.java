package com.moon.exchange.counter;

import com.moon.exchange.common.uuid.OurUuid;
import com.moon.exchange.counter.config.SecurityConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CounterApplication {

    @Autowired
    private SecurityConfig securityConfig;

    @PostConstruct
    private void init() {
        OurUuid.getInstance().init(securityConfig.getDataCenterId(),
                securityConfig.getWorkerId());
    }

    public static void main(String[] args) {
        SpringApplication.run(CounterApplication.class, args);
    }

}
