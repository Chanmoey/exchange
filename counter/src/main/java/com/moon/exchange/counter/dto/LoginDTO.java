package com.moon.exchange.counter.dto;

import lombok.*;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginDTO {

    private Long uid;

    private String password;

    private String captchaId;

    private String captcha;
}
