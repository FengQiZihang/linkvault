package com.linkvault.linkvaultserver.controller;

import com.linkvault.linkvaultserver.common.ApiResponse;
import com.linkvault.linkvaultserver.dto.auth.LoginRequest;
import com.linkvault.linkvaultserver.dto.auth.SendSmsCodeRequest;
import com.linkvault.linkvaultserver.vo.auth.AuthSessionVO;
import com.linkvault.linkvaultserver.vo.auth.SendSmsCodeResponseVO;
import com.linkvault.linkvaultserver.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sms-code")
    public ApiResponse<SendSmsCodeResponseVO> sendSmsCode(@Valid @RequestBody SendSmsCodeRequest request) {
        SendSmsCodeResponseVO response = authService.sendSmsCode(request.getPhone());
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ApiResponse<AuthSessionVO> login(@Valid @RequestBody LoginRequest request) {
        AuthSessionVO response = authService.login(request.getPhone(), request.getCode());
        return ApiResponse.success(response);
    }

}
