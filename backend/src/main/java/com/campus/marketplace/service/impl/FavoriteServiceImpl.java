package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Favorite;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.FavoriteRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收藏服务实现类
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final GoodsRepository goodsRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 添加收藏
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    // 🆙 升级缓存空间：favorite:list → favorite:v2:list
    @CacheEvict(value = {"favorite:list", "favorite:v2:list"}, key = "T(com.campus.marketplace.common.utils.SecurityUtil).getCurrentUserId()")
    public void addFavorite(Long goodsId) {
        log.info("添加收藏: goodsId={}", goodsId);

        // 1. 获取当前登录用户
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 查询物品
        Goods goods = goodsRepository.findById(goodsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOODS_NOT_FOUND));

        // 3. 验证物品状态（只能收藏已审核通过的物品）
        if (goods.getStatus() != GoodsStatus.APPROVED) {
            throw new BusinessException(ErrorCode.GOODS_NOT_APPROVED);
        }

        // 3.1 校区隔离：无跨校权限禁止跨校收藏
        try {
            if (!SecurityUtil.hasAuthority(PermissionCodes.SYSTEM_CAMPUS_CROSS)) {
                if (user.getCampusId() != null && goods.getCampusId() != null
                        && !user.getCampusId().equals(goods.getCampusId())) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "跨校区收藏被禁止");
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception ignored) { }

        // 4. 检查是否已收藏
        if (favoriteRepository.existsByUserIdAndGoodsId(user.getId(), goodsId)) {
            throw new BusinessException(ErrorCode.FAVORITE_EXISTS);
        }

        // 5. 创建收藏记录
        Favorite favorite = Favorite.builder()
                .userId(user.getId())
                .goodsId(goodsId)
                .build();

        favoriteRepository.save(favorite);

        // 6. 增加物品收藏数
        goods.incrementFavoriteCount();
        goodsRepository.save(goods);

        log.info("收藏成功: userId={}, goodsId={}", user.getId(), goodsId);
    }

    /**
     * 取消收藏
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    // 同时清理老版本与新版本缓存键，保证兼容
    @CacheEvict(value = {"favorite:list", "favorite:v2:list"}, key = "T(com.campus.marketplace.common.utils.SecurityUtil).getCurrentUserId()")
    public void removeFavorite(Long goodsId) {
        log.info("取消收藏: goodsId={}", goodsId);

        // 1. 获取当前登录用户
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 查询收藏记录
        Favorite favorite = favoriteRepository.findByUserIdAndGoodsId(user.getId(), goodsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_NOT_FOUND));

        // 3. 删除收藏记录
        favoriteRepository.delete(favorite);

        // 4. 减少物品收藏数
        Goods goods = goodsRepository.findById(goodsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOODS_NOT_FOUND));
        goods.decrementFavoriteCount();
        goodsRepository.save(goods);

        log.info("取消收藏成功: userId={}, goodsId={}", user.getId(), goodsId);
    }

    /**
     * 查询收藏列表
     */
    @Override
    @Transactional(readOnly = true)
    // 读取新版本缓存空间，避免读取历史不兼容数据
    @Cacheable(value = "favorite:v2:list", key = "T(com.campus.marketplace.common.utils.SecurityUtil).getCurrentUserId() + ':' + #page + ':' + #size")
    public Page<GoodsResponse> listFavorites(int page, int size) {
        log.info("查询收藏列表: page={}, size={}", page, size);

        // 1. 获取当前登录用户
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 构建分页参数（按收藏时间倒序）
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 3. 查询收藏列表
        Page<Favorite> favoritePage = favoriteRepository.findByUserIdWithGoods(user.getId(), pageable);

        // 4. 转换为响应 DTO
        return favoritePage.map(favorite -> convertToResponse(favorite.getGoods()));
    }

    /**
     * 检查是否已收藏
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(Long goodsId) {
        try {
            String username = SecurityUtil.getCurrentUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            return favoriteRepository.existsByUserIdAndGoodsId(user.getId(), goodsId);
        } catch (Exception e) {
            // 未登录用户返回 false
            return false;
        }
    }

    /**
     * 转换为响应 DTO
     */
    private GoodsResponse convertToResponse(Goods goods) {
        // 获取分类名称
        String categoryName = categoryRepository.findById(goods.getCategoryId())
                .map(Category::getName)
                .orElse("未知分类");

        // 获取卖家信息（包括头像）
        User seller = userRepository.findById(goods.getSellerId()).orElse(null);
        String sellerUsername = seller != null ? seller.getUsername() : "未知用户";
        String sellerAvatar = seller != null ? seller.getAvatar() : null;

        // 获取封面图片（第一张）
        String coverImage = goods.getImages() != null && goods.getImages().length > 0
                ? goods.getImages()[0]
                : null;

        return GoodsResponse.builder()
                .id(goods.getId())
                .title(goods.getTitle())
                .description(truncateDescription(goods.getDescription()))
                .price(goods.getPrice())
                .categoryId(goods.getCategoryId())
                .categoryName(categoryName)
                .sellerId(goods.getSellerId())
                .sellerUsername(sellerUsername)
                .sellerAvatar(sellerAvatar)  // ✅ 新增
                .status(goods.getStatus())
                .viewCount(goods.getViewCount())
                .favoriteCount(goods.getFavoriteCount())
                .stock(goods.getStock())  // ✅ 新增
                .soldCount(goods.getSoldCount())  // ✅ 新增
                .originalPrice(goods.getOriginalPrice())  // ✅ 新增
                .coverImage(coverImage)
                .images(goods.getImages())  // ✅ 新增：所有图片（支持轮播）
                .createdAt(goods.getCreatedAt())
                .build();
    }

    /**
     * 截断描述
     */
    private String truncateDescription(String description) {
        if (description == null) {
            return null;
        }
        return description.length() > 100 
                ? description.substring(0, 100) + "..." 
                : description;
    }
}
