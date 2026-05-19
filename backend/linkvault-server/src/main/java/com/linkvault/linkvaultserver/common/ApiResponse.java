package com.linkvault.linkvaultserver.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    public Integer code; // 业务状态码，0表示成功，非0表示失败
    public String msg; // 响应提示文案
    public T data; // 响应数据主体，失败时通常为null

    /**
     * 构造成功响应，统一返回code=0和success文案。
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    /**
     * 构造失败响应，业务数据统一置空。
     */
    public static <T> ApiResponse<T> fail(Integer code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

}
