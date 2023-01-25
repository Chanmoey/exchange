package com.moon.exchange.common.order;

import lombok.Getter;

/**
 * @author Chanmoey
 * @date 2023年01月21日
 */
@Getter
public enum OrderStatus {

    NOT_SET(-1),

    CANCEL_STANDBY(0),
    CANCEL_ED(1),
    PART_CANCEL(2),

    ORDER_STANDBY(3),
    ORDER_ED(4),

    TRADE_ED(5),
    PART_TRADE(6),

    FAIL(7);

    final int code;

    OrderStatus(int code) {
        this.code = code;
    }
}
