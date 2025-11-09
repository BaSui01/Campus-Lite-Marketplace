package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateTagRequest;
import com.campus.marketplace.common.dto.request.MergeTagRequest;
import com.campus.marketplace.common.dto.request.UpdateTagRequest;
import com.campus.marketplace.common.dto.response.TagResponse;
import com.campus.marketplace.common.entity.Tag;

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

    /**
     * 分页查询标签列表（支持筛选）
     */
    org.springframework.data.domain.Page<TagResponse> listTags(String keyword, Boolean enabled, int page, int size);

    /**
     * 获取热门标签列表
     */
    List<com.campus.marketplace.common.dto.response.TagStatisticsResponse> getHotTags(int limit);

    // 🎯 BaSui 新增方法（标签管理扩展）
    /**
     * 根据ID获取标签详情
     */
    Tag getById(Long id);

    /**
     * 切换标签启用状态
     */
    void toggleEnabled(Long id);

    /**
     * 批量删除标签
     * @return 成功删除的数量
     */
    int batchDelete(List<Long> ids);

    /**
     * 获取标签统计信息
     */
    com.campus.marketplace.common.dto.response.TagStatisticsResponse getStatistics(Long id);
}
