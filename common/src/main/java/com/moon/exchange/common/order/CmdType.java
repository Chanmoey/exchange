package com.moon.exchange.common.order;

import lombok.Getter;

import java.util.Arrays;

/**
 * @author Chanmoey
 * @date 2023年01月21日
 */
@Getter
public enum CmdType {

    /**
     * 委托类
     */
    NEW_ORDER(0),
    CANCEL_ORDER(1),

    /**
     * 权限类
     */
    SUSPEND_USER(2),
    RESUME_USER(3),

    /**
     * 状态类
     */
    SHUTDOWN_ENGINE(4),

    /**
     * 查询类
     */
    BINARY_DATA(5),
    ORDER_BOOK_REQUEST(6),

    /**
     * 行情类
     */
    QUOTATION_PUB(7),

    /**
     * 资金类
     */
    BALANCE_ADJUSTMENT(8);

    private final int type;

    CmdType(int type) {
        this.type =  type;
    }

    public static CmdType of(int type) {

        return Arrays.stream(CmdType.values())
                .filter(c -> c.getType() == type)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No such CmdType"));
    }
}
