package com.linkvault.linkvaultserver.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameTagRequest {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 20, message = "标签名称不能超过20个字")
    private String name; // 新标签名称，同一用户下唯一
}
