package com.linkvault.linkvaultserver.dto.bookmark;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookmarkNoteRequest {

    @NotNull(message = "备注不能为空")
    @Size(max = 1000, message = "备注不能超过1000字")
    private String note; // 用户备注，可为空字符串
}
