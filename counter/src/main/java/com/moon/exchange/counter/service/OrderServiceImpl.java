package com.moon.exchange.counter.service;

import com.moon.exchange.counter.cache.CacheType;
import com.moon.exchange.counter.cache.RedisStringCache;
import com.moon.exchange.counter.entity.Order;
import com.moon.exchange.counter.exception.bussness.NoFountException;
import com.moon.exchange.counter.repository.OrderRepository;
import com.moon.exchange.counter.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月20日
 */
@Service
public class OrderServiceImpl {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StockServiceImpl stockService;

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

    private void setStockName(List<Order> orders) {
        orders.forEach(order -> order.setName(stockService.getNameByCode(order.getCode())));
    }
}
