package com.moon.exchange.common.order;

import lombok.Getter;

import java.util.Arrays;

/**
 * @author Chanmoey
 * @date 2023年01月21日
 */
@Getter
public enum OrderDirection {

    BUY(0),
    SELL(1);

    final int direction;

    OrderDirection(int direction) {
        this.direction = direction;
    }

    public static OrderDirection of(int direction) {
        return Arrays.stream(OrderDirection.values())
                .filter(o -> o.getDirection() == direction)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No Such Direction"));
    }
}
