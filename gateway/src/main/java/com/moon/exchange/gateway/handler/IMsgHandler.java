package com.moon.exchange.gateway.handler;

import com.moon.exchange.common.bean.CommonMsg;
import io.vertx.core.net.NetSocket;

/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
public interface IMsgHandler {

    default void onConnect(NetSocket socket) {
    }


    default void onDisConnect(NetSocket socket) {
    }


    default void onException(NetSocket socket, Throwable e) {
    }


    void onCounterData(CommonMsg msg);
}
