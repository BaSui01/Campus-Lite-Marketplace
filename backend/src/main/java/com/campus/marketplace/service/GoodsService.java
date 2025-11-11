package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateGoodsRequest;
import com.campus.marketplace.common.dto.response.GoodsDetailResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.enums.GoodsStatus;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物品服务接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface GoodsService {

    /**
     * 发布物品
     * 
     * @param request 创建物品请求
     * @return 物品 ID
     */
    Long createGoods(CreateGoodsRequest request);

    /**
     * 查询物品列表
     *
     * @param keyword 关键词（可选）
     * @param categoryId 分类 ID（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @param status 物品状态（可选）
     * @param page 页码（从 0 开始）
     * @param size 每页数量
     * @param sortBy 排序字段（createdAt/price/viewCount）
     * @param sortDirection 排序方向（ASC/DESC）
     * @param tagIds 标签 ID 列表（可选）
     * @return 物品列表（分页）
     */
    Page<GoodsResponse> listGoods(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            GoodsStatus status,
            int page,
            int size,
            String sortBy,
            String sortDirection,
            List<Long> tagIds
    );

    /**
     * 查询物品详情
     * 
     * @param id 物品 ID
     * @return 物品详情
     */
    GoodsDetailResponse getGoodsDetail(Long id);

    /**
     * 查询待审核物品列表
     * 
     * @param keyword 关键词（可选）
     * @param page 页码（从 0 开始）
     * @param size 每页数量
     * @return 待审核物品列表（分页）
     */
    Page<GoodsResponse> listPendingGoods(String keyword, int page, int size);

    /**
     * 审核物品
     * 
     * @param id 物品 ID
     * @param approved 是否通过审核
     * @param rejectReason 拒绝原因（审核不通过时必填）
     */
    void approveGoods(Long id, boolean approved, String rejectReason);

    /**
     * 查询当前用户发布的物品列表
     * 
     * @param page 页码（从 0 开始）
     * @param size 每页数量
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 我的商品列表（分页）
     */
    Page<GoodsResponse> getMyGoods(int page, int size, String sortBy, String sortDirection);
}
