package com.moon.exchange.counter.vo;

import com.moon.exchange.counter.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
@Getter
@Setter
@NoArgsConstructor
public class UserVO {

    private Long uid;

    private String token;

    public static UserVO copyFromUser(User user) {
        UserVO vo = new UserVO();
        vo.setUid(user.getUid());
        vo.setToken(user.getToken());
        return vo;
    }
}
