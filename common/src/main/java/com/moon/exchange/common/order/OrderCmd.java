package com.moon.exchange.common.order;

import lombok.Builder;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Chanmoey
 * @date 2023年01月21日
 */
@Builder
@ToString
public class OrderCmd implements Serializable {

    public CmdType type;

    public long timestamp;

    /**
     * 会员id，券商的id
     */
    public final short mid;

    /**
     * 用户id
     */
    public final long uid;

    public final int code;

    public final OrderDirection direction;

    public final long price;

    /**
     * 委托量
     */
    public final long volume;

    /**
     * 订单委托类型
     */
    public final OrderType orderType;

    /**
     * 委托编号
     */
    public long oid;
}
