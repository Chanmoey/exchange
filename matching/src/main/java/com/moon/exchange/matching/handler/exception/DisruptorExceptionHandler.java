package com.moon.exchange.matching.handler.exception;


import com.lmax.disruptor.ExceptionHandler;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.function.BiConsumer;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
@Log4j2
@AllArgsConstructor
public final class DisruptorExceptionHandler<T> implements ExceptionHandler<T> {

    public final String name;
    public final BiConsumer<Throwable, Long> onException;

    @Override
    public void handleEventException(Throwable ex, long sequence, T event) {
        if (log.isDebugEnabled()) {
            log.debug("Disruptor '{}' seq={} caught exception: {}", name, sequence, event, ex);
        }
        onException.accept(ex, sequence);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        if (log.isDebugEnabled()) {
            log.debug("Disruptor '{}' startup exception: {}", name, ex);
        }
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        if (log.isDebugEnabled()) {
            log.debug("Disruptor '{}' shutdown exception: {}", name, ex);
        }
    }
}

