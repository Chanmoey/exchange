package com.moon.exchange.common.bus;

import com.moon.exchange.common.bean.CommonMsg;
import com.moon.exchange.common.codec.IMsgCodec;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.TimeUnit;

@Log4j2
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


    @Override
    public void startUp() {
        //连接总线
        mqttConnect();
    }

    private void mqttConnect() {
        MqttClient mqttClient = MqttClient.create(vertx);
        sender = mqttClient;
        mqttClient.connect(port,ip, res -> {
            if (res.succeeded()) {
                log.info("connect to mqtt bus[ip:{},port:{}] succeed", ip, port);
                sender = mqttClient;
            } else {
                log.info("connect to mqtt bus[ip:{},port:{}] fail", ip, port);
                mqttConnect();
            }
        });


        mqttClient.closeHandler(h -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                log.error(e);
            }
            mqttConnect();
        });
    }

    private volatile MqttClient sender;

    @Override
    public void publish(CommonMsg msg) {
        sender.publish(Short.toString(msg.getMsgDst()),
                msgCodec.encodeToBuffer(msg),
                MqttQoS.AT_LEAST_ONCE,
                false,
                false);
    }
}
