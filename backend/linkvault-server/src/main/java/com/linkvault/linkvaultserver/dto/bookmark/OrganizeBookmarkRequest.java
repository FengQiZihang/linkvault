package com.linkvault.linkvaultserver.dto.bookmark;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizeBookmarkRequest {

    @Size(max = 1000, message = "备注最长1000个字符")
    private String note;

    private List<Long> tagIds;
}
