package com.linkvault.linkvaultserver.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLibraryStatsVO {

    private Long totalBookmarkCount;

    private Long tagCount;

    private Long untaggedBookmarkCount;
}
