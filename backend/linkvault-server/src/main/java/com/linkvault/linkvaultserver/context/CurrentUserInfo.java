package com.linkvault.linkvaultserver.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CurrentUserInfo {

    private final Long userId; // 当前登录用户ID
    private final String phone; // 当前登录用户手机号

}
