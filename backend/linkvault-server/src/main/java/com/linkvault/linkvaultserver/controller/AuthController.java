package com.linkvault.linkvaultserver.controller;

import com.linkvault.linkvaultserver.common.ApiResponse;
import com.linkvault.linkvaultserver.dto.auth.LoginRequest;
import com.linkvault.linkvaultserver.dto.auth.SendSmsCodeRequest;
import com.linkvault.linkvaultserver.vo.auth.AuthSessionVO;
import com.linkvault.linkvaultserver.vo.auth.SendSmsCodeResponseVO;
import com.linkvault.linkvaultserver.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // 鉴权与用户业务服务

    @PostMapping("/sms-code")
    public ApiResponse<SendSmsCodeResponseVO> sendSmsCode(@Valid @RequestBody SendSmsCodeRequest request) {
        log.info("发送短信验证码接口调用，phone={}", request.getPhone());
        SendSmsCodeResponseVO response = authService.sendSmsCode(request.getPhone());
        log.info("发送短信验证码接口完成，phone={}, expiresIn={}, cooldownSeconds={}",
                request.getPhone(), response.getExpiresIn(), response.getCooldownSeconds());
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ApiResponse<AuthSessionVO> login(@Valid @RequestBody LoginRequest request) {
        log.info("手机号验证码登录接口调用，phone={}", request.getPhone());
        AuthSessionVO response = authService.login(request.getPhone(), request.getCode());
        log.info("手机号验证码登录接口完成，phone={}, userId={}, isNewUser={}",
                request.getPhone(), response.getUser().getUserId(), response.getIsNewUser());
        return ApiResponse.success(response);
    }

}
