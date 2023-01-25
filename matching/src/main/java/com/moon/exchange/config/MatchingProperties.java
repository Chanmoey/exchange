package com.moon.exchange.config;

import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author Chanmoey
 * @date 2023年01月25日
 */
@Component
@PropertySource(value = "classpath:matching.properties")
@Getter
@ToString
public class MatchingProperties {

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

