package com.linkvault.linkvaultserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkvault.linkvaultserver.common.TimeUtils;
import com.linkvault.linkvaultserver.component.JwtTokenProvider;
import com.linkvault.linkvaultserver.context.CurrentUserInfo;
import com.linkvault.linkvaultserver.context.UserContext;
import com.linkvault.linkvaultserver.entity.SmsCodeEntity;
import com.linkvault.linkvaultserver.entity.UserEntity;
import com.linkvault.linkvaultserver.exception.ErrorCode;
import com.linkvault.linkvaultserver.mapper.SmsCodeMapper;
import com.linkvault.linkvaultserver.mapper.UserMapper;
import com.linkvault.linkvaultserver.vo.auth.AuthSessionVO;
import com.linkvault.linkvaultserver.vo.auth.SendSmsCodeResponseVO;
import com.linkvault.linkvaultserver.exception.BusinessException;
import com.linkvault.linkvaultserver.service.AuthService;
import com.linkvault.linkvaultserver.vo.auth.UserVO;
import com.linkvault.linkvaultserver.vo.auth.UserLibraryStatsVO;
import com.linkvault.linkvaultserver.mapper.BookmarkMapper;
import com.linkvault.linkvaultserver.mapper.TagMapper;
import com.linkvault.linkvaultserver.entity.BookmarkEntity;
import com.linkvault.linkvaultserver.entity.TagEntity;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int COOLDOWN_SECONDS = 60; // 同手机号再次发送验证码的冷却秒数
    private static final int EXPIRES_IN_SECONDS = 300; // 验证码有效期秒数
    private static final String DEV_SMS_CODE = "123456"; // 开发阶段固定验证码
    private static final int MAX_FAIL_COUNT = 5; // 单条验证码最大校验失败次数
    private static final String LOGIN_SCENE = "LOGIN"; // 登录验证码场景标识
    private static final String DEFAULT_AVATAR_URL = "/static/avatars/avatar-01.png"; // 新用户默认头像路径
    private static final String ACTIVE_STATUS = "ACTIVE"; // 新用户默认启用状态
    
    private static final Set<String> AVATAR_WHITE_LIST = Set.of(
            "/static/avatars/avatar-01.png", "/static/avatars/avatar-02.png", "/static/avatars/avatar-03.png",
            "/static/avatars/avatar-04.png", "/static/avatars/avatar-05.png", "/static/avatars/avatar-06.png",
            "/static/avatars/avatar-07.png", "/static/avatars/avatar-08.png", "/static/avatars/avatar-09.png"
    );

    private final JwtTokenProvider jwtTokenProvider; // JWT生成组件
    private final UserMapper userMapper; // 用户表数据访问对象
    private final SmsCodeMapper smsCodeMapper; // 短信验证码表数据访问对象
    private final BookmarkMapper bookmarkMapper;
    private final TagMapper tagMapper;

    @Override
    public SendSmsCodeResponseVO sendSmsCode(String phone) {
        LocalDateTime now = TimeUtils.nowUtc();

        // 1、查询最近一次验证码并校验发送冷却
        SmsCodeEntity latest = findLatestSmsCode(phone);
        checkSmsCooldown(latest, now);

        // 2、生成验证码记录并写入数据库
        SmsCodeEntity smsCode = buildSmsCodeEntity(phone, now);
        smsCodeMapper.insert(smsCode);
        log.info("短信验证码发送完成，phone={}, smsCodeId={}, expiresAt={}",
                phone, smsCode.getId(), smsCode.getExpiresAt());

        // 3、返回验证码有效期和再次发送冷却时间
        return new SendSmsCodeResponseVO(EXPIRES_IN_SECONDS, COOLDOWN_SECONDS);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    @Override
    public AuthSessionVO login(String phone, String code) {
        LocalDateTime now = TimeUtils.nowUtc();

        // 1、查询可用验证码并校验
        SmsCodeEntity smsCode = findLatestAvailableSmsCode(phone);
        validateSmsCode(smsCode, code, now);

        // 2、标记验证码已使用，避免重复登录
        markSmsCodeUsed(smsCode, now);

        // 3、查询用户，未注册则创建新用户，已注册则更新最近登录时间
        UserEntity existingUser = findUserByPhone(phone);
        boolean isNewUser = existingUser == null;
        UserEntity userEntity = saveOrUpdateLoginUser(phone, now, existingUser);

        // 4、生成登录态并返回给前端
        AuthSessionVO authSession = buildAuthSession(userEntity, isNewUser);
        log.info("手机号验证码登录完成，phone={}, userId={}, isNewUser={}",
                phone, userEntity.getId(), isNewUser);
        return authSession;
    }


    @Override
    public UserVO getCurrentUser() {
        // 1、从请求线程上下文中获取当前登录用户
        CurrentUserInfo currentUser = getRequiredCurrentUser();

        // 2、根据用户ID查询数据库中的用户信息
        UserEntity userEntity = getRequiredUserById(currentUser.getUserId());

        // 3、转换为前端需要的用户VO
        log.info("获取当前用户完成，userId={}, phone={}", userEntity.getId(), userEntity.getPhone());
        return toUserVO(userEntity);
    }

    private CurrentUserInfo getRequiredCurrentUser() {
        CurrentUserInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return currentUser;
    }

    private UserEntity getRequiredUserById(Long userId) {
        UserEntity userEntity = userMapper.selectById(userId);
        if (userEntity == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userEntity;
    }

    private SmsCodeEntity findLatestSmsCode(String phone) {
        return smsCodeMapper.selectOne(
                new LambdaQueryWrapper<SmsCodeEntity>()
                        .eq(SmsCodeEntity::getPhone, phone)
                        .eq(SmsCodeEntity::getScene, LOGIN_SCENE)
                        .orderByDesc(SmsCodeEntity::getCreatedAt)
                        .last("limit 1")
        );
    }

    private SmsCodeEntity findLatestAvailableSmsCode(String phone) {
        return smsCodeMapper.selectOne(
                new LambdaQueryWrapper<SmsCodeEntity>()
                        .eq(SmsCodeEntity::getPhone, phone)
                        .eq(SmsCodeEntity::getScene, LOGIN_SCENE)
                        .isNull(SmsCodeEntity::getUsedAt)
                        .orderByDesc(SmsCodeEntity::getCreatedAt)
                        .last("limit 1")
        );
    }

    private void checkSmsCooldown(SmsCodeEntity latest, LocalDateTime now) {
        if (latest != null && latest.getCreatedAt() != null
                && latest.getCreatedAt().plusSeconds(COOLDOWN_SECONDS).isAfter(now)) {
            throw new BusinessException(ErrorCode.SMS_COOLDOWN);
        }
    }

    private SmsCodeEntity buildSmsCodeEntity(String phone, LocalDateTime now) {
        return SmsCodeEntity.builder()
                .phone(phone)
                .code(DEV_SMS_CODE)
                .scene(LOGIN_SCENE)
                .expiresAt(now.plusSeconds(EXPIRES_IN_SECONDS))
                .failCount(0)
                .build();
    }

    private void validateSmsCode(SmsCodeEntity smsCode, String code, LocalDateTime now) {
        if (smsCode == null) {
            throw new BusinessException(ErrorCode.SMS_INVALID);
        }

        if (smsCode.getExpiresAt() == null || smsCode.getExpiresAt().isBefore(now)) {
            throw new BusinessException(ErrorCode.SMS_INVALID);
        }

        Integer currentFailCount = smsCode.getFailCount() == null ? 0 : smsCode.getFailCount();
        if (currentFailCount >= MAX_FAIL_COUNT) {
            throw new BusinessException(ErrorCode.SMS_INVALID);
        }

        if (!smsCode.getCode().equals(code)) {
            smsCode.setFailCount(currentFailCount + 1);
            smsCodeMapper.updateById(smsCode);
            throw new BusinessException(ErrorCode.SMS_INVALID);
        }
    }

    private void markSmsCodeUsed(SmsCodeEntity smsCode, LocalDateTime now) {
        smsCode.setUsedAt(now);
        smsCodeMapper.updateById(smsCode);
    }

    private UserEntity findUserByPhone(String phone) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getPhone, phone)
                        .last("limit 1")
        );
    }

    private UserEntity saveOrUpdateLoginUser(String phone, LocalDateTime now, UserEntity existingUser) {
        if (existingUser == null) {
            UserEntity newUser = UserEntity.builder()
                    .phone(phone)
                    .nickname("用户" + phone.substring(phone.length() - 4))
                    .avatarUrl(DEFAULT_AVATAR_URL)
                    .status(ACTIVE_STATUS)
                    .lastLoginAt(now)
                    .build();
            userMapper.insert(newUser);
            return newUser;
        }

        existingUser.setLastLoginAt(now);
        userMapper.updateById(existingUser);
        return existingUser;
    }

    private AuthSessionVO buildAuthSession(UserEntity userEntity, boolean isNewUser) {
        String accessToken = jwtTokenProvider.generateAccessToken(userEntity.getId(), userEntity.getPhone());

        return AuthSessionVO.builder()
                .accessToken(accessToken)
                .expiresIn((int) jwtTokenProvider.getAccessTokenExpiresIn())
                .isNewUser(isNewUser)
                .user(toUserVO(userEntity))
                .build();
    }

    private UserVO toUserVO(UserEntity userEntity) {
        return UserVO.builder()
                .userId(userEntity.getId())
                .phone(userEntity.getPhone())
                .nickname(userEntity.getNickname())
                .avatarUrl(userEntity.getAvatarUrl())
                .build();
    }

    @Transactional
    @Override
    public UserVO updateUserProfile(String nickname, String avatarUrl) {
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        
        if (nickname == null || nickname.trim().isBlank() || nickname.length() > 50) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        
        String normalizedAvatar = avatarUrl;
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            if (!AVATAR_WHITE_LIST.contains(avatarUrl)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }
        } else {
            normalizedAvatar = DEFAULT_AVATAR_URL;
        }

        UserEntity user = getRequiredUserById(currentUser.getUserId());
        user.setNickname(nickname.trim());
        user.setAvatarUrl(normalizedAvatar);
        user.setUpdatedAt(TimeUtils.nowUtc());
        userMapper.updateById(user);

        log.info("更新当前用户资料成功，userId={}, nickname={}, avatarUrl={}", 
                user.getId(), user.getNickname(), user.getAvatarUrl());
        return toUserVO(user);
    }

    @Override
    public UserLibraryStatsVO getUserLibraryStats() {
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Long userId = currentUser.getUserId();

        Long totalBookmarkCount = bookmarkMapper.selectCount(
                new LambdaQueryWrapper<BookmarkEntity>()
                        .eq(BookmarkEntity::getUserId, userId)
        );

        Long tagCount = tagMapper.selectCount(
                new LambdaQueryWrapper<TagEntity>()
                        .eq(TagEntity::getUserId, userId)
        );

        Long untaggedBookmarkCount = bookmarkMapper.selectCount(
                new LambdaQueryWrapper<BookmarkEntity>()
                        .eq(BookmarkEntity::getUserId, userId)
                        .apply("id NOT IN (select bookmark_id from lv_bookmark_tag where user_id = {0})", userId)
        );

        return UserLibraryStatsVO.builder()
                .totalBookmarkCount(totalBookmarkCount)
                .tagCount(tagCount)
                .untaggedBookmarkCount(untaggedBookmarkCount)
                .build();
    }
}
