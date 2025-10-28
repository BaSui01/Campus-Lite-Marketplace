package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 导出任务仓库接口 📦
 *
 * 提供导出任务的数据库操作方法
 *
 * @author BaSui
 * @date 2025-10-27
 */
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {

    /**
     * 根据下载令牌查询导出任务
     */
    Optional<ExportJob> findByDownloadToken(String token);

    /**
     * 根据请求用户查询导出任务列表（按创建时间倒序）
     *
     * 用于用户查看自己的导出历史记录
     */
    List<ExportJob> findByRequestedByOrderByCreatedAtDesc(String requestedBy);

    /**
     * 根据校区ID查询导出任务列表（按创建时间倒序）
     *
     * 用于管理员查看校区内的导出历史
     */
    List<ExportJob> findByCampusIdOrderByCreatedAtDesc(Long campusId);
}
