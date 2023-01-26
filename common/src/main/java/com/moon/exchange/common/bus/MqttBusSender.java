package com.moon.exchange.common.bus;

import com.moon.exchange.common.bean.CommonMsg;
import com.moon.exchange.common.codec.IMsgCodec;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
@Slf4j
@RequiredArgsConstructor
public class MqttBusSender implements IBusSender {

    @NonNull
    private String ip;
    @NonNull
    private int port;
    @NonNull
    private IMsgCodec msgCodec;
    @NonNull
    private Vertx vertx;

    private volatile MqttClient sender;

    @Override
    public void startUp() {
        // 连接总线
        mqttConnect();
    }

    private void mqttConnect() {
        MqttClient mqttClient = MqttClient.create(vertx);
        mqttClient.connect(port, ip, res -> {
            if (res.succeeded()) {
                log.info("succeed to connect to mqtt bus[ip: {}, port: {}]", ip, port);
                sender = mqttClient;
            } else {
                // 失败重连
                log.error("fail connect to mqtt bus[ip: {}, port: {}]", ip, port);
                mqttConnect();
            }
        });

        mqttClient.closeHandler(c -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                log.error(String.valueOf(e));
            }
            mqttConnect();
        });
    }

    @Override
    public void publish(CommonMsg msg) {
        sender.publish(
                Short.toString(msg.getMsgDst()),
                msgCodec.encodeToBuffer(msg),
                MqttQoS.AT_LEAST_ONCE,
                false,
                false
        );
    }
}
