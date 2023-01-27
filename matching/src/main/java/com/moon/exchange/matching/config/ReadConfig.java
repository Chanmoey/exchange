package com.moon.exchange.matching.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Chanmoey
 * @date 2023年01月27日
 */
@PropertySource(value = "classpath:matching.properties")
@Getter
public class ReadConfig {

    @Value("${id}")
    private short id;

    @Value("${order-recv-ip}")
    private String orderRecvIp;
    @Value("${order-recv-port}")
    private int orderRecvPort;

    @Value("${pub-ip}")
    private String pubIp;
    @Value("${pub-port}")
    private int pubPort;

    @Value("${seq-url-list}")
    private String seqUrlList;
}
