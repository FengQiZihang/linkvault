package com.linkvault.linkvaultserver.vo.bookmark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkDetailVO {

    private Long bookmarkId; // 收藏ID

    private LinkMetaVO link; // 链接元信息

    private String note; // 用户备注

    private String savedAt; // 用户保存时间，UTC ISO 8601字符串

    private List<TagSimpleVO> tags; // 当前收藏关联标签
}
