package com.moon.exchange.counter.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Chanmoey
 * @date 2023年01月19日
 */
public class LocalUser {

    private LocalUser(){}

    private static final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    public static void set(Long uid) {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        LocalUser.threadLocal.set(map);
    }

    public static void clear() {
        LocalUser.threadLocal.remove();
    }

    public static Long getUid() {
        Map<String, Object> map = LocalUser.threadLocal.get();
        return (Long) map.get("uid");
    }
}
