package com.moon.exchange.matching.event;

import com.moon.exchange.common.order.OrderStatus;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Chanmoey
 * @date 2023年01月25日
 */
@NoArgsConstructor
@ToString
public final class MatchEvent {

    public long timestamp;

    public short mid;

    public long oid;

    public OrderStatus status = OrderStatus.NOT_SET;

    public long tid;

    //撤单数量 成交数量
    public long volume;

    public long price;


    public MatchData copy() {
        return MatchData.builder()
                .timestamp(this.timestamp)
                .mid(this.mid)
                .oid(this.oid)
                .status(this.status)
                .tid(this.tid)
                .volume(this.volume)
                .price(this.price)
                .build();

    }



}
