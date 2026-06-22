package com.linkvault.linkvaultserver.service;

import com.linkvault.linkvaultserver.vo.auth.AuthSessionVO;
import com.linkvault.linkvaultserver.vo.auth.SendSmsCodeResponseVO;
import com.linkvault.linkvaultserver.vo.auth.UserVO;
import com.linkvault.linkvaultserver.vo.auth.UserLibraryStatsVO;

public interface AuthService {

    SendSmsCodeResponseVO sendSmsCode(String phone);

    AuthSessionVO login(String phone, String code);

    UserVO getCurrentUser();

    UserVO updateUserProfile(String nickname, String avatarUrl);

    UserLibraryStatsVO getUserLibraryStats();
}
