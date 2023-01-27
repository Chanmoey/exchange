package com.moon.exchange.counter.config;

import io.vertx.core.Vertx;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author Chanmoey
 * @date 2023年01月27日
 */
@Log4j2
@Configuration
public class WebSocketConfig {

    public static final String L1_MARKET_DATA_PREFIX = "l1-market-data";

    public final static String TRADE_NOTIFY_ADDR_PREFIX = "trade-change-";

    public final static String ORDER_NOTIFY_ADDR_PREFIX = "order-change-";

    @Autowired
    private CounterConfig config;

    @PostConstruct
    private void init() {
        Vertx vertx = config.getVertx();
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);

        // 只允许成交、委托变动通过总线往外发送
        SockJSBridgeOptions options = new SockJSBridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress(L1_MARKET_DATA_PREFIX))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex(ORDER_NOTIFY_ADDR_PREFIX))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex(TRADE_NOTIFY_ADDR_PREFIX));
        sockJSHandler.bridge(options, event -> {
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                log.info("client : {} connected", event.socket().remoteAddress());
            } else if (event.type() == BridgeEventType.SOCKET_CLOSED) {
                log.info("client : {} closed", event.socket().remoteAddress());
            }
            event.complete(true);
        });

        Router router = Router.router(vertx);

        router.route("/event-bus/*").handler(sockJSHandler);
        vertx.createHttpServer().requestHandler(router).listen(config.getSendPort());
    }
}
