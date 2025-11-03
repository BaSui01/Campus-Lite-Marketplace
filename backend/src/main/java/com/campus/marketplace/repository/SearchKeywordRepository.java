package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.SearchKeyword;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 热门搜索关键词数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    /**
     * 根据关键词查询
     */
    Optional<SearchKeyword> findByKeyword(String keyword);

    /**
     * 获取热门搜索关键词（按搜索次数倒序）
     */
    List<SearchKeyword> findAllByOrderBySearchCountDesc(Pageable pageable);

    /**
     * 根据关键词前缀查询（智能补全）
     */
    List<SearchKeyword> findByKeywordStartingWithIgnoreCaseOrderBySearchCountDesc(String prefix, Pageable pageable);
}
