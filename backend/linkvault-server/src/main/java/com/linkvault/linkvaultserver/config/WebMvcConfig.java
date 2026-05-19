package com.linkvault.linkvaultserver.config;

import com.linkvault.linkvaultserver.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor; // 登录鉴权拦截器

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1、默认拦截/api/v1下的所有接口
        // 2、放行健康检查接口，便于启动和连通性验证
        // 3、放行发送验证码和登录接口，避免未登录时无法获取token
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/ping",
                        "/api/v1/auth/sms-code",
                        "/api/v1/auth/login"
                );
    }
}
