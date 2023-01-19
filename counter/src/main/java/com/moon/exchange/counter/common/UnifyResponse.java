package com.moon.exchange.counter.common;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Getter
@Setter
public class UnifyResponse<T> {

    private static final int SUCCESS_CODE = 0;
    private static final String SUCCESS_MESSAGE = "SUCCESS";

    /**
     * 错误码
     */
    private int code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 请求路径，只有请求错误，发送异常，才需要把路径返回
     */
    private String request;

    /**
     * 返回数据
     */
    private T data;

    public UnifyResponse(int code, String message, String request, T data) {
        this.code = code;
        this.message = message;
        this.request = request;
        this.data = data;
    }

    public static <T> UnifyResponse<T> ok(T data) {
        return new UnifyResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, null, data);
    }

    public static <T> UnifyResponse<T> ok() {
        return new UnifyResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, null, null);
    }

    public static <T> UnifyResponse<T> ok(String message) {
        return new UnifyResponse<>(SUCCESS_CODE, message, null, null);
    }

    public static <T> UnifyResponse<T> fail(int code, String message, String request) {
        return new UnifyResponse<>(code, message, request, null);
    }
}
