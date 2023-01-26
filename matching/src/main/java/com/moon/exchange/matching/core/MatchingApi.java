package com.moon.exchange.matching.core;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.moon.exchange.common.order.CmdType;
import com.moon.exchange.common.order.OrderCmd;
import com.moon.exchange.matching.event.CmdResultCode;
import com.moon.exchange.matching.event.RbCmd;
import lombok.Getter;

/**
 * @author Chanmoey
 * @date 2023年01月25日
 */
@Getter
public class MatchingApi {

    private final RingBuffer<RbCmd> ringBuffer;

    public MatchingApi(RingBuffer<RbCmd> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * 通过此方法，将委托事件推入Disruptor中*
     */
    public void submitCommand(OrderCmd cmd) {
        switch (cmd.type) {
            case QUOTATION_PUB:
                ringBuffer.publishEvent(QUOTATION_PUB_TRANSLATOR, cmd);
                break;
            case NEW_ORDER:
                ringBuffer.publishEvent(NEW_ORDER_TRANSLATOR, cmd);
                break;
            case CANCEL_ORDER:
                ringBuffer.publishEvent(CANCEL_ORDER_TRANSLATOR, cmd);
                break;
            default:
                throw new IllegalArgumentException("Unsupported cmdType: " + cmd.getClass().getSimpleName());
        }
    }

    /**
     * 委托trans
     */
    private static final EventTranslatorOneArg<RbCmd, OrderCmd> NEW_ORDER_TRANSLATOR = (rbCmd, seq, newOrder) -> {
        rbCmd.command = CmdType.NEW_ORDER;
        rbCmd.timestamp = newOrder.timestamp;
        rbCmd.mid = newOrder.mid;
        rbCmd.uid = newOrder.uid;
        rbCmd.code = newOrder.code;
        rbCmd.direction = newOrder.direction;
        rbCmd.price = newOrder.price;
        rbCmd.volume = newOrder.volume;
        rbCmd.orderType = newOrder.orderType;
        rbCmd.oid = newOrder.oid;
        rbCmd.resultCode = CmdResultCode.SUCCESS;
    };

    /**
     * 撤单trans
     */
    private static final EventTranslatorOneArg<RbCmd, OrderCmd> CANCEL_ORDER_TRANSLATOR = (rbCmd, seq, cancelOrder) -> {
        rbCmd.command = CmdType.CANCEL_ORDER;
        rbCmd.timestamp = cancelOrder.timestamp;
        rbCmd.mid = cancelOrder.mid;
        rbCmd.uid = cancelOrder.uid;
        rbCmd.code = cancelOrder.code;
        rbCmd.oid = cancelOrder.oid;
        rbCmd.resultCode = CmdResultCode.SUCCESS;
    };

    /**
     * 行情发送
     */
    private static final EventTranslatorOneArg<RbCmd, OrderCmd> QUOTATION_PUB_TRANSLATOR = (rbCmd, seq, hqPub) -> {
        rbCmd.command = CmdType.QUOTATION_PUB;
        rbCmd.resultCode = CmdResultCode.SUCCESS;
    };
}
