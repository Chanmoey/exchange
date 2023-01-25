package com.moon.exchange.matching.orderbook;

import com.moon.exchange.common.order.OrderStatus;
import com.moon.exchange.matching.event.RbCmd;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Log4j2
@ToString
public class GOrderBucketImpl implements IOrderBucket {

    /**
     * 价格
     */
    @Getter
    @Setter
    private long price;

    /**
     * 量
     */
    @Getter
    private long totalVolume = 0;

    /**
     * 委托列表 list行不通
     * key：oid；value：order
     */
    private final LinkedHashMap<Long, Order> entries = new LinkedHashMap<>();


    @Override
    public void put(Order order) {
        entries.put(order.getOid(), order);
        // 总委托 = 订单委托 - 已成交的委托
        totalVolume += order.getVolume() - order.getTradeVolume();
    }

    @Override
    public Order remove(long oid) {
        //防止重复执行删除订单的请求
        Order order = entries.get(oid);
        if (order == null) {
            return null;
        }
        entries.remove(oid);

        totalVolume -= order.getVolume() - order.getTradeVolume();

        return order;
    }

    @Override
    public long match(long volumeLeft, RbCmd triggerCmd, Consumer<Order> removeOrderCallback) {
        // S 46 --> 5 10 24
        // S 45 --> 11 20 10 20
        // B 45 100
        // 遍历bucket中的委托单
        Iterator<Map.Entry<Long, Order>> iterator = entries.entrySet().iterator();

        long volumeMatch = 0;

        while (iterator.hasNext() && volumeLeft > 0) {
            Map.Entry<Long, Order> next = iterator.next();
            Order order = next.getValue();
            //计算order可以吃多少量，处理的委托能吃掉的量和bucket中当前委托剩余的量，取最小值
            long traded = Math.min(volumeLeft, order.getVolume() - order.getTradeVolume());
            volumeMatch += traded;

            //1.order自身的量 2.volumeLeft 3.bucket总委托量
            order.setTradeVolume(order.getTradeVolume() + traded);
            volumeLeft -= traded;
            totalVolume -= traded;

            //生成事件
            boolean fullMatch = order.getVolume() == order.getTradeVolume();
            genMatchEvent(order, triggerCmd, fullMatch, volumeLeft == 0, traded);

            // 委托已经全部完成，移除当前order
            if (fullMatch) {
                removeOrderCallback.accept(order);
                iterator.remove();
            }
        }

        return volumeMatch;
    }

    /**
     *
     * @param order 卖委托
     * @param cmd 买委托
     * @param fullMatch 卖委托是否已经全部卖完
     * @param cmdFullMatch 买委托是否已经全部买完
     * @param traded 成交量
     */
    private void genMatchEvent(final Order order, final RbCmd cmd, boolean fullMatch, boolean cmdFullMatch, long traded) {

        long now = System.currentTimeMillis();

        long tid = IOrderBucket.tidGen.getAndIncrement();

        //两个MatchEvent
        MatchEvent bidEvent = new MatchEvent();
        bidEvent.timestamp = now;
        bidEvent.mid = cmd.mid;
        bidEvent.oid = cmd.oid;
        bidEvent.status = cmdFullMatch ? OrderStatus.TRADE_ED : OrderStatus.PART_TRADE;
        bidEvent.tid = tid;
        bidEvent.volume = traded;
        bidEvent.price = order.getPrice();
        cmd.matchEventList.add(bidEvent);


        MatchEvent ofrEvent = new MatchEvent();
        ofrEvent.timestamp = now;
        ofrEvent.mid = order.getMid();
        ofrEvent.oid = order.getOid();
        ofrEvent.status = fullMatch ? OrderStatus.TRADE_ED : OrderStatus.PART_TRADE;
        ofrEvent.tid = tid;
        ofrEvent.volume = traded;
        ofrEvent.price = order.getPrice();
        cmd.matchEventList.add(ofrEvent);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GOrderBucketImpl that = (GOrderBucketImpl) o;

        return new EqualsBuilder()
                .append(price, that.price)
                .append(entries, that.entries)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(price)
                .append(entries)
                .toHashCode();
    }
}
