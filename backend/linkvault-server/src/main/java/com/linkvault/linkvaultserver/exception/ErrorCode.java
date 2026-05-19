package com.linkvault.linkvaultserver.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    PARAM_ERROR(400, 40001, "请求参数错误"), // 请求参数错误
    UNAUTHORIZED(401, 40101, "未登录或token无效"), // 未登录或token无效
    TOKEN_EXPIRED(401, 40102, "token已过期"), // token已过期
    FORBIDDEN(403, 40301, "无权访问当前资源"), // 已登录但无权访问当前资源
    NOT_FOUND(404, 40401, "资源不存在"), // 请求资源不存在
    CONFLICT(409, 40901, "资源冲突"), // 资源冲突
    TOO_MANY_REQUESTS(429, 42901, "请求过于频繁"), // 请求频率超限
    SERVER_ERROR(500, 50001, "服务端异常"), // 服务端未预期异常

    SMS_INVALID(400, 40001, "验证码错误或已失效"), // 验证码错误、过期、已使用或失败次数过多
    SMS_COOLDOWN(429, 42901, "请求过于频繁"); // 短信验证码发送冷却中

    private final int httpStatus; // HTTP状态码
    private final int code; // 业务错误码
    private final String msg; // 默认错误提示文案

    ErrorCode(int httpStatus, int code, String msg) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.msg = msg;
    }
}
