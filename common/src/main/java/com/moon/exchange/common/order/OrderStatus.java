package com.moon.exchange.common.order;

import lombok.Getter;

/**
 * @author Chanmoey
 * @date 2023年01月21日
 */
@Getter
public enum OrderStatus {
    NOT_SEL(0);

    final int code;

    OrderStatus(int code) {
        this.code = code;
    }
}
