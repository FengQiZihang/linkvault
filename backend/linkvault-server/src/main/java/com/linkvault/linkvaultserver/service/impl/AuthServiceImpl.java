package com.linkvault.linkvaultserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkvault.linkvaultserver.component.JwtTokenProvider;
import com.linkvault.linkvaultserver.context.CurrentUserInfo;
import com.linkvault.linkvaultserver.context.UserContext;
import com.linkvault.linkvaultserver.entity.SmsCodeEntity;
import com.linkvault.linkvaultserver.entity.UserEntity;
import com.linkvault.linkvaultserver.mapper.SmsCodeMapper;
import com.linkvault.linkvaultserver.mapper.UserMapper;
import com.linkvault.linkvaultserver.vo.auth.AuthSessionVO;
import com.linkvault.linkvaultserver.vo.auth.SendSmsCodeResponseVO;
import com.linkvault.linkvaultserver.exception.BusinessException;
import com.linkvault.linkvaultserver.service.AuthService;
import com.linkvault.linkvaultserver.vo.auth.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int COOLDOWN_SECONDS = 60;
    private static final int EXPIRES_IN_SECONDS = 300;

    private static final String DEV_LOGIN_CODE = "123456";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final SmsCodeMapper smsCodeMapper;

    @Override
    public SendSmsCodeResponseVO sendSmsCode(String phone) {
        LocalDateTime now = LocalDateTime.now();

        SmsCodeEntity latest = smsCodeMapper.selectOne(
                new LambdaQueryWrapper<SmsCodeEntity>()
                        .eq(SmsCodeEntity::getPhone, phone)
                        .eq(SmsCodeEntity::getScene, "LOGIN")
                        .orderByDesc(SmsCodeEntity::getCreatedAt)
                        .last("limit 1")
        );

        if (latest != null && latest.getCreatedAt() != null
                && latest.getCreatedAt().plusSeconds(COOLDOWN_SECONDS).isAfter(now)) {
            throw new BusinessException(429, 42901, "请求过于频繁");
        }

        SmsCodeEntity smsCode = new SmsCodeEntity();
        smsCode.setPhone(phone);
        smsCode.setCode("123456");
        smsCode.setScene("LOGIN");
        smsCode.setExpiresAt(now.plusSeconds(EXPIRES_IN_SECONDS));
        smsCode.setFailCount(0);

        smsCodeMapper.insert(smsCode);

        return new SendSmsCodeResponseVO(EXPIRES_IN_SECONDS, COOLDOWN_SECONDS);
    }


    @Override
    public AuthSessionVO login(String phone, String code) {
        LocalDateTime now = LocalDateTime.now();

        SmsCodeEntity smsCode = smsCodeMapper.selectOne(
                new LambdaQueryWrapper<SmsCodeEntity>()
                        .eq(SmsCodeEntity::getPhone, phone)
                        .eq(SmsCodeEntity::getScene, "LOGIN")
                        .isNull(SmsCodeEntity::getUsedAt)
                        .orderByDesc(SmsCodeEntity::getCreatedAt)
                        .last("limit 1")
        );

        if (smsCode == null || smsCode.getExpiresAt().isBefore(now) || !smsCode.getCode().equals(code)) {
            throw new BusinessException(400, 40001, "验证码错误或已失效");
        }

        smsCode.setUsedAt(now);
        smsCodeMapper.updateById(smsCode);

        UserEntity userEntity = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getPhone, phone)
                        .last("limit 1")
        );

        boolean isNewUser = userEntity == null;

        if (userEntity == null) {
            userEntity = new UserEntity();
            userEntity.setPhone(phone);
            userEntity.setNickname("用户" + phone.substring(phone.length() - 4));
            userEntity.setAvatarUrl("/static/avatars/avatar-01.png");
            userEntity.setStatus("ACTIVE");
            userMapper.insert(userEntity);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(userEntity.getId(), userEntity.getPhone());

        UserVO userVO = new UserVO(
                userEntity.getId(),
                userEntity.getPhone(),
                userEntity.getNickname(),
                userEntity.getAvatarUrl()
        );

        return new AuthSessionVO(
                accessToken,
                (int) jwtTokenProvider.getAccessTokenExpiresIn(),
                isNewUser,
                userVO
        );
    }


    @Override
    public UserVO getCurrentUser() {
        CurrentUserInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(401, 40101, "未登录或token无效");
        }

        UserEntity userEntity = userMapper.selectById(currentUser.getUserId());
        if (userEntity == null) {
            throw new BusinessException(401, 40101, "未登录或token无效");
        }

        return new UserVO(
                userEntity.getId(),
                userEntity.getPhone(),
                userEntity.getNickname(),
                userEntity.getAvatarUrl()
        );
    }

}
