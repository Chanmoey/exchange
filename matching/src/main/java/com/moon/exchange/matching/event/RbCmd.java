package com.moon.exchange.matching.event;

import com.moon.exchange.common.order.CmdType;
import com.moon.exchange.common.order.OrderDirection;
import com.moon.exchange.common.order.OrderType;
import com.moon.exchange.matching.orderbook.MatchEvent;
import io.netty.util.collection.IntObjectHashMap;
import lombok.Builder;
import lombok.ToString;

import java.util.List;

/**
 * 实际存储到disruptor中的数据
 *
 * @author Chanmoey
 * @date 2023年01月25日
 */
@Builder
@ToString
public class RbCmd {

    public long timestamp;
    /**
     * 会员id，券商的id
     */
    public final short mid;
    /**
     * 用户id
     */
    public final long uid;

    public CmdType command;

    public final int code;

    public final OrderDirection direction;

    public final long price;

    /**
     * 委托量
     */
    public final long volume;

    /**
     * 委托编号
     */
    public long oid;

    /**
     * 订单委托类型
     */
    public final OrderType orderType;

    /**
     * 保存撮合结果
     */
    public List<MatchEvent> matchEventList;

    /**
     * 前置风控 -> 撮合 -> 发布
     */
    public CmdResultCode resultCode;

    /**
     * 保存行情
     */
    public IntObjectHashMap<L1MarketData> marketDataMap;
}
