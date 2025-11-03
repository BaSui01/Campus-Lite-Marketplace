package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.GoodsDetailDTO;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.GoodsDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品详情控制器
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
@Tag(name = "商品详情", description = "商品详情页相关接口")
public class GoodsDetailController {

    private final GoodsDetailService goodsDetailService;
    private final UserRepository userRepository;

    @GetMapping("/{goodsId}/detail")
    @Operation(summary = "获取商品详情")
    public ApiResponse<GoodsDetailDTO> getGoodsDetail(
        @Parameter(description = "商品ID") @PathVariable Long goodsId
    ) {
        Long userId = getCurrentUserIdOrNull();
        GoodsDetailDTO detail = goodsDetailService.getGoodsDetail(goodsId, userId);
        return ApiResponse.success(detail);
    }

    @GetMapping("/{goodsId}/similar")
    @Operation(summary = "获取相似商品推荐")
    public ApiResponse<List<GoodsResponse>> getSimilarGoods(
        @Parameter(description = "商品ID") @PathVariable Long goodsId,
        @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") int limit
    ) {
        List<GoodsResponse> similarGoods = goodsDetailService.getSimilarGoods(goodsId, limit);
        return ApiResponse.success(similarGoods);
    }

    @PostMapping("/{goodsId}/view")
    @Operation(summary = "记录商品浏览")
    public ApiResponse<Void> recordView(
        @Parameter(description = "商品ID") @PathVariable Long goodsId,
        @Parameter(description = "来源页面") @RequestParam(required = false, defaultValue = "detail") String sourcePage
    ) {
        Long userId = getCurrentUserId();
        goodsDetailService.recordView(goodsId, userId, sourcePage);
        return ApiResponse.success();
    }

    @GetMapping("/view-history")
    @Operation(summary = "获取我的浏览历史")
    public ApiResponse<List<GoodsResponse>> getUserViewHistory(
        @Parameter(description = "返回数量") @RequestParam(defaultValue = "50") int limit
    ) {
        Long userId = getCurrentUserId();
        List<GoodsResponse> history = goodsDetailService.getUserViewHistory(userId, limit);
        return ApiResponse.success(history);
    }

    @DeleteMapping("/view-history")
    @Operation(summary = "清空浏览历史")
    public ApiResponse<Void> clearViewHistory() {
        Long userId = getCurrentUserId();
        goodsDetailService.clearUserViewHistory(userId);
        return ApiResponse.success();
    }

    /**
     * 获取当前登录用户ID（可能为null）
     */
    private Long getCurrentUserIdOrNull() {
        try {
            String username = SecurityUtil.getCurrentUsername();
            return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户ID（必须登录）
     */
    private Long getCurrentUserId() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
