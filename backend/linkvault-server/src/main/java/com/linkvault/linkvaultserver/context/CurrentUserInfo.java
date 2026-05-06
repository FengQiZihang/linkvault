package com.linkvault.linkvaultserver.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CurrentUserInfo {

    private final Long userId;
    private final String phone;

}
