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
public class UserVO {
    private Long userId; // 用户ID
    private String phone; // 用户手机号
    private String nickname; // 用户昵称
    private String avatarUrl; // 用户头像路径
}
