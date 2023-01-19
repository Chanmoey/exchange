package com.moon.exchange.counter.interceptor;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.moon.exchange.counter.cache.CacheType;
import com.moon.exchange.counter.cache.RedisStringCache;
import com.moon.exchange.counter.exception.bussness.LoginException;
import com.moon.exchange.counter.token.JwtToken;
import com.moon.exchange.counter.util.Constant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

import java.util.Map;
import java.util.Optional;

/**
 * @author Chanmoey
 * @date 2023年01月19日
 */
@Component
public class LoginInterceptor extends WebRequestHandlerInterceptorAdapter {


    public LoginInterceptor(WebRequestInterceptor requestInterceptor) {
        super(requestInterceptor);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String requestUrl = request.getRequestURI();
        System.out.println(requestUrl);
        if (UrlWriteList.isInWriteList(requestUrl)) {
            return true;
        }

        String token = request.getHeader("token");

        // 是否携带token
        if (StringUtils.isBlank(token)) {
            throw new LoginException(10003);
        }

        try {
            return JwtToken.verifyToken(token);
        } catch (TokenExpiredException e) {
            // 如果token过期了，则看看第二token有没有过期
            Optional<Map<String, Claim>> optionalMap = JwtToken.getClaims(token);
            Map<String, Claim> map = optionalMap
                    .orElseThrow(() -> new LoginException(10003));

            // 获取用户uid
            Long uid = map.get("uid").asLong();

            // Redis中获取第二个token
            String otherToken = RedisStringCache.get(String.valueOf(uid), CacheType.ACCOUNT);
            if (!Constant.SECOND_TOKEN.equals(otherToken)) {
                throw new LoginException(10003);
            }

            // 有第二个token，获取新token
            String newToken = JwtToken.makeToken(uid);
            response.setHeader("token", newToken);

            // 更新redis
            RedisStringCache.cache(String.valueOf(uid), Constant.SECOND_TOKEN, CacheType.ACCOUNT);

            return true;
        }
    }
}
