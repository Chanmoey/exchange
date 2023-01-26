package com.moon.exchange.counter.bus.consumer;

import com.google.common.collect.ImmutableMap;
import com.moon.exchange.common.order.OrderCmd;
import com.moon.exchange.common.order.OrderDirection;
import com.moon.exchange.common.order.OrderStatus;
import com.moon.exchange.common.quotation.MatchData;
import com.moon.exchange.counter.config.CounterConfig;
import com.moon.exchange.counter.repository.OrderRepository;
import com.moon.exchange.counter.service.IUserService;
import com.moon.exchange.counter.service.OrderServiceImpl;
import com.moon.exchange.counter.service.PositionServiceImpl;
import com.moon.exchange.counter.service.TradeServiceImpl;
import com.moon.exchange.counter.util.IDConverter;
import com.moon.exchange.counter.util.JsonUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.moon.exchange.counter.config.WebSocketConfig.ORDER_NOTIFY_ADDR_PREFIX;
import static com.moon.exchange.counter.config.WebSocketConfig.TRADE_NOTIFY_ADDR_PREFIX;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
@Log4j2
@Component
public class MatchDataConsumer {
    @Autowired
    private OrderRepository orderRepository;

    public static final String ORDER_DATA_CACHE_ADDR = "order_data_cache_addr";

    @Autowired
    private CounterConfig config;

    @Autowired
    private PositionServiceImpl positionService;

    @Autowired
    private IUserService userService;

    @Autowired
    private TradeServiceImpl tradeService;

    @Autowired
    private OrderServiceImpl orderService;

    /**
     * key: 委托编号, value: OrderCmd>
     */
    private LongObjectHashMap<OrderCmd> oidOrderMap = new LongObjectHashMap<>();

    @PostConstruct
    private void init() {
        EventBus eventBus = config.getVertx().eventBus();
        eventBus.consumer("INNER_MATCH_DATA_ADDR")
                .handler(buffer -> {
                    Buffer body = (Buffer) buffer.body();
                    if (body.length() == 0) {
                        return;
                    }
                    MatchData[] matchDataArr = null;
                    try {
                        matchDataArr = config.getBodyCodec().deserialize(body.getBytes(), MatchData[].class);
                    } catch (Exception e) {
                        log.error(e);
                    }

                    if (matchDataArr == null || ArrayUtils.isEmpty(matchDataArr)) {
                        return;
                    }

                    // 按照oid进行分类
                    Map<Long, List<MatchData>> collect = Arrays.stream(matchDataArr).collect(Collectors.groupingBy(t -> t.oid));

                    for (Map.Entry<Long, List<MatchData>> entry : collect.entrySet()) {
                        if (CollectionUtils.isEmpty(entry.getValue())) {
                            continue;
                        }

                        // 拆分柜台内部编号
                        long oid = entry.getKey();

                        // 这里拿到的是数据库中t_order的主键id
                        int orderId = IDConverter.separateLong2Int(oid)[1];

                        updateAndNotify(orderId, entry.getValue(), oidOrderMap.get(oid));
                    }
                });
    }

    private void updateAndNotify(int orderId, List<MatchData> value, OrderCmd orderCmd) {
        if (CollectionUtils.isEmpty(value)) {
            return;
        }

        // 成交 委托变动
        for (MatchData md : value) {
            OrderStatus status = md.status;
            if (status == OrderStatus.TRADE_ED || status == OrderStatus.PART_TRADE) {
                // 更新数据库
                tradeService.saveTrade(orderId, md, orderCmd);
                // 持仓 资金 多退少补
                if (orderCmd.direction == OrderDirection.BUY) {

                    // 如果成交的价格，低于出价价格，则需要返还资金
                    if (orderCmd.price > md.price) {
                        userService.addBalance(orderCmd.uid, (orderCmd.price - md.price) * md.volume);
                    }

                    positionService.addPosition(orderCmd.uid, orderCmd.code, md.volume, md.price);
                } else if (orderCmd.direction == OrderDirection.SELL) {
                    // 持仓的变化已经在一开始就做处理了，只需要处理资金
                    userService.addBalance(orderCmd.uid, md.price * md.volume);
                } else {
                    log.error("wrong direction[{}]", orderCmd.direction);
                }

                // 通知客户端
                config.getVertx().eventBus()
                        .publish(TRADE_NOTIFY_ADDR_PREFIX + orderCmd.uid,
                                JsonUtil.toJson(
                                        ImmutableMap.of("code", orderCmd.code,
                                                "direction", orderCmd.direction,
                                                "volume", md.volume)
                                ));
            }
        }

        // 委托变动
        // 根据最后一笔Match处理委托
        MatchData finalMatchData = value.get(value.size() - 1);
        OrderStatus finalMatchDataStatus = finalMatchData.status;
        orderService.update(orderCmd.uid, orderId, finalMatchDataStatus);

        // 撤单需要特殊处理
        if (finalMatchDataStatus == OrderStatus.CANCEL_ED || finalMatchDataStatus == OrderStatus.PART_TRADE) {
            oidOrderMap.remove(orderCmd.oid);

            if (orderCmd.direction == OrderDirection.BUY) {
                // 释放冻结的资金，撤单中，volume为负数
                userService.addBalance(orderCmd.uid, -(orderCmd.price * finalMatchData.volume));
            } else if (orderCmd.direction == OrderDirection.SELL) {
                // 释放持仓
                positionService.addPosition(orderCmd.uid, orderCmd.code, -finalMatchData.volume, orderCmd.price);
            } else {
                log.error("wrong direction[{}]", orderCmd.direction);
            }
        }

        // 通知委托终端
        config.getVertx().eventBus()
                .publish(ORDER_NOTIFY_ADDR_PREFIX + orderCmd.uid,
                        "");
    }
}
