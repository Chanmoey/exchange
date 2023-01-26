package com.moon.exchange.counter.service;

import com.moon.exchange.common.order.OrderCmd;
import com.moon.exchange.common.order.OrderStatus;
import com.moon.exchange.common.quotation.MatchData;
import com.moon.exchange.counter.cache.CacheType;
import com.moon.exchange.counter.cache.RedisStringCache;
import com.moon.exchange.counter.entity.Trade;
import com.moon.exchange.counter.exception.bussness.NoFountException;
import com.moon.exchange.counter.repository.TradeRepository;
import com.moon.exchange.counter.util.JsonUtil;
import com.moon.exchange.counter.util.TimeformatUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月20日
 */
@Service
public class TradeServiceImpl {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private StockServiceImpl stockService;

    public List<Trade> getTradeList(Long uid) {
        // 先查缓存
        String uidStr = uid.toString();
        String tradeStr = RedisStringCache.get(uidStr, CacheType.TRADE);

        if (StringUtils.isEmpty(tradeStr)) {
            // 查数据库
            List<Trade> dbList = tradeRepository.findTradesByUid(uid)
                    .orElseThrow(() -> new NoFountException(20000));
            // 设置股票名字
            this.setStockName(dbList);

            // 更新缓存
            RedisStringCache.cache(uidStr, JsonUtil.toJson(dbList), CacheType.TRADE);
            return dbList;
        } else {
            return JsonUtil.fromJsonArr(tradeStr, Trade.class);
        }
    }

    private void setStockName(List<Trade> trades) {
        trades.forEach(trade -> trade.setName(stockService.getNameByCode(trade.getCode())));
    }

    public void saveTrade(int orderId, MatchData md, OrderCmd orderCmd) {
        if (orderCmd == null) {
            return;
        }

        Trade trade = new Trade();
        trade.setId(md.tid);
        trade.setUid(orderCmd.uid);
        trade.setCode(orderCmd.code);
        trade.setDirection(orderCmd.direction.getDirection());
        trade.setPrice(md.price);
        trade.setCount(md.volume);
        trade.setOid(orderId);
        trade.setDate(TimeformatUtil.yyyyMMdd(md.timestamp));
        trade.setTime(TimeformatUtil.hhMMss(md.timestamp));

        tradeRepository.save(trade);
    }
}
