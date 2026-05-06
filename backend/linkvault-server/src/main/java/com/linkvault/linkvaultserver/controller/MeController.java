package com.linkvault.linkvaultserver.controller;

import com.linkvault.linkvaultserver.common.ApiResponse;
import com.linkvault.linkvaultserver.service.AuthService;
import com.linkvault.linkvaultserver.vo.auth.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MeController {

    private final AuthService authService;

    @GetMapping("/me")
    public ApiResponse<UserVO> me(@RequestHeader("Authorization") String authorization) {
        UserVO user = authService.getCurrentUser();
        return ApiResponse.success(user);
    }
}
