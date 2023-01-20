package com.moon.exchange.counter.exception.bussness;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
public class NoFountException extends BusinessException{

    public NoFountException(int code) {
        this.code = code;
        httpStatusCode = 404;
    }
}
