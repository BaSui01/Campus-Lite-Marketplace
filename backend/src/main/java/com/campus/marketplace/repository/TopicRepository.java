package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 话题数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    /**
     * 根据话题名称查询话题
     */
    Optional<Topic> findByName(String name);

    /**
     * 获取热门话题（前10个）
     */
    List<Topic> findTop10ByOrderByHotnessDesc();

    /**
     * 检查话题名称是否存在
     */
    boolean existsByName(String name);
}
