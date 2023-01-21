package com.moon.exchange.common.order;

import lombok.Getter;

/**
 * @author Chanmoey
 * @date 2023年01月21日
 */
@Getter
public enum OrderType {
    LIMIT(0); // Immediate or Cancel - equivalent to strict-risk market order

    private final int type;

    OrderType(int type) {
        this.type = type;
    }

    public static OrderType of(int type) {
        if (type == 0) {
            return LIMIT;
        }
        throw new IllegalArgumentException("unknown OrderType:" + type);
    }

}