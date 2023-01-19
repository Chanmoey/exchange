package com.moon.exchange.counter.interceptor;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.moon.exchange.counter.cache.CacheType;
import com.moon.exchange.counter.cache.RedisStringCache;
import com.moon.exchange.counter.exception.bussness.LoginException;
import com.moon.exchange.counter.token.JwtToken;
import com.moon.exchange.counter.util.Constant;
import com.moon.exchange.counter.util.LocalUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Optional;

/**
 * @author Chanmoey
 * @date 2023年01月19日
 */
public class LoginInterceptor implements HandlerInterceptor {

    public LoginInterceptor() {

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String requestUrl = request.getRequestURI();
        if (UrlWriteList.isInWriteList(requestUrl)) {
            return true;
        }

        String token = request.getHeader("Authorization");

        // 是否携带token
        if (StringUtils.isBlank(token)) {
            throw new LoginException(10003);
        }

        try {
            boolean valid = JwtToken.verifyToken(token);
            if (!valid) {
                // 验证未通过
                return false;
            }

            // 验证通过，LocalUser保存用户信息
            LocalUser.set(this.getUid(token));
            return true;
        } catch (TokenExpiredException e) {
            // 获取用户uid
            Long uid = this.getUid(token);

            // Redis中获取第二个token
            String otherToken = RedisStringCache.get(String.valueOf(uid), CacheType.ACCOUNT);
            if (!Constant.SECOND_TOKEN.equals(otherToken)) {
                throw new LoginException(10003);
            }

            // 有第二个token，获取新token
            String newToken = JwtToken.makeToken(uid);
            response.setHeader("Authorization", newToken);

            // 更新redis
            RedisStringCache.cache(String.valueOf(uid), Constant.SECOND_TOKEN, CacheType.ACCOUNT);

            // 验证通过，LocalUser保存用户信息
            LocalUser.set(this.getUid(token));
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求用完，要清空数据，避免内存泄露
        LocalUser.clear();
    }

    private Long getUid(String token) {
        // 如果token过期了，则看看第二token有没有过期
        Optional<Map<String, Claim>> optionalMap = JwtToken.getClaims(token);
        Map<String, Claim> map = optionalMap
                .orElseThrow(() -> new LoginException(10003));

        // 获取用户uid
        return map.get("uid").asLong();
    }
}
