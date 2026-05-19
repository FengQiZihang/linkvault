package com.linkvault.linkvaultserver.interceptor;

import com.linkvault.linkvaultserver.component.JwtTokenProvider;
import com.linkvault.linkvaultserver.context.CurrentUserInfo;
import com.linkvault.linkvaultserver.context.UserContext;
import com.linkvault.linkvaultserver.exception.BusinessException;
import com.linkvault.linkvaultserver.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider; // JWT生成和解析组件

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1、读取Authorization请求头并校验Bearer token格式
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.info("鉴权失败，缺少或非法 Authorization 请求头，method={}, uri={}",
                    request.getMethod(), request.getRequestURI());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 2、解析JWT中的用户身份信息
        String token = authorization.substring(7);
        Claims claims = jwtTokenProvider.parseToken(token);

        Long userId = Long.valueOf(claims.getSubject());
        String phone = claims.get("phone", String.class);

        // 3、写入UserContext，供Service层读取当前用户
        UserContext.setCurrentUser(new CurrentUserInfo(userId, phone));
        log.info("鉴权通过，method={}, uri={}, userId={}, phone={}",
                request.getMethod(), request.getRequestURI(), userId, phone);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 1、记录本次请求清理的用户ID，便于排查上下文链路
        CurrentUserInfo currentUser = UserContext.getCurrentUser();
        if (currentUser != null) {
            log.info("清理当前用户上下文，method={}, uri={}, userId={}",
                    request.getMethod(), request.getRequestURI(), currentUser.getUserId());
        }

        // 2、移除ThreadLocal，避免Tomcat线程复用导致用户信息串用
        UserContext.clear();
    }
}
