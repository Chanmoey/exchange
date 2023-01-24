package com.moon.exchange.counter.config;

import com.moon.exchange.common.bean.CommonMsg;
import com.moon.exchange.common.order.OrderCmd;
import com.moon.exchange.common.tcp.TcpDirectSender;
import com.moon.exchange.common.uuid.OurUuid;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import static com.moon.exchange.common.bean.MsgConstants.COUNTER_NEW_ORDER;
import static com.moon.exchange.common.bean.MsgConstants.NORMAL;

/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
@Log4j2
@Configuration
public class GatewayConnection {

    @Autowired
    private CounterConfig config;

    private TcpDirectSender directSender;

    @PostConstruct
    private void init() {
        directSender = new TcpDirectSender(config.getSendIp(), config.getSendPort(),
                config.getVertx());
        directSender.startUp();
    }

    public void sendOrder(OrderCmd orderCmd) {
        byte[] data;

        try {
            data = config.getBodyCodec().serialize(orderCmd);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("encode error for OrderCmd: {}", orderCmd);
            return;
        }

        CommonMsg msg = CommonMsg.createCommonMsg(data,
                config.getCheckSum().getCheckSum(data),
                config.getId(), config.getGatewayId(),
                COUNTER_NEW_ORDER, NORMAL, OurUuid.getInstance().getUUID());

        directSender.send(config.getMsgCodec().encodeToBuffer(msg));
    }
}
