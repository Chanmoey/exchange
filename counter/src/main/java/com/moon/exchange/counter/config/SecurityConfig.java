package com.moon.exchange.counter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
@PropertySource(value = "classpath:config/security.properties")
@Component
@Getter
@Setter
public class SecurityConfig {

    @Value("${counter.data-center-id}")
    public long dataCenterId;

    @Value("${counter.worker-id}")
    public long workerId;

    @Value("${counter.id}")
    public short id;

}
