package com.moon.exchange.matching.orderbook;


import com.moon.exchange.common.quotation.L1MarketData;
import com.moon.exchange.matching.event.CmdResultCode;
import com.moon.exchange.matching.event.RbCmd;

import static com.moon.exchange.common.quotation.L1MarketData.L1_SIZE;

public interface IOrderBook {

    /**
     * 新增委托
     */
    CmdResultCode newOrder(RbCmd cmd);

    /**
     * 撤单
     */
    CmdResultCode cancelOrder(RbCmd cmd);

    /**
     * 查询行情快照
     */
    default L1MarketData getL1MarketDataSnapshot() {
        final int buySize = limitBuyBucketSize(L1_SIZE);
        final int sellSize = limitSellBucketSize(L1_SIZE);
        final L1MarketData data = new L1MarketData(buySize, sellSize);
        fillBuys(buySize, data);
        fillSells(sellSize, data);
        fillCode(data);

        data.timestamp = System.currentTimeMillis();

        return data;
    }

    void fillCode(L1MarketData data);

    void fillSells(int size, L1MarketData data);

    void fillBuys(int size, L1MarketData data);

    int limitBuyBucketSize(int maxSize);

    int limitSellBucketSize(int maxSize);

}
