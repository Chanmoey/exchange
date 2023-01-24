package com.moon.exchange.common.fetch;

import com.moon.exchange.common.order.OrderCmd;

import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月23日
 */
public interface IFetchService {

    List<OrderCmd> fetchData();
}
