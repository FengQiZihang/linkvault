package com.linkvault.linkvaultserver.vo.tag;

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
public class TagVO {

    private Long tagId; // 标签ID

    private String name; // 标签名称

    private Boolean pinned; // 是否置顶

    private Long bookmarkCount; // 当前用户下该标签关联的收藏数量
}
