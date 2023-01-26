package com.moon.exchange.counter.bus;

import com.moon.exchange.common.bean.CommonMsg;
import com.moon.exchange.common.checksum.ICheckSum;
import com.moon.exchange.common.codec.IMsgCodec;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.moon.exchange.common.bean.MsgConstants.MATCH_ORDER_DATA;
import static com.moon.exchange.common.bean.MsgConstants.MATCH_QUOTATION_DATA;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
@Log4j2
@RequiredArgsConstructor
public class MqttBusConsumer {

    @NonNull
    private String busIp;

    @NonNull
    private int busPort;

    @NonNull
    private String recvAddr;

    @NonNull
    private IMsgCodec msgCodec;

    @NonNull
    private ICheckSum checkSum;

    @NonNull
    private Vertx vertx;

    public void startUp() {

    }

    private final static String QUOTATION_ADDR = "-1";

    public static final String INNER_MARKET_DATA_CACHE_ADDR = "l1_market_data_cache_addr";

    public static final String INNER_MATCH_DATA_ADDR = "match_data_addr";

    private void mqttConnect(Vertx vertx, int busPort, String busIp) {
        MqttClient mqttClient = MqttClient.create(vertx);
        mqttClient.connect(busPort, busIp, res -> {
            if (res.succeeded()) {
                log.info("connect mqtt bus succeed");
                Map<String, Integer> topic = new HashMap<>();
                topic.put(recvAddr, MqttQoS.AT_LEAST_ONCE.value());
                topic.put(QUOTATION_ADDR, MqttQoS.AT_LEAST_ONCE.value());

                mqttClient.subscribe(topic);
                mqttClient.publishHandler(h -> {
                    CommonMsg msg = msgCodec.decodeFromBuffer(h.payload());
                    if (msg.getChecksum() != checkSum.getCheckSum(msg.getBody())) {
                        return;
                    }

                    byte[] body = msg.getBody();

                    if (ArrayUtils.isNotEmpty(body)) {
                        short msgType = msg.getMsgType();
                        if (msgType == MATCH_QUOTATION_DATA) {
                            vertx.eventBus().send(INNER_MATCH_DATA_ADDR, Buffer.buffer(body));
                        } else if (msgType == MATCH_ORDER_DATA) {
                            vertx.eventBus().send(INNER_MARKET_DATA_CACHE_ADDR, Buffer.buffer(body));
                        } else {
                            log.error("recv unknown msgType: {}", msg);
                        }
                    }
                });
            } else {
                log.error("connect mqtt bus failed");
            }
        });

        mqttClient.closeHandler(c -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                log.error(e);
            }
            mqttConnect(vertx, busPort, busIp);
        });
    }
}
