package com.moon.exchange.counter.service;

import com.moon.exchange.counter.cache.CacheType;
import com.moon.exchange.counter.cache.RedisStringCache;
import com.moon.exchange.counter.entity.User;
import com.moon.exchange.counter.exception.bussness.LoginException;
import com.moon.exchange.counter.exception.bussness.NoFountException;
import com.moon.exchange.counter.exception.bussness.OrderException;
import com.moon.exchange.counter.repository.UserRepository;
import com.moon.exchange.counter.token.JwtToken;
import com.moon.exchange.counter.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User login(Long uid, String password,
                      String captchaId, String captcha) {

        // 校验验证码
        String cacheCaptcha = RedisStringCache.get(captchaId, CacheType.CAPTCHA);
        if (cacheCaptcha == null || !cacheCaptcha.equalsIgnoreCase(captcha)) {
            throw new LoginException(10001);
        }
        // 删除验证码
        RedisStringCache.remove(captchaId, CacheType.CAPTCHA);

        // 校验账号密码
        User user = userRepository.findByUidAndPassword(uid, password).orElseThrow(
                () -> new LoginException(10002));
        // 更改上次登录时间
        Date last = user.getLastLoginTime();
        user.setLastLoginTime(new Date());
        userRepository.save(user);
        user.setToken(JwtToken.makeToken(user.getUid()));
        user.setLastLoginTime(last);

        // 双token机制：Redis中记录一个空token
        RedisStringCache.cache(String.valueOf(uid), Constant.SECOND_TOKEN, CacheType.ACCOUNT);

        return user;
    }

    @Override
    public void logout(Long uid) {
        // 清除Redis的数据
        RedisStringCache.remove(String.valueOf(uid), CacheType.ACCOUNT);
    }

    @Override
    public void changePassword(Long uid, String oldPassword, String newPassword) {
        User user = userRepository.findByUid(uid).orElseThrow(() -> new LoginException(10005));

        if (oldPassword.equals(newPassword)) {
            throw new LoginException(10007);

        }

        if (!user.getPassword().equals(oldPassword)) {
            throw new LoginException(10006);
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }

    @Override
    public Long getBalance(Long uid) {
        return userRepository.getBalanceByUid(uid)
                .orElseThrow(() -> new LoginException(10008));
    }

    @Override
    public void addBalance(Long uid, long balance) {
        User user = userRepository.findByUid(uid).orElseThrow(() -> new NoFountException(10008));
        long oldMoney = user.getBalance();
        long newMoney = oldMoney + balance;
        if (newMoney < 0) {
            throw new OrderException(30003);
        }
        user.setBalance(newMoney);
        userRepository.save(user);
    }

    @Override
    public void minusBalance(Long uid, long balance) {
        this.addBalance(uid, -balance);
    }
}
