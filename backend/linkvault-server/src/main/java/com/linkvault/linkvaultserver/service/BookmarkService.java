package com.linkvault.linkvaultserver.service;

import com.linkvault.linkvaultserver.common.PageResponse;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkDetailVO;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkListItemVO;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkNoteVO;
import com.linkvault.linkvaultserver.vo.bookmark.ImportBookmarkVO;

public interface BookmarkService {

    ImportBookmarkVO importBookmark(String url);

    PageResponse<BookmarkListItemVO> listRecentBookmarks(Integer page, Integer pageSize);

    BookmarkDetailVO getBookmarkDetail(Long bookmarkId);

    BookmarkNoteVO updateBookmarkNote(Long bookmarkId, String note);

    Boolean deleteBookmark(Long bookmarkId);
}
