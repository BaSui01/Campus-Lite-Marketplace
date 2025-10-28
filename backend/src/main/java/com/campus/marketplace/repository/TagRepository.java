package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标签仓库
 *
 * 提供标签的持久化查询与去重相关操作
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    List<Tag> findByEnabledTrue();
}
