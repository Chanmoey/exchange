package com.moon.exchange.counter.config;

import com.moon.exchange.common.checksum.ICheckSum;
import com.moon.exchange.common.codec.IBodyCodec;
import com.moon.exchange.common.codec.IMsgCodec;
import io.vertx.core.Vertx;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class CounterConfig {

    /**
     * uuid相关配置
     */
    @Value("${counter.data-center-id}")
    public long dataCenterId;

    @Value("${counter.worker-id}")
    public long workerId;

    @Value("${counter.id}")
    public short id;

    /**
     * 编解码相关配置
     */
    @Value("${counter.checksum}")
    private String checkSumClass;

    @Value("${counter.body-codec}")
    private String bodyCodecClass;

    @Value("${counter.msg-codec}")
    private String msgCodecClass;

    private ICheckSum checkSum;

    private IBodyCodec bodyCodec;

    private IMsgCodec msgCodec;

    @PostConstruct
    private void init() {
        Class<?> clazz;

        try {
            clazz = Class.forName(checkSumClass);
            checkSum = (ICheckSum) clazz.getDeclaredConstructor().newInstance();

            clazz = Class.forName(bodyCodecClass);
            bodyCodec = (IBodyCodec) clazz.getDeclaredConstructor().newInstance();

            clazz = Class.forName(msgCodecClass);
            msgCodec = (IMsgCodec) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("init config error", e);
        }
    }

    /**
     * 网关相关配置
     */
    @Value("${counter.send-ip}")
    private String sendIp;

    @Value("${counter.send-port}")
    private int sendPort;

    @Value("${counter.gateway-id}")
    private short gatewayId;

    private Vertx vertx = Vertx.vertx();
}
