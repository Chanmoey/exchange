package com.moon.exchange.common.quotation;

import com.moon.exchange.common.order.OrderStatus;
import lombok.Builder;

import java.io.Serializable;

/**
 * 放到总线，供柜台等业务使用
 */
@Builder
public class MatchData implements Serializable {

    public long timestamp;

    public short mid;

    public long oid;

    public OrderStatus status;

    public long tid;

    //撤单数量 成交数量
    public long volume;

    public long price;
}
