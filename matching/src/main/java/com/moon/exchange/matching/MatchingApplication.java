package com.moon.exchange.matching;

import com.moon.exchange.common.checksum.XorCheckSum;
import com.moon.exchange.common.codec.BodyCodec;
import com.moon.exchange.common.codec.MsgCodec;
import com.moon.exchange.matching.config.MatchingConfig;
import com.moon.exchange.matching.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * @author Chanmoey
 * @date 2023年01月25日
 */
@SpringBootApplication
public class MatchingApplication {

    @Autowired
    private MatchingService service;

    @PostConstruct
    private void init() throws Exception {
        MatchingConfig config = new MatchingConfig(service);
        config.setMsgCodec(new MsgCodec());
        config.setCheckSum(new XorCheckSum());
        config.setBodyCodec(new BodyCodec());
        config.startUp();
    }

    public static void main(String[] args) {
        SpringApplication.run(MatchingApplication.class, args);
    }
}
