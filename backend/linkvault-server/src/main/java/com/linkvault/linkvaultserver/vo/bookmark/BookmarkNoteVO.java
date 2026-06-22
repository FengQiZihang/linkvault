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
public class BookmarkNoteVO {

    private Long bookmarkId; // 收藏ID

    private String note; // 更新后的备注
}
