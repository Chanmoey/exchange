package com.moon.exchange.matching.orderbook;

import com.moon.exchange.matching.event.RbCmd;
import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
public interface IOrderBucket extends Comparable<IOrderBucket> {

    AtomicLong tidGen = new AtomicLong(0);

    /**
     * 新增订单
     */
    void put(Order order);

    /**
     * 移除订单
     */
    Order remove(long oid);

    /**
     * 撮合match
     */
    long match(long volumeLeft, RbCmd triggerCmd,
               Consumer<Order> removeOrderCallback);

    /**
     * 行情发布
     */
    long getPrice();

    void setPrice(long price);

    long getTotalVolume();

    /**
     * 初始化选项
     */
    static IOrderBucket create(OrderBucketImplType type) {
        if (Objects.requireNonNull(type) == OrderBucketImplType.GUDY) {
            return new GOrderBucketImpl();
        }
        throw new IllegalArgumentException();
    }

    @Getter
    enum OrderBucketImplType {
        GUDY(0);

        private final byte code;

        OrderBucketImplType(int code) {
            this.code = (byte) code;
        }
    }


    //6.比较 排序
    default int compareTo(IOrderBucket other) {
        return Long.compare(this.getPrice(), other.getPrice());
    }
}
