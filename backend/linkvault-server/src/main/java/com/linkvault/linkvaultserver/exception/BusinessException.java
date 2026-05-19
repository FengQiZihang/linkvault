package com.linkvault.linkvaultserver.exception;

public class BusinessException extends RuntimeException {
    private final int httpStatus; // HTTP状态码，用于控制响应协议状态
    private final int code; // 业务错误码，用于前端识别具体失败类型
    private final String msg; // 业务错误提示文案

    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getHttpStatus(), errorCode.getCode(), errorCode.getMsg());
    }

    private BusinessException(int httpStatus, int code, String msg) {
        super(msg);
        this.httpStatus = httpStatus;
        this.code = code;
        this.msg = msg;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
