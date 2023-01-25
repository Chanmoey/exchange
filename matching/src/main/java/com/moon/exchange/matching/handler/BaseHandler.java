package com.moon.exchange.matching.handler;


import com.lmax.disruptor.EventHandler;
import com.moon.exchange.matching.event.RbCmd;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
public abstract class BaseHandler implements EventHandler<RbCmd> {

}
