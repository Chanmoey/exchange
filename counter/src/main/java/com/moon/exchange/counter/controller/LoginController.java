package com.moon.exchange.counter.controller;

import com.moon.exchange.common.uuid.OurUuid;
import com.moon.exchange.counter.cache.CacheType;
import com.moon.exchange.counter.cache.RedisStringCache;
import com.moon.exchange.counter.common.UnifyResponse;
import com.moon.exchange.counter.dto.LoginDTO;
import com.moon.exchange.counter.entity.User;
import com.moon.exchange.counter.service.IUserService;
import com.moon.exchange.counter.util.Captcha;
import com.moon.exchange.counter.util.LocalUser;
import com.moon.exchange.counter.vo.CaptchaVO;
import com.moon.exchange.counter.vo.UserVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
@RestController
@RequestMapping("/login")
@Log4j2
public class LoginController {

    @Autowired
    private IUserService userService;

    @GetMapping("/captcha")
    public UnifyResponse<CaptchaVO> getCaptcha() throws IOException {
        // 生成验证码
        Captcha captcha = new Captcha(120, 40, 4, 10);

        // 将验证码放入缓存
        String uuid = String.valueOf(OurUuid.getInstance().getUUID());
        RedisStringCache.cache(uuid, captcha.getCode(), CacheType.CAPTCHA);

        // 转换成base64编码并缓存
        CaptchaVO captchaVO = new CaptchaVO(uuid, captcha.getBase64ByteStr());

        return UnifyResponse.ok(captchaVO);
    }

    @PostMapping("/login")
    public UnifyResponse<UserVO> login(@RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO.getUid(), loginDTO.getPassword(),
                loginDTO.getCaptchaId(), loginDTO.getCaptcha());
        return UnifyResponse.ok(UserVO.copyFromUser(user));
    }

    @PostMapping("/logout")
    public UnifyResponse<Object> logout(HttpServletResponse response) {
        Long uid = LocalUser.getUid();
        this.userService.logout(uid);
        response.setHeader("Authorization", "");
        return UnifyResponse.ok();
    }
}
