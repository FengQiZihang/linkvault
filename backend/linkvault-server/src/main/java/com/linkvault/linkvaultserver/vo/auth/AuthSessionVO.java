package com.linkvault.linkvaultserver.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthSessionVO {
    private String accessToken; // JWT访问令牌
    private Integer expiresIn; // accessToken有效期，单位秒
    private Boolean isNewUser; // 是否为本次登录自动创建的新用户
    private UserVO user; // 当前登录用户信息
}
