package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateTagRequest;
import com.campus.marketplace.common.dto.request.MergeTagRequest;
import com.campus.marketplace.common.dto.request.UpdateTagRequest;
import com.campus.marketplace.common.dto.response.TagResponse;

import java.util.List;

/**
 * 标签服务接口
 *
 * 提供标签的创建、更新、删除、合并与查询能力
 *
 * @author BaSui
 * @date 2025-10-27
 */
public interface TagService {

    Long createTag(CreateTagRequest request);

    void updateTag(Long id, UpdateTagRequest request);

    void deleteTag(Long id);

    void mergeTag(MergeTagRequest request);

    List<TagResponse> listAllTags();
}
