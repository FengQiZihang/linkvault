package com.linkvault.linkvaultserver.exception;

import com.linkvault.linkvaultserver.common.ApiResponse;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理DTO参数校验失败，优先返回字段注解上的错误文案。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "请求参数错误";
        log.info("请求参数校验失败，msg={}", msg);
        return ApiResponse.fail(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 处理业务异常，使用异常对象中的HTTP状态码和业务错误码。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.info("业务异常，httpStatus={}, code={}, msg={}", e.getHttpStatus(), e.getCode(), e.getMsg());
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ApiResponse.fail(e.getCode(), e.getMsg()));
    }

    /**
     * 处理JWT解析和校验失败，统一返回未登录或token无效。
     */
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleJwtException(JwtException e) {
        log.info("JWT解析失败，msg={}", e.getMessage());
        return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMsg());
    }



}
