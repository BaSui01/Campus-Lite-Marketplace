package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Resource;
import org.springframework.data.domain.Page;

/**
 * 学习资源服务接口
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
public interface ResourceService {

    /**
     * 查询资源列表（分页）
     */
    Page<Resource> listResources(int page, int size, String type, String category, String keyword);

    /**
     * 获取资源详情
     */
    Resource getResourceDetail(Long resourceId);

    /**
     * 记录下载
     */
    void recordDownload(Long resourceId);

    /**
     * 获取热门资源
     */
    Page<Resource> getHotResources(int page, int size);

    /**
     * 获取我上传的资源
     */
    Page<Resource> getMyResources(int page, int size);
}
