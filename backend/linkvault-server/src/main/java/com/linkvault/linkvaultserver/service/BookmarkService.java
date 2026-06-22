package com.linkvault.linkvaultserver.service;

import com.linkvault.linkvaultserver.common.PageResponse;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkDetailVO;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkListItemVO;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkNoteVO;
import com.linkvault.linkvaultserver.vo.bookmark.ImportBookmarkVO;
import com.linkvault.linkvaultserver.vo.bookmark.UpdateBookmarkTagsVO;
import com.linkvault.linkvaultserver.vo.bookmark.OrganizeBookmarkVO;

import java.util.List;

public interface BookmarkService {

    ImportBookmarkVO importBookmark(String url);

    PageResponse<BookmarkListItemVO> listRecentBookmarks(Integer page, Integer pageSize);

    BookmarkDetailVO getBookmarkDetail(Long bookmarkId);

    BookmarkNoteVO updateBookmarkNote(Long bookmarkId, String note);

    Boolean deleteBookmark(Long bookmarkId);

    UpdateBookmarkTagsVO updateBookmarkTags(Long bookmarkId, List<Long> tagIds);

    OrganizeBookmarkVO organizeBookmark(Long bookmarkId, String note, List<Long> tagIds);

    PageResponse<BookmarkListItemVO> searchBookmarks(String keyword, List<Long> tagIds, Boolean untagged, Integer page, Integer pageSize);
}
