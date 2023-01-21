package com.moon.exchange.counter.exception.bussness;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
public class OrderException extends BusinessException{

    public OrderException(int code) {
        this.code = code;
        httpStatusCode = 500;
    }
}
