package com.moon.exchange.counter.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author Chanmoey
 * @date 2023年01月14日
 */
@Component
@PropertySource(value = "classpath:config/security.properties")
public class RedisStringCache {

    private static RedisStringCache redisStringCache;

    private RedisStringCache() {
    }

    @Value("${security.captcha-expired-time}")
    private int captchaExpireTime;

    @Value("${security.account-expired-time}")
    private int accountExpireTime;

    @Value("${security.order-expired-time}")
    private int orderExpireTime;

    public int getCaptchaExpireTime() {
        return captchaExpireTime;
    }

    public void setCaptchaExpireTime(int captchaExpireTime) {
        this.captchaExpireTime = captchaExpireTime;
    }

    public int getAccountExpireTime() {
        return accountExpireTime;
    }

    public void setAccountExpireTime(int accountExpireTime) {
        this.accountExpireTime = accountExpireTime;
    }

    public int getOrderExpireTime() {
        return orderExpireTime;
    }

    public void setOrderExpireTime(int orderExpireTime) {
        this.orderExpireTime = orderExpireTime;
    }

    public StringRedisTemplate getTemplate() {
        return template;
    }

    public void setTemplate(StringRedisTemplate template) {
        this.template = template;
    }

    @Autowired
    private StringRedisTemplate template;

    @PostConstruct
    private void init() {
        redisStringCache = new RedisStringCache();
        redisStringCache.setTemplate(template);
        redisStringCache.setCaptchaExpireTime(captchaExpireTime);
        redisStringCache.setAccountExpireTime(accountExpireTime);
        redisStringCache.setOrderExpireTime(orderExpireTime);
    }

    public static void cache(String key, String value, CacheType cacheType) {

        int expireTime;
        switch (cacheType) {
            case ACCOUNT:
                expireTime = redisStringCache.getAccountExpireTime();
                break;
            case CAPTCHA:
                expireTime = redisStringCache.getCaptchaExpireTime();
                break;
            case ORDER:
            case TRADE:
            case POSITION:
                expireTime = redisStringCache.getOrderExpireTime();
                break;
            default:
                expireTime = 10;
                break;
        }

        redisStringCache.getTemplate()
                .opsForValue()
                .set(cacheType.type() + key, value, expireTime, TimeUnit.SECONDS);
    }

    public static String get(String key, CacheType cacheType) {
        return redisStringCache.getTemplate().opsForValue().get(cacheType.type() + key);
    }

    public static void remove(String key, CacheType cacheType) {
        redisStringCache.getTemplate().delete(cacheType.type() + key);
    }
}
