package com.moon.exchange.gateway.cache;

import com.moon.exchange.common.order.OrderCmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
public class OrderCmdContainer {

    private static final OrderCmdContainer INSTANCE = new OrderCmdContainer();

    private OrderCmdContainer() {

    }

    public static OrderCmdContainer getInstance() {
        return INSTANCE;
    }

    private final BlockingDeque<OrderCmd> queue = new LinkedBlockingDeque<>();

    public boolean cache(OrderCmd cmd) {
        return queue.offer(cmd);
    }

    public int size() {
        return queue.size();
    }

    public List<OrderCmd> getAll() {
        List<OrderCmd> msgList = new ArrayList<>();
        int count = queue.drainTo(msgList);
        if (count == 0) {
            return Collections.emptyList();
        } else {
            return msgList;
        }
    }
}
