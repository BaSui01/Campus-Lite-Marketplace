package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Banner;

import java.util.List;

/**
 * 轮播图服务接口
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */
public interface BannerService {
    
    /**
     * 获取启用的轮播图列表
     * 
     * @return 轮播图列表（按排序顺序）
     */
    List<Banner> getActiveBanners();
    
    /**
     * 记录轮播图点击
     * 
     * @param id 轮播图 ID
     */
    void recordClick(Long id);
    
    /**
     * 记录轮播图展示
     * 
     * @param id 轮播图 ID
     */
    void recordView(Long id);
}
