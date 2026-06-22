package com.linkvault.linkvaultserver.vo.bookmark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagSimpleVO {

    private Long tagId; // 标签ID

    private String name; // 标签名称

    private Boolean pinned; // 是否置顶
}
