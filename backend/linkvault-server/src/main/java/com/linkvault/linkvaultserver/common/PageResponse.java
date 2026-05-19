package com.linkvault.linkvaultserver.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> items; // 当前页数据列表
    private Integer page; // 当前页码，从1开始
    private Integer pageSize; // 每页数量
    private Long total; // 总记录数

    /**
     * 构造分页响应对象，统一分页接口的出参结构。
     */
    public static <T> PageResponse<T> of(List<T> items, Integer page, Integer pageSize, Long total) {
        return new PageResponse<>(items, page, pageSize, total);
    }
}
