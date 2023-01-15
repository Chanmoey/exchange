package com.moon.exchange.counter.controller;

import com.moon.exchange.common.uuid.OurUuid;
import com.moon.exchange.counter.cache.CacheType;
import com.moon.exchange.counter.cache.RedisStringCache;
import com.moon.exchange.counter.common.UnifyResponse;
import com.moon.exchange.counter.util.Captcha;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
@RestController
@RequestMapping("/login")
@Log4j2
public class LoginController {

    @GetMapping("/captcha")
    public UnifyResponse<Captcha> getCaptcha() {
        // 生成验证码
        Captcha captcha = new Captcha(120, 40, 4, 10);

        // 将验证码放入缓存
        String uuid = String.valueOf(OurUuid.getInstance().getUUID());
        RedisStringCache.cache(uuid, captcha.getCode(), CacheType.CAPTCHA);

        // 转换成base64编码并缓存

    }
}
