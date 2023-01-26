package com.moon.exchange.common.bus;

import com.moon.exchange.common.bean.CommonMsg;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
public interface IBusSender {

    void startUp();

    void publish(CommonMsg commonMsg);
}
