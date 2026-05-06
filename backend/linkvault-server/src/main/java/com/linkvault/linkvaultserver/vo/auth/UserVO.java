package com.linkvault.linkvaultserver.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Long userId;
    private String phone;
    private String nickname;
    private String avatarUrl;
}
