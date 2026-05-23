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
        // 1、只拦截需要登录的资源接口
        // 2、健康检查、发送验证码和登录接口不进入鉴权拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/me",
                        "/me/**",
                        "/bookmarks",
                        "/bookmarks/**",
                        "/tags",
                        "/tags/**"
                );
    }
}
