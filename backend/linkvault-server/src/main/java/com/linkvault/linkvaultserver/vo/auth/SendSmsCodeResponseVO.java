package com.linkvault.linkvaultserver.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendSmsCodeResponseVO {

    private Integer expiresIn; // 验证码有效期，单位秒
    private Integer cooldownSeconds; // 再次发送验证码的冷却时间，单位秒

}
