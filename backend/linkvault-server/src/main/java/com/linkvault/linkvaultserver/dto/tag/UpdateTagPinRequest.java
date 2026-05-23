package com.linkvault.linkvaultserver.dto.tag;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTagPinRequest {

    @NotNull(message = "置顶状态不能为空")
    private Boolean pinned; // 是否置顶
}
