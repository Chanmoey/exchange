package com.moon.exchange.counter.exception.bussness;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
public class LoginException extends BusinessException{

    public LoginException(int code) {
        this.code = code;
        httpStatusCode = 401;
    }
}
