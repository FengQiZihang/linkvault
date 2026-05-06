package com.linkvault.linkvaultserver.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthSessionVO {
    private String accessToken;
    private Integer expiresIn;
    private Boolean isNewUser;
    private UserVO user;
}
