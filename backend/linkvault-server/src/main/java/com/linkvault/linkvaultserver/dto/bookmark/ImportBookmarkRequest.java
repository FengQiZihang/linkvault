package com.linkvault.linkvaultserver.dto.bookmark;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportBookmarkRequest {

    @NotBlank(message = "URL不能为空")
    @Size(max = 2048, message = "URL长度不能超过2048")
    @Pattern(regexp = "^https?://.+", message = "只支持http或https链接")
    private String url; // 用户粘贴的原始URL
}
