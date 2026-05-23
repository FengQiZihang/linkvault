package com.linkvault.linkvaultserver.dto.tag;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MergeTagRequest {

    @NotNull(message = "目标标签不能为空")
    @Positive(message = "目标标签ID不合法")
    private Long targetTagId; // 合并目标标签ID
}
