package com.moon.exchange.counter.interceptor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Chanmoey
 * @date 2023年01月19日
 */
public class UrlWriteList {

    private UrlWriteList() {}

    private static final Set<String> URL_WRITE_LIST;

    static {
        URL_WRITE_LIST = new HashSet<>();
        URL_WRITE_LIST.add("/login/login");
        URL_WRITE_LIST.add("/login/captcha");
    }

    public static boolean isInWriteList(String requestUrl) {
        return URL_WRITE_LIST.contains(requestUrl);
    }
}
