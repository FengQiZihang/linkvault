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
public class ImportBookmarkVO {

    private Long bookmarkId; // 收藏ID

    private Boolean duplicated; // 是否为当前用户已存在收藏

    private LinkMetaVO link; // 链接元信息

    private String savedAt; // 用户保存时间，UTC ISO 8601字符串
}
