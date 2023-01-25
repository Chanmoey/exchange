package com.moon.exchange.matching.event;

import lombok.Getter;

/**
 * @author Chanmoey
 * @date 2023年01月25日
 */
@Getter
public enum CmdResultCode {

    SUCCESS(1),


    /////////////////////////OrderBook/////////////////////////////

    INVALID_ORDER_ID(-1),//不合法委托ID
    INVALID_ORDER_PRICE(-2),//不合法委托价格
    DUPLICATE_ORDER_ID(-3),//重复委托编号
    UNKNOWN_MATCH_CMD(-4),//未知撮合指令
    INVALID_ORDER_BOOK_ID(-5),//未知订单簿

    ///////////////////////risk/////////////////////////////
    RISK_INVALID_USER(-100),//用户不存在
    RISK_INVALID_CODE(-101),//代码不存在
    RISK_INVALID_BALANCE(-102),//资金不正确

    ///////////////////////match/////////////////////////////
    MATCHING_INVALID_STOCK(-200),


    DROP(-9999);

    private final int code;

    CmdResultCode(int code) {
        this.code = code;
    }
}
