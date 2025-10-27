package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateGoodsRequest;
import com.campus.marketplace.common.dto.response.GoodsDetailResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.EncryptUtil;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.GoodsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 物品服务实现类
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final GoodsRepository goodsRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SensitiveWordFilter sensitiveWordFilter;

    /**
     * 发布物品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGoods(CreateGoodsRequest request) {
        log.info("发布物品: title={}, price={}, categoryId={}", 
                request.title(), request.price(), request.categoryId());

        // 1. 获取当前登录用户
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 验证分类是否存在
        if (!categoryRepository.existsById(request.categoryId())) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 3. 敏感词过滤
        String filteredTitle = sensitiveWordFilter.filter(request.title());
        String filteredDescription = sensitiveWordFilter.filter(request.description());

        // 4. 创建物品
        Goods goods = Goods.builder()
                .title(filteredTitle)
                .description(filteredDescription)
                .price(request.price())
                .categoryId(request.categoryId())
                .sellerId(user.getId())
                .status(GoodsStatus.PENDING) // 待审核
                .viewCount(0)
                .favoriteCount(0)
                .images(request.images().toArray(new String[0]))
                .build();

        // 5. 保存物品
        goodsRepository.save(goods);

        log.info("物品发布成功: goodsId={}, sellerId={}, title={}", 
                goods.getId(), user.getId(), goods.getTitle());

        return goods.getId();
    }

    /**
     * 查询物品列表
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "goods:list", key = "#keyword + ':' + #categoryId + ':' + #minPrice + ':' + #maxPrice + ':' + #page + ':' + #size + ':' + #sortBy + ':' + #sortDirection", unless = "#result == null")
    public Page<GoodsResponse> listGoods(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        log.info("查询物品列表: keyword={}, categoryId={}, minPrice={}, maxPrice={}, page={}, size={}", 
                keyword, categoryId, minPrice, maxPrice, page, size);

        // 1. 构建排序
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        
        String sortField = switch (sortBy != null ? sortBy : "createdAt") {
            case "price" -> "price";
            case "viewCount" -> "viewCount";
            default -> "createdAt";
        };
        
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. 查询物品（只返回已审核通过的）
        Page<Goods> goodsPage = goodsRepository.findByConditions(
                GoodsStatus.APPROVED,
                categoryId,
                minPrice,
                maxPrice,
                keyword,
                pageable
        );

        // 3. 转换为响应 DTO
        return goodsPage.map(this::convertToResponse);
    }

    /**
     * 查询物品详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Cacheable(value = "goods:detail", key = "#id", unless = "#result == null")
    public GoodsDetailResponse getGoodsDetail(Long id) {
        log.info("查询物品详情: goodsId={}", id);

        // 1. 查询物品（包含卖家和分类信息）
        Goods goods = goodsRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOODS_NOT_FOUND));

        // 2. 增加浏览量
        goods.incrementViewCount();
        goodsRepository.save(goods);

        // 3. 转换为响应 DTO
        return convertToDetailResponse(goods);
    }

    /**
     * 转换为列表响应 DTO
     */
    private GoodsResponse convertToResponse(Goods goods) {
        // 获取分类名称
        String categoryName = categoryRepository.findById(goods.getCategoryId())
                .map(Category::getName)
                .orElse("未知分类");

        // 获取卖家用户名
        String sellerUsername = userRepository.findById(goods.getSellerId())
                .map(User::getUsername)
                .orElse("未知用户");

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
                .status(goods.getStatus())
                .viewCount(goods.getViewCount())
                .favoriteCount(goods.getFavoriteCount())
                .coverImage(coverImage)
                .createdAt(goods.getCreatedAt())
                .build();
    }

    /**
     * 转换为详情响应 DTO
     */
    private GoodsDetailResponse convertToDetailResponse(Goods goods) {
        // 获取分类名称
        String categoryName = goods.getCategory() != null 
                ? goods.getCategory().getName() 
                : "未知分类";

        // 获取卖家信息（敏感信息脱敏）
        User seller = goods.getSeller();
        GoodsDetailResponse.SellerInfo sellerInfo = GoodsDetailResponse.SellerInfo.builder()
                .id(seller.getId())
                .username(seller.getUsername())
                .avatar(seller.getAvatar())
                .points(seller.getPoints())
                .phone(seller.getPhone() != null ? EncryptUtil.maskPhone(seller.getPhone()) : null)
                .email(seller.getEmail() != null ? EncryptUtil.maskEmail(seller.getEmail()) : null)
                .build();

        // 转换图片数组为列表
        List<String> images = goods.getImages() != null 
                ? Arrays.asList(goods.getImages()) 
                : List.of();

        return GoodsDetailResponse.builder()
                .id(goods.getId())
                .title(goods.getTitle())
                .description(goods.getDescription())
                .price(goods.getPrice())
                .categoryId(goods.getCategoryId())
                .categoryName(categoryName)
                .status(goods.getStatus())
                .viewCount(goods.getViewCount())
                .favoriteCount(goods.getFavoriteCount())
                .images(images)
                .seller(sellerInfo)
                .createdAt(goods.getCreatedAt())
                .updatedAt(goods.getUpdatedAt())
                .build();
    }

    /**
     * 查询待审核物品列表
     */
    @Override
    @Transactional(readOnly = true)
    public Page<GoodsResponse> listPendingGoods(int page, int size) {
        log.info("查询待审核物品列表: page={}, size={}", page, size);

        // 构建分页参数（按创建时间倒序）
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 查询待审核物品
        Page<Goods> goodsPage = goodsRepository.findByConditions(
                GoodsStatus.PENDING,
                null, null, null, null,
                pageable
        );

        // 转换为响应 DTO
        return goodsPage.map(this::convertToResponse);
    }

    /**
     * 审核物品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveGoods(Long id, boolean approved, String rejectReason) {
        log.info("审核物品: goodsId={}, approved={}, rejectReason={}", id, approved, rejectReason);

        // 1. 获取当前审核人
        String adminUsername = SecurityUtil.getCurrentUsername();
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 查询物品
        Goods goods = goodsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOODS_NOT_FOUND));

        // 3. 验证物品状态（只能审核待审核的物品）
        if (goods.getStatus() != GoodsStatus.PENDING) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "该物品已审核，无法重复审核");
        }

        // 4. 更新物品状态
        if (approved) {
            goods.setStatus(GoodsStatus.APPROVED);
            log.info("物品审核通过: goodsId={}, adminId={}, adminUsername={}", 
                    id, admin.getId(), adminUsername);
        } else {
            goods.setStatus(GoodsStatus.REJECTED);
            log.info("物品审核拒绝: goodsId={}, adminId={}, adminUsername={}, reason={}", 
                    id, admin.getId(), adminUsername, rejectReason);
        }

        // 5. 保存物品
        goodsRepository.save(goods);

        // 6. 记录审核日志
        log.info("【审核日志】审核人: {}, 物品ID: {}, 审核结果: {}, 拒绝原因: {}, 时间: {}", 
                adminUsername, id, approved ? "通过" : "拒绝", rejectReason, LocalDateTime.now());

        // TODO: 发送通知给发布者
        // notificationService.sendGoodsApprovalNotification(goods.getSellerId(), approved, rejectReason);

        log.info("物品审核完成: goodsId={}, status={}", id, goods.getStatus());
    }

    /**
     * 截断描述（列表展示时只显示前 100 个字符）
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
