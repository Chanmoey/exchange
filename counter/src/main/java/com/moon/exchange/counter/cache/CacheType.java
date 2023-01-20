package com.moon.exchange.counter.cache;

/**
 * @author Chanmoey
 * @date 2023年01月14日
 */
public enum CacheType {

    CAPTCHA("captcha:"),

    ACCOUNT("account:"),

    ORDER("order:"),

    TRADE("trade:"),

    POSITION("position:");

    private final String type;

    CacheType(String type) {
        this.type = type;
    }

    public String type() {
        return this.type;
    }
}
