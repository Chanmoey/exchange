package com.moon.exchange.gateway;

import com.moon.exchange.common.checksum.XorCheckSum;
import com.moon.exchange.common.codec.BodyCodec;
import com.moon.exchange.gateway.config.GatewayConfig;
import lombok.extern.log4j.Log4j2;

import java.util.Objects;

/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
@Log4j2
public class GatewayStartup {

    public static void main(String[] args) throws Exception {
        String configFileName = "gateway.xml";

        GatewayConfig config = new GatewayConfig();
        config.initConfig(Objects.requireNonNull(
                GatewayStartup.class.getResource("/")).getPath() + configFileName);
        config.setCheckSum(new XorCheckSum());
        config.setBodyCodec(new BodyCodec());
        config.startUp();
    }
}
