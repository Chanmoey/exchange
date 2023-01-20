package com.moon.exchange.counter.service;

import com.moon.exchange.counter.entity.User;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
public interface IUserService {

    User login(Long uid, String password, String captchaId, String captcha);

    void logout(Long uid);

    void changePassword(Long uid, String oldPassword, String newPassword);
}
