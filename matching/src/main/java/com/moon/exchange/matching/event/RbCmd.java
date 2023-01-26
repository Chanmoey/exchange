package com.moon.exchange.matching.event;

import com.moon.exchange.common.order.CmdType;
import com.moon.exchange.common.order.OrderDirection;
import com.moon.exchange.common.order.OrderType;
import com.moon.exchange.common.quotation.L1MarketData;
import com.moon.exchange.matching.orderbook.MatchEvent;
import lombok.Builder;
import lombok.ToString;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

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
    public  short mid;
    /**
     * 用户id
     */
    public  long uid;

    public CmdType command;

    public  int code;

    public  OrderDirection direction;

    public  long price;

    /**
     * 委托量
     */
    public  long volume;

    /**
     * 委托编号
     */
    public long oid;

    /**
     * 订单委托类型
     */
    public  OrderType orderType;

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
