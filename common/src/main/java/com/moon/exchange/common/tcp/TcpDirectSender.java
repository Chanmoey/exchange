package com.moon.exchange.common.tcp;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
@Log4j2
@RequiredArgsConstructor
public class TcpDirectSender {

    @NonNull
    private String ip;

    @NonNull
    private int port;

    @NonNull
    private Vertx vertx;

    private volatile NetSocket socket;

    // 对订单进行缓存，方便异步处理，socket从缓存中获取数据，并发送到网关
    private final BlockingDeque<Buffer> sendCache = new LinkedBlockingDeque<>();

    public boolean send(Buffer buffer) {
        return sendCache.offer(buffer);
    }

    public void startUp() {
        vertx.createNetClient().connect(port, ip, new ClientConnectionHandler());

        new Thread(() -> {
            while (true) {
                try {
                    Buffer msg = sendCache.poll(5, TimeUnit.SECONDS);
                    if (msg != null && msg.length() > 0 && socket != null) {
                        socket.write(msg);
                    }
                } catch (Exception e) {
                    log.error("msg send fail, continue");
                }
            }
        }).start();
    }


    private class ClientConnectionHandler implements Handler<AsyncResult<NetSocket>> {

        private void reconnect() {
            vertx.setTimer((long) 1000 * 5, r -> {
                log.info("try reconnect to server to {}:{}", ip, port);
                vertx.createNetClient().connect(port, ip, new ClientConnectionHandler());
            });
        }

        @Override
        public void handle(AsyncResult<NetSocket> result) {
            if (result.succeeded()) {
                log.info("connect success to remote {}:{}", ip, port);
                socket = result.result();

                socket.closeHandler(close -> {
                    log.error("connect to remote {} closed", socket.remoteAddress());
                    reconnect();
                });

                socket.exceptionHandler(ex -> log.error("error exist", ex.getCause()));
            } else {
                // 获取消息，并插入缓存
                log.error("connection fail to remote {}:{}", ip, port);
            }
        }
    }
}
