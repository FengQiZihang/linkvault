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
public class BookmarkListItemVO {

    private Long bookmarkId; // 收藏ID

    private String title; // 标题

    private String platform; // 信息源平台

    private String publisher; // 发布作者

    private String publishedAt; // 内容发布时间，UTC ISO 8601字符串

    private String note; // 用户备注

    private String savedAt; // 用户保存时间，UTC ISO 8601字符串

    private List<TagSimpleVO> tags; // 当前收藏关联标签
}
