package com.moon.exchange.counter;

import com.moon.exchange.common.uuid.OurUuid;
import com.moon.exchange.counter.config.CounterConfig;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CounterApplication {

    @Autowired
    private CounterConfig counterConfig;

    @PostConstruct
    private void init() {
        OurUuid.getInstance().init(counterConfig.getDataCenterId(),
                counterConfig.getWorkerId());
    }

    public static void main(String[] args) {
        SpringApplication.run(CounterApplication.class, args);
    }

}
