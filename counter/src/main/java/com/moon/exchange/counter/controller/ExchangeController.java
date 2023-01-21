package com.moon.exchange.counter.controller;

import com.moon.exchange.common.order.CmdType;
import com.moon.exchange.common.order.OrderCmd;
import com.moon.exchange.common.order.OrderDirection;
import com.moon.exchange.common.order.OrderType;
import com.moon.exchange.counter.cache.StockCache;
import com.moon.exchange.counter.common.UnifyResponse;
import com.moon.exchange.counter.config.SecurityConfig;
import com.moon.exchange.counter.dto.OrderDTO;
import com.moon.exchange.counter.entity.Order;
import com.moon.exchange.counter.entity.Position;
import com.moon.exchange.counter.entity.Stock;
import com.moon.exchange.counter.entity.Trade;
import com.moon.exchange.counter.service.OrderServiceImpl;
import com.moon.exchange.counter.service.PositionServiceImpl;
import com.moon.exchange.counter.service.TradeServiceImpl;
import com.moon.exchange.counter.service.UserServiceImpl;
import com.moon.exchange.counter.util.LocalUser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 关于金钱、交易、委托等相关的api接口
 *
 * @author Chanmoey
 * @date 2023年01月21日
 */
@RestController
@RequestMapping("/api")
@Log4j2
public class ExchangeController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private TradeServiceImpl tradeService;

    @Autowired
    private PositionServiceImpl positionService;

    @Autowired
    private StockCache stockCache;

    @Autowired
    private SecurityConfig config;

    @GetMapping("/balance")
    public UnifyResponse<Long> getBalance() {
        Long uid = LocalUser.getUid();

        Long balance = userService.getBalance(uid);
        return UnifyResponse.ok(balance);
    }

    @GetMapping("/position")
    public UnifyResponse<List<Position>> getPosition() {
        Long uid = LocalUser.getUid();

        List<Position> positions = positionService.getPositionList(uid);
        return UnifyResponse.ok(positions);
    }

    @GetMapping("/order")
    public UnifyResponse<List<Order>> getOrder() {
        Long uid = LocalUser.getUid();

        List<Order> orders = orderService.getOrderList(uid);
        return UnifyResponse.ok(orders);
    }

    @GetMapping("/trade")
    public UnifyResponse<List<Trade>> getTrade() {
        Long uid = LocalUser.getUid();

        List<Trade> trades = tradeService.getTradeList(uid);
        return UnifyResponse.ok(trades);
    }

    @GetMapping("/stock")
    public UnifyResponse<Collection<Stock>> getStocks(@RequestParam String key) {
        Collection<Stock> stocks = stockCache.getStocks(key);
        return UnifyResponse.ok(stocks);
    }

    @PostMapping("/send-order")
    public UnifyResponse<Objects> order(@RequestBody OrderDTO orderDTO) {

        OrderCmd cmd = OrderCmd.builder()
                .type(CmdType.of(orderDTO.getType()))
                .timestamp(orderDTO.getTimestamp())
                .mid(config.getId())
                .uid(LocalUser.getUid())
                .code(orderDTO.getCode())
                .direction(OrderDirection.of(orderDTO.getDirection()))
                .price(orderDTO.getPrice())
                .volume(orderDTO.getVolume())
                .orderType(OrderType.of(orderDTO.getOrderType()))
                .build();

        this.orderService.saveOrder(cmd);
        return UnifyResponse.ok("您的委托提交成功");

    }
}
