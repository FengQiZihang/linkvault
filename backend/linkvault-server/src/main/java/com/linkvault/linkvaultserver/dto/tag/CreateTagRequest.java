package com.linkvault.linkvaultserver.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTagRequest {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 20, message = "标签名称不能超过20个字")
    private String name; // 标签名称，同一用户下唯一

    @NotNull(message = "置顶状态不能为空")
    private Boolean pinned; // 是否置顶
}
