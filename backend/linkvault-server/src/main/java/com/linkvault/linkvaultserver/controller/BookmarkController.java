package com.linkvault.linkvaultserver.controller;

import com.linkvault.linkvaultserver.common.ApiResponse;
import com.linkvault.linkvaultserver.common.PageResponse;
import com.linkvault.linkvaultserver.dto.bookmark.ImportBookmarkRequest;
import com.linkvault.linkvaultserver.dto.bookmark.UpdateBookmarkNoteRequest;
import com.linkvault.linkvaultserver.dto.bookmark.UpdateBookmarkTagsRequest;
import com.linkvault.linkvaultserver.dto.bookmark.OrganizeBookmarkRequest;
import com.linkvault.linkvaultserver.service.BookmarkService;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkDetailVO;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkListItemVO;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkNoteVO;
import com.linkvault.linkvaultserver.vo.bookmark.ImportBookmarkVO;
import com.linkvault.linkvaultserver.vo.bookmark.UpdateBookmarkTagsVO;
import com.linkvault.linkvaultserver.vo.bookmark.OrganizeBookmarkVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService; // 收藏业务服务

    @PostMapping("/import")
    public ApiResponse<ImportBookmarkVO> importBookmark(@Valid @RequestBody ImportBookmarkRequest request) {
        log.info("导入收藏接口调用，url={}", request.getUrl());
        ImportBookmarkVO response = bookmarkService.importBookmark(request.getUrl());
        log.info("导入收藏接口完成，bookmarkId={}, duplicated={}",
                response.getBookmarkId(), response.getDuplicated());
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<PageResponse<BookmarkListItemVO>> listRecentBookmarks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        log.info("获取最近保存收藏接口调用，page={}, pageSize={}", page, pageSize);
        PageResponse<BookmarkListItemVO> response = bookmarkService.listRecentBookmarks(page, pageSize);
        log.info("获取最近保存收藏接口完成，page={}, pageSize={}, total={}",
                response.getPage(), response.getPageSize(), response.getTotal());
        return ApiResponse.success(response);
    }

    @GetMapping("/{bookmarkId}")
    public ApiResponse<BookmarkDetailVO> getBookmarkDetail(@PathVariable Long bookmarkId) {
        log.info("获取收藏详情接口调用，bookmarkId={}", bookmarkId);
        BookmarkDetailVO response = bookmarkService.getBookmarkDetail(bookmarkId);
        log.info("获取收藏详情接口完成，bookmarkId={}", response.getBookmarkId());
        return ApiResponse.success(response);
    }

    @PutMapping("/{bookmarkId}/note")
    public ApiResponse<BookmarkNoteVO> updateBookmarkNote(
            @PathVariable Long bookmarkId,
            @Valid @RequestBody UpdateBookmarkNoteRequest request) {
        log.info("更新收藏备注接口调用，bookmarkId={}", bookmarkId);
        BookmarkNoteVO response = bookmarkService.updateBookmarkNote(bookmarkId, request.getNote());
        log.info("更新收藏备注接口完成，bookmarkId={}", response.getBookmarkId());
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{bookmarkId}")
    public ApiResponse<Boolean> deleteBookmark(@PathVariable Long bookmarkId) {
        log.info("删除收藏接口调用，bookmarkId={}", bookmarkId);
        Boolean response = bookmarkService.deleteBookmark(bookmarkId);
        log.info("删除收藏接口完成，bookmarkId={}", bookmarkId);
        return ApiResponse.success(response);
    }

    @PutMapping("/{bookmarkId}/tags")
    public ApiResponse<UpdateBookmarkTagsVO> updateBookmarkTags(
            @PathVariable Long bookmarkId,
            @Valid @RequestBody UpdateBookmarkTagsRequest request) {
        log.info("更新收藏标签接口调用，bookmarkId={}, tagCount={}", bookmarkId, request.getTagIds().size());
        UpdateBookmarkTagsVO response = bookmarkService.updateBookmarkTags(bookmarkId, request.getTagIds());
        log.info("更新收藏标签接口完成，bookmarkId={}", response.getBookmarkId());
        return ApiResponse.success(response);
    }

    @PutMapping("/{bookmarkId}/organize")
    public ApiResponse<OrganizeBookmarkVO> organizeBookmark(
            @PathVariable Long bookmarkId,
            @Valid @RequestBody OrganizeBookmarkRequest request) {
        log.info("承接页整理收藏接口调用，bookmarkId={}, tagCount={}", bookmarkId, request.getTagIds() != null ? request.getTagIds().size() : 0);
        OrganizeBookmarkVO response = bookmarkService.organizeBookmark(bookmarkId, request.getNote(), request.getTagIds());
        log.info("承接页整理收藏接口完成，bookmarkId={}", response.getBookmarkId());
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<BookmarkListItemVO>> searchBookmarks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) Boolean untagged,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("搜索筛选收藏接口调用，keyword={}, tagCount={}, untagged={}, page={}, pageSize={}",
                keyword, tagIds != null ? tagIds.size() : 0, untagged, page, pageSize);
        PageResponse<BookmarkListItemVO> response = bookmarkService.searchBookmarks(keyword, tagIds, untagged, page, pageSize);
        log.info("搜索筛选收藏接口完成，page={}, pageSize={}, total={}",
                response.getPage(), response.getPageSize(), response.getTotal());
        return ApiResponse.success(response);
    }
}
