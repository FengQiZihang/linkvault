package com.linkvault.linkvaultserver.interceptor;

import com.linkvault.linkvaultserver.component.JwtTokenProvider;
import com.linkvault.linkvaultserver.context.CurrentUserInfo;
import com.linkvault.linkvaultserver.context.UserContext;
import com.linkvault.linkvaultserver.exception.BusinessException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, 40101, "未登录或token无效");
        }

        String token = authorization.substring(7);
        Claims claims = jwtTokenProvider.parseToken(token);

        Long userId = Long.valueOf(claims.getSubject());
        String phone = claims.get("phone", String.class);

        UserContext.setCurrentUser(new CurrentUserInfo(userId, phone));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
