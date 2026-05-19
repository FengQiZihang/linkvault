package com.linkvault.linkvaultserver.common;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequest {

    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1; // 页码，从1开始

    @Min(value = 1, message = "每页数量不能小于1")
    private Integer pageSize = 20; // 每页数量，默认20
}
