package com.linkvault.linkvaultserver.controller;

import com.linkvault.linkvaultserver.common.ApiResponse;
import com.linkvault.linkvaultserver.service.AuthService;
import com.linkvault.linkvaultserver.vo.auth.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MeController {

    private final AuthService authService; // 当前用户相关业务服务

    @GetMapping("/me")
    public ApiResponse<UserVO> me(@RequestHeader("Authorization") String authorization) {
        log.info("获取当前用户接口调用");
        UserVO user = authService.getCurrentUser();
        log.info("获取当前用户接口完成，userId={}, phone={}", user.getUserId(), user.getPhone());
        return ApiResponse.success(user);
    }
}
