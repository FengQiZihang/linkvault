package com.linkvault.linkvaultserver.service;

import com.linkvault.linkvaultserver.vo.auth.AuthSessionVO;
import com.linkvault.linkvaultserver.vo.auth.SendSmsCodeResponseVO;
import com.linkvault.linkvaultserver.vo.auth.UserVO;

public interface AuthService {

    SendSmsCodeResponseVO sendSmsCode(String phone);

    AuthSessionVO login(String phone, String code);

    UserVO getCurrentUser();
}
