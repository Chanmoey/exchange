package com.moon.exchange.counter.controller;

import com.moon.exchange.counter.common.UnifyResponse;
import com.moon.exchange.counter.entity.Order;
import com.moon.exchange.counter.entity.Position;
import com.moon.exchange.counter.entity.Trade;
import com.moon.exchange.counter.service.OrderServiceImpl;
import com.moon.exchange.counter.service.PositionServiceImpl;
import com.moon.exchange.counter.service.TradeServiceImpl;
import com.moon.exchange.counter.service.UserServiceImpl;
import com.moon.exchange.counter.util.LocalUser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
