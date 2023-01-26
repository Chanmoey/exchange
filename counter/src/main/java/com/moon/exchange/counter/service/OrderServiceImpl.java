package com.moon.exchange.counter.service;

import com.moon.exchange.common.order.CmdType;
import com.moon.exchange.common.order.OrderCmd;
import com.moon.exchange.common.order.OrderDirection;
import com.moon.exchange.common.order.OrderStatus;
import com.moon.exchange.counter.cache.CacheType;
import com.moon.exchange.counter.cache.RedisStringCache;
import com.moon.exchange.counter.config.CounterConfig;
import com.moon.exchange.counter.config.GatewayConnection;
import com.moon.exchange.counter.entity.Order;
import com.moon.exchange.counter.exception.bussness.NoFountException;
import com.moon.exchange.counter.exception.bussness.OrderException;
import com.moon.exchange.counter.repository.OrderRepository;
import com.moon.exchange.counter.util.IDConverter;
import com.moon.exchange.counter.util.JsonUtil;
import com.moon.exchange.counter.util.TimeformatUtil;
import io.vertx.core.buffer.Buffer;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.moon.exchange.counter.bus.consumer.MatchDataConsumer.ORDER_DATA_CACHE_ADDR;

/**
 * @author Chanmoey
 * @date 2023年01月20日
 */
@Service
@Log4j2
public class OrderServiceImpl {

    @Autowired
    private IUserService userService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StockServiceImpl stockService;

    @Autowired
    private PositionServiceImpl positionService;

    @Autowired
    private CounterConfig config;

    @Autowired
    private GatewayConnection gatewayConnection;

    public List<Order> getOrderList(Long uid) {
        // 先查缓存
        String uidStr = uid.toString();
        String orderStr = RedisStringCache.get(uidStr, CacheType.ORDER);

        if (StringUtils.isEmpty(orderStr)) {
            // 查数据库
            List<Order> dbList = orderRepository.findOrdersByUid(uid)
                    .orElseThrow(() -> new NoFountException(20000));
            // 查询股票名字
            this.setStockName(dbList);

            // 更新缓存
            RedisStringCache.cache(uidStr, JsonUtil.toJson(dbList), CacheType.ORDER);
            return dbList;
        } else {
            return JsonUtil.fromJsonArr(orderStr, Order.class);
        }
    }

    public void saveOrder(OrderCmd orderCmd) {
        // 后端自己设置时间
        long dbTime = System.currentTimeMillis();
        Order order = new Order();
        order.setUid(orderCmd.uid);
        order.setCode(orderCmd.code);
        order.setDirection(orderCmd.direction.getDirection());
        order.setType(orderCmd.type.getType());
        order.setPrice(orderCmd.price);
        order.setCount(orderCmd.volume);
        order.setTradeCount(0L);
        order.setStatus(OrderStatus.NOT_SET.getCode());
        order.setDate(TimeformatUtil.yyyyMMdd(dbTime));
        order.setTime(TimeformatUtil.hhMMss(dbTime));

        orderRepository.save(order);

        if (order.getId() == null) {
            throw new OrderException(30000);
        } else {
            // 调整持仓数据
            if (orderCmd.direction == OrderDirection.BUY) {
                // 扣减用户的金额
                userService.minusBalance(orderCmd.uid,
                        orderCmd.price * orderCmd.volume);
            } else if (orderCmd.direction == OrderDirection.SELL) {
                // 扣减持仓
                positionService.minusPosition(orderCmd.uid, orderCmd.code,
                        orderCmd.volume, orderCmd.price);
            } else {
                log.error("wrong direction[{}], OrderCmd: {}",
                        orderCmd.direction, orderCmd);
                throw new OrderException(30001);
            }

            // 生成全局ID ID = long[对台ID、委托ID]
            orderCmd.oid = IDConverter.combineInt2Long(config.getId(), order.getId());

            // 保存委托到缓存
            byte[] serialize = null;
            try {
                serialize = config.getBodyCodec().serialize(orderCmd);
            } catch (Exception e) {
                log.error(e);
            }
            if (serialize == null) {
                return;
            }

            config.getVertx().eventBus().send(ORDER_DATA_CACHE_ADDR, Buffer.buffer(serialize));

            // 打包委托OrderCmd -> CommonCmd -> TCP数据流，并发送到网关
            gatewayConnection.sendOrder(orderCmd);
            log.info(orderCmd);

            // 删除Redis缓存
            RedisStringCache.remove(Long.toString(orderCmd.uid), CacheType.ORDER);
        }
    }


    private void setStockName(List<Order> orders) {
        orders.forEach(order -> order.setName(stockService.getNameByCode(order.getCode())));
    }

    public void update(long uid, int orderId, OrderStatus finalMatchDataStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderException(30004));
        order.setStatus(finalMatchDataStatus.getCode());
        orderRepository.save(order);

        // 删除Redis缓存
        RedisStringCache.remove(Long.toString(uid), CacheType.ORDER);
    }

    public void cancelOrder(Long uid, int orderId, int code) {
        final OrderCmd orderCmd = OrderCmd.builder()
                .uid(uid)
                .code(code)
                .type(CmdType.CANCEL_ORDER)
                .oid(IDConverter.combineInt2Long(config.getId(), orderId))
                .build();
        log.info("recv cancel order: {}", orderCmd);

        // 撤单我们柜台无能为力，只能通过网关发送到撮合核心去处理
        gatewayConnection.sendOrder(orderCmd);
    }
}
