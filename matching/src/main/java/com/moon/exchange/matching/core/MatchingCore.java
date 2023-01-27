package com.moon.exchange.matching.core;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.moon.exchange.common.order.CmdType;
import com.moon.exchange.common.order.OrderCmd;
import com.moon.exchange.matching.event.RbCmd;
import com.moon.exchange.matching.handler.BaseHandler;
import com.moon.exchange.matching.handler.exception.DisruptorExceptionHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;

import java.util.Timer;
import java.util.TimerTask;

import static com.moon.exchange.matching.handler.quotation.L1PubHandler.QUOTATION_RATE;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
@Log4j2
public class MatchingCore {

    private final Disruptor<RbCmd> disruptor;

    @Getter
    private final MatchingApi api;

    public static final int RING_BUFFER_SIZE = 1024;

    public MatchingCore(
            @NonNull final BaseHandler riskHandler,
            @NonNull final BaseHandler matchHandler,
            @NonNull final BaseHandler pubHandler
    ) {
        this.disruptor = new Disruptor<>(
                new RbCmdFactory(),
                RING_BUFFER_SIZE,
                new AffinityThreadFactory("aft_matching_core",
                        AffinityStrategies.ANY),
                ProducerType.SINGLE,
                new BlockingWaitStrategy()
        );

        this.api = new MatchingApi(disruptor.getRingBuffer());

        // 全局异常处理
        final DisruptorExceptionHandler<RbCmd> exceptionHandler =
                new DisruptorExceptionHandler<>(
                        "main",
                        (ex, seq) -> log.error("exception thrown on seq = {}", seq, ex)
                );

        this.disruptor.setDefaultExceptionHandler(exceptionHandler);

        // 绑定事件处理器，前置风控、撮合、发布数据
        disruptor.handleEventsWith(riskHandler)
                .then(matchHandler)
                .then(pubHandler);

        // 启动
        disruptor.start();
        log.info("match engin start");

        // 消费线程：定时任务，定时发布行情
        new Timer().schedule(new QuotationPubTask(), 1000, QUOTATION_RATE);
    }

    private class QuotationPubTask extends TimerTask {

        @Override
        public void run() {
            api.submitCommand(
                    OrderCmd.builder()
                            .type(CmdType.QUOTATION_PUB)
                            .build());
        }
    }
}
