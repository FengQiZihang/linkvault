package com.linkvault.linkvaultserver.service.impl;

import com.linkvault.linkvaultserver.component.JwtTokenProvider;
import com.linkvault.linkvaultserver.context.CurrentUserInfo;
import com.linkvault.linkvaultserver.context.UserContext;
import com.linkvault.linkvaultserver.vo.auth.AuthSessionVO;
import com.linkvault.linkvaultserver.vo.auth.SendSmsCodeResponseVO;
import com.linkvault.linkvaultserver.exception.BusinessException;
import com.linkvault.linkvaultserver.service.AuthService;
import com.linkvault.linkvaultserver.vo.auth.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int COOLDOWN_SECONDS = 60;
    private static final int EXPIRES_IN_SECONDS = 300;

    private static final String DEV_LOGIN_CODE = "123456";

    private final JwtTokenProvider jwtTokenProvider;


    private final Map<String, UserVO> userStore = new ConcurrentHashMap<>();
    private final AtomicLong userIdGenerator = new AtomicLong(1);


    private final Map<String, Instant> lastSentTimeMap = new ConcurrentHashMap<>();

    @Override
    public SendSmsCodeResponseVO sendSmsCode(String phone) {
        Instant now = Instant.now();
        Instant lastSentTime = lastSentTimeMap.get(phone);

        if (lastSentTime != null && now.isBefore(lastSentTime.plusSeconds(COOLDOWN_SECONDS))) {
            throw new BusinessException(429, 42901, "请求过于频繁");
        }

        lastSentTimeMap.put(phone, now);

        return new SendSmsCodeResponseVO(EXPIRES_IN_SECONDS, COOLDOWN_SECONDS);
    }

    @Override
    public AuthSessionVO login(String phone, String code) {
        if (!DEV_LOGIN_CODE.equals(code)) {
            throw new BusinessException(400, 40001, "验证码错误或已失效");
        }

        UserVO existingUser = userStore.get(phone);
        boolean isNewUser = existingUser == null;

        UserVO user = existingUser;
        if (user == null) {
            String nickname = "用户" + phone.substring(phone.length() - 4);
            user = new UserVO(
                    userIdGenerator.getAndIncrement(),
                    phone,
                    nickname,
                    "/static/avatars/avatar-01.png"
            );
            userStore.put(phone, user);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getPhone());
        return new AuthSessionVO(accessToken, (int) jwtTokenProvider.getAccessTokenExpiresIn(), isNewUser, user);
    }

    @Override
    public UserVO getCurrentUser() {
        CurrentUserInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(401, 40101, "未登录或token无效");
        }

        return userStore.values().stream()
                .filter(user -> user.getUserId().equals(currentUser.getUserId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(401, 40101, "未登录或token无效"));
    }
}
