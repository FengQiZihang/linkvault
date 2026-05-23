package com.linkvault.linkvaultserver.controller;

import com.linkvault.linkvaultserver.common.ApiResponse;
import com.linkvault.linkvaultserver.dto.tag.CreateTagRequest;
import com.linkvault.linkvaultserver.dto.tag.MergeTagRequest;
import com.linkvault.linkvaultserver.dto.tag.RenameTagRequest;
import com.linkvault.linkvaultserver.dto.tag.UpdateTagPinRequest;
import com.linkvault.linkvaultserver.service.TagService;
import com.linkvault.linkvaultserver.vo.tag.TagListVO;
import com.linkvault.linkvaultserver.vo.tag.TagVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService; // 标签业务服务

    @GetMapping
    public ApiResponse<TagListVO> listTags() {
        log.info("获取标签列表接口调用");
        TagListVO response = tagService.listTags();
        log.info("获取标签列表接口完成，pinnedCount={}, normalCount={}",
                response.getPinned().size(), response.getNormal().size());
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<TagVO> createTag(@Valid @RequestBody CreateTagRequest request) {
        log.info("创建标签接口调用，name={}, pinned={}", request.getName(), request.getPinned());
        TagVO response = tagService.createTag(request.getName(), request.getPinned());
        log.info("创建标签接口完成，tagId={}, name={}, pinned={}",
                response.getTagId(), response.getName(), response.getPinned());
        return ApiResponse.success(response);
    }

    @PatchMapping("/{tagId}/name")
    public ApiResponse<TagVO> renameTag(@PathVariable Long tagId, @Valid @RequestBody RenameTagRequest request) {
        log.info("重命名标签接口调用，tagId={}, name={}", tagId, request.getName());
        TagVO response = tagService.renameTag(tagId, request.getName());
        log.info("重命名标签接口完成，tagId={}, name={}", response.getTagId(), response.getName());
        return ApiResponse.success(response);
    }

    @PatchMapping("/{tagId}/pin")
    public ApiResponse<TagVO> updateTagPinned(@PathVariable Long tagId, @Valid @RequestBody UpdateTagPinRequest request) {
        log.info("更新标签置顶状态接口调用，tagId={}, pinned={}", tagId, request.getPinned());
        TagVO response = tagService.updateTagPinned(tagId, request.getPinned());
        log.info("更新标签置顶状态接口完成，tagId={}, pinned={}", response.getTagId(), response.getPinned());
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{tagId}")
    public ApiResponse<Boolean> deleteTag(@PathVariable Long tagId) {
        log.info("删除标签接口调用，tagId={}", tagId);
        Boolean response = tagService.deleteTag(tagId);
        log.info("删除标签接口完成，tagId={}", tagId);
        return ApiResponse.success(response);
    }

    @PostMapping("/{sourceTagId}/merge")
    public ApiResponse<Boolean> mergeTag(@PathVariable Long sourceTagId, @Valid @RequestBody MergeTagRequest request) {
        log.info("合并标签接口调用，sourceTagId={}, targetTagId={}", sourceTagId, request.getTargetTagId());
        Boolean response = tagService.mergeTag(sourceTagId, request.getTargetTagId());
        log.info("合并标签接口完成，sourceTagId={}, targetTagId={}", sourceTagId, request.getTargetTagId());
        return ApiResponse.success(response);
    }
}
