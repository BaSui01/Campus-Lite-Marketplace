package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.SearchHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 搜索历史数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    /**
     * 根据用户ID查询搜索历史（按时间倒序）
     */
    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 删除用户的所有搜索历史
     */
    void deleteByUserId(Long userId);

    /**
     * 统计关键词的搜索次数
     */
    long countByKeyword(String keyword);
}
