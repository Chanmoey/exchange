package com.moon.exchange.counter.bus.consumer;

import com.moon.exchange.common.quotation.L1MarketData;
import com.moon.exchange.counter.config.CounterConfig;
import com.moon.exchange.counter.util.JsonUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.moon.exchange.counter.bus.MqttBusConsumer.INNER_MARKET_DATA_CACHE_ADDR;
import static com.moon.exchange.counter.config.WebSocketConfig.L1_MARKET_DATA_PREFIX;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
@Log4j2
@Component
public class MarketDataConsumer {

    @Autowired
    private CounterConfig config;

    private IntObjectHashMap<L1MarketData> l1Cache = new IntObjectHashMap<>();

    @PostConstruct
    private void init() {
        // 订阅数据
        EventBus eventBus = config.getVertx().eventBus();
        // 处理核心发过来的行情
        eventBus.consumer(INNER_MARKET_DATA_CACHE_ADDR)
                .handler(buffer -> {
                    Buffer body = (Buffer) buffer.body();
                    if (body.length() == 0) {
                        return;
                    }

                    L1MarketData[] marketData = null;

                    try {
                        marketData = config.getBodyCodec().deserialize(body.getBytes(), L1MarketData[].class);
                    } catch (Exception e) {
                        log.error(e);
                    }

                    if (marketData == null || ArrayUtils.isEmpty(marketData)) {
                        return;
                    }

                    for (L1MarketData md : marketData) {
                        L1MarketData l1MarketData = l1Cache.get(md.code);
                        if (l1MarketData == null || l1MarketData.timestamp < md.timestamp) {
                            l1Cache.put(md.code, md);
                        } else {
                            log.error("l1MarketData is not null and l1MarketData.timestamp > md.timestamp");
                        }
                    }
                });

        // 委托终端的行情处理器
        eventBus.consumer(L1_MARKET_DATA_PREFIX)
                .handler(h -> {
                    int code = Integer.parseInt(h.headers().get("code"));
                    L1MarketData data = l1Cache.get(code);
                    h.reply(JsonUtil.toJson(data));
                });
    }
}
