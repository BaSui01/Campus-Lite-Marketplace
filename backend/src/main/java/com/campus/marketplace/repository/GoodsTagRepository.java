package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.GoodsTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 商品-标签关联关系仓库
 *
 * 负责商品与标签绑定关系的增删查与批量操作
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface GoodsTagRepository extends JpaRepository<GoodsTag, Long> {

    List<GoodsTag> findByGoodsId(Long goodsId);

    List<GoodsTag> findByGoodsIdIn(Collection<Long> goodsIds);

    @Modifying
    @Query("DELETE FROM GoodsTag gt WHERE gt.goodsId = :goodsId AND gt.tagId NOT IN (:tagIds)")
    void deleteByGoodsIdAndTagIdNotIn(@Param("goodsId") Long goodsId, @Param("tagIds") Collection<Long> tagIds);

    @Modifying
    void deleteByGoodsId(Long goodsId);

    Optional<GoodsTag> findByGoodsIdAndTagId(Long goodsId, Long tagId);

    List<GoodsTag> findByTagId(Long tagId);

    long countByTagId(Long tagId);

    @Query("SELECT gt.goodsId FROM GoodsTag gt WHERE gt.tagId IN (:tagIds) GROUP BY gt.goodsId HAVING COUNT(DISTINCT gt.tagId) = :size")
    List<Long> findGoodsIdsByAllTagIds(@Param("tagIds") Collection<Long> tagIds, @Param("size") long size);

    @Query("SELECT gt.tagId, COUNT(gt.goodsId) FROM GoodsTag gt GROUP BY gt.tagId ORDER BY COUNT(gt.goodsId) DESC")
    List<Object[]> findTopTagsByUsageCount(org.springframework.data.domain.Pageable pageable);
}
