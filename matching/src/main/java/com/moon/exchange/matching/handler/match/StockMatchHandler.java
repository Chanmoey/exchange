package com.moon.exchange.matching.handler.match;

import com.moon.exchange.matching.event.CmdResultCode;
import com.moon.exchange.matching.event.RbCmd;
import com.moon.exchange.matching.handler.BaseHandler;
import com.moon.exchange.matching.orderbook.IOrderBook;
import io.netty.util.collection.IntObjectHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
@RequiredArgsConstructor
public class StockMatchHandler extends BaseHandler {

    @NonNull
    private final IntObjectHashMap<IOrderBook> orderBookMap;

    @Override
    public void onEvent(RbCmd rbCmd, long sequence, boolean endOfBatch) throws Exception {
        if (rbCmd.resultCode.getCode() < 0) {
            return;
        }

        rbCmd.resultCode = processRbCmd(rbCmd);
    }

    private CmdResultCode processRbCmd(RbCmd rbCmd) {
        switch (rbCmd.command) {
            case NEW_ORDER:
                return orderBookMap.get(rbCmd.code).newOrder(rbCmd);
            case CANCEL_ORDER:
                return orderBookMap.get(rbCmd.code).cancelOrder(rbCmd);
            case QUOTATION_PUB:
                orderBookMap.forEach((code, orderBook) ->
                        rbCmd.marketDataMap.put(code, orderBook.getL1MarketDataSnapshot()));
                return CmdResultCode.SUCCESS;
            default:
                return CmdResultCode.SUCCESS;
        }
    }


}
