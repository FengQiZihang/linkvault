package com.linkvault.linkvaultserver.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@Getter
public class JwtTokenProvider {

    private final SecretKey secretKey; // JWT签名密钥
    private final long accessTokenExpiresIn; // accessToken有效期，单位秒

    /**
     * 读取配置中的JWT密钥和过期时间，并初始化签名密钥对象。
     */
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expires-in}") long accessTokenExpiresIn
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    public String generateAccessToken(Long userId, String phone) {
        // 1、计算签发时间和过期时间
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpiresIn);

        // 2、subject存用户ID，claim中补充手机号
        // 3、使用配置中的密钥签名并生成紧凑JWT字符串
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        // 1、使用同一签名密钥构建解析器
        // 2、解析签名JWT，jjwt会同时校验签名和过期时间
        // 3、返回业务侧需要读取的Claims
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
