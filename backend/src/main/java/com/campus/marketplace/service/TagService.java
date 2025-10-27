package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateTagRequest;
import com.campus.marketplace.common.dto.request.MergeTagRequest;
import com.campus.marketplace.common.dto.request.UpdateTagRequest;
import com.campus.marketplace.common.dto.response.TagResponse;

import java.util.List;

public interface TagService {

    Long createTag(CreateTagRequest request);

    void updateTag(Long id, UpdateTagRequest request);

    void deleteTag(Long id);

    void mergeTag(MergeTagRequest request);

    List<TagResponse> listAllTags();
}
