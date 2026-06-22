package com.linkvault.linkvaultserver.dto.bookmark;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookmarkTagsRequest {

    @NotNull(message = "标签列表不能为空")
    private List<Long> tagIds;
}
