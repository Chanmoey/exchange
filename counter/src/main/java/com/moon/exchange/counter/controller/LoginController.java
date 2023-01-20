package com.moon.exchange.counter.controller;

import com.moon.exchange.common.uuid.OurUuid;
import com.moon.exchange.counter.cache.CacheType;
import com.moon.exchange.counter.cache.RedisStringCache;
import com.moon.exchange.counter.common.UnifyResponse;
import com.moon.exchange.counter.dto.ChangePasswordDTO;
import com.moon.exchange.counter.dto.LoginDTO;
import com.moon.exchange.counter.entity.User;
import com.moon.exchange.counter.exception.bussness.LoginException;
import com.moon.exchange.counter.service.IUserService;
import com.moon.exchange.counter.util.Captcha;
import com.moon.exchange.counter.util.LocalUser;
import com.moon.exchange.counter.vo.CaptchaVO;
import com.moon.exchange.counter.vo.UserVO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
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

        // 参数合法性校验
        if (loginDTO.getUid() == null
                || StringUtils.isAnyBlank(loginDTO.getPassword(),
                loginDTO.getCaptchaId(), loginDTO.getCaptcha())) {
            throw new LoginException(10000);
        }

        User user = userService.login(loginDTO.getUid(), loginDTO.getPassword(),
                loginDTO.getCaptchaId(), loginDTO.getCaptcha());
        return UnifyResponse.ok(UserVO.copyFromUser(user));
    }

    @GetMapping("/logout")
    public UnifyResponse<Object> logout() {
        Long uid = LocalUser.getUid();
        this.userService.logout(uid);
        return UnifyResponse.ok("退出成功");
    }

    @PostMapping("/change-password")
    public UnifyResponse<Object> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        Long uid = LocalUser.getUid();
        String oldPassword = changePasswordDTO.getOldPassword();
        String newPassword = changePasswordDTO.getNewPassword();

        if (StringUtils.isAnyBlank(oldPassword, newPassword)) {
            throw new LoginException(1004);
        }
        this.userService.changePassword(uid, oldPassword, newPassword);

        return UnifyResponse.ok("密码修改成功");
    }
}
