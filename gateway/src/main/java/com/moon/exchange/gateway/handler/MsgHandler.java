package com.moon.exchange.gateway.handler;

import com.moon.exchange.common.bean.CommonMsg;
import com.moon.exchange.common.codec.IBodyCodec;
import com.moon.exchange.common.order.OrderCmd;
import com.moon.exchange.gateway.cache.OrderCmdContainer;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
@Log4j2
@AllArgsConstructor
public class MsgHandler implements IMsgHandler {

    private IBodyCodec bodyCodec;


    @Override
    public void onCounterData(CommonMsg msg) {
        OrderCmd orderCmd;
        try {
            orderCmd = bodyCodec.deserialize(msg.getBody(), OrderCmd.class);

            // TODO: 开发时，打印结果看看，生产环境需要去掉
            log.info("recv cmd: {}", orderCmd);

            if (!OrderCmdContainer.getInstance().cache(orderCmd)) {
                log.error("gateway queue insert fail, queue length: {}, order: {}",
                        OrderCmdContainer.getInstance().size(), orderCmd);
            }
        } catch (Exception e) {
            log.error("decode order cmd error", e);
        }
    }
}
