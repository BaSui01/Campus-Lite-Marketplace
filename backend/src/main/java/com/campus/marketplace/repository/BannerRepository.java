package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Banner;
import com.campus.marketplace.common.enums.BannerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 轮播图仓储接口
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    
    /**
     * 根据状态查询轮播图，按排序顺序升序
     * 
     * @param status 状态
     * @return 轮播图列表
     */
    List<Banner> findByStatusOrderBySortOrderAsc(BannerStatus status);
}
