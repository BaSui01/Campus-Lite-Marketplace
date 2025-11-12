package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Resource;
import com.campus.marketplace.common.enums.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 学习资源数据访问接口
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    /**
     * 根据类型查询资源列表
     */
    Page<Resource> findByType(ResourceType type, Pageable pageable);

    /**
     * 根据分类查询资源列表
     */
    Page<Resource> findByCategory(String category, Pageable pageable);

    /**
     * 根据上传者查询资源列表
     */
    Page<Resource> findByUploaderId(Long uploaderId, Pageable pageable);

    /**
     * 搜索资源（标题或描述）
     */
    @Query("SELECT r FROM Resource r WHERE r.title LIKE %:keyword% OR r.description LIKE %:keyword%")
    Page<Resource> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询热门资源（按下载次数排序）
     */
    Page<Resource> findAllByOrderByDownloadCountDesc(Pageable pageable);
}
