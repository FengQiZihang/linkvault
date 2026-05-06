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

    private Integer expiresIn;
    private Integer cooldownSeconds;

}
