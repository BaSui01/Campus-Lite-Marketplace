package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateGoodsRequest;
import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.dto.response.GoodsDetailResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.dto.response.TagResponse;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.GoodsTag;
import com.campus.marketplace.common.entity.Tag;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.common.utils.EncryptUtil;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.GoodsTagRepository;
import com.campus.marketplace.repository.TagRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.GoodsService;
import com.campus.marketplace.service.FollowService;
import com.campus.marketplace.service.MessageService;
import com.campus.marketplace.service.SubscriptionService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 物品服务实现类
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final GoodsRepository goodsRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final com.campus.marketplace.service.ComplianceService complianceService;
    private final MessageService messageService;
    private final TagRepository tagRepository;
    private final GoodsTagRepository goodsTagRepository;
    private final FollowService followService;
    private final SubscriptionService subscriptionService;
    private final EncryptUtil encryptUtil;

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

        // 3. 合规：文本与图片
        String filteredTitle;
        String filteredDescription;
        if (complianceService != null) {
            var titleMod = complianceService.moderateText(request.title(), "GOODS_TITLE");
            var descMod = complianceService.moderateText(request.description(), "GOODS_DESC");
            if ((titleMod.hit() && titleMod.action() == com.campus.marketplace.common.enums.ComplianceAction.BLOCK)
                    || (descMod.hit() && descMod.action() == com.campus.marketplace.common.enums.ComplianceAction.BLOCK)) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "包含敏感词，请修改后再发布");
            }
            filteredTitle = titleMod.filteredText();
            filteredDescription = descMod.filteredText();
            if (request.images() != null && !request.images().isEmpty()) {
                var imgRes = complianceService.scanImages(request.images(), "GOODS_IMAGES");
                if (imgRes.action() == com.campus.marketplace.service.ComplianceService.ImageAction.REJECT) {
                    throw new BusinessException(ErrorCode.INVALID_PARAMETER, "图片未通过安全检测");
                }
            }
        } else {
            filteredTitle = sensitiveWordFilter.filter(request.title());
            filteredDescription = sensitiveWordFilter.filter(request.description());
        }

        // 4. 创建物品
        Goods goods = Goods.builder()
                .title(filteredTitle)
                .description(filteredDescription)
                .price(request.price())
                .categoryId(request.categoryId())
                .sellerId(user.getId())
                .campusId(user.getCampusId())
                .status(GoodsStatus.PENDING) // 待审核
                .viewCount(0)
                .favoriteCount(0)
                .images(request.images().toArray(new String[0]))
                .build();

        // 5. 保存物品
        goodsRepository.save(goods);
        syncGoodsTags(goods.getId(), request.tagIds());

        log.info("物品发布成功: goodsId={}, sellerId={}, title={}", 
                goods.getId(), user.getId(), goods.getTitle());

        return goods.getId();
    }

    /**
     * 查询物品列表
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "goods:list",
            key = "T(java.util.Objects).hash(#keyword,#categoryId,#minPrice,#maxPrice,#page,#size,#sortBy,#sortDirection,#tagIds)",
            unless = "#result == null")
    public Page<GoodsResponse> listGoods(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String sortDirection,
            List<Long> tagIds
    ) {
        List<Long> sanitizedTagIds = tagIds == null
                ? List.of()
                : tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        log.info("查询物品列表: keyword={}, categoryId={}, minPrice={}, maxPrice={}, page={}, size={}, tags={}",
                keyword, categoryId, minPrice, maxPrice, page, size, sanitizedTagIds);

        if (sanitizedTagIds.size() > 10) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "最多选择 10 个标签");
        }

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

        Long campusFilter = null;
        try {
            if (SecurityUtil.isAuthenticated() && !SecurityUtil.hasAuthority(PermissionCodes.SYSTEM_CAMPUS_CROSS)) {
                String username = SecurityUtil.getCurrentUsername();
                User u = userRepository.findByUsername(username).orElse(null);
                campusFilter = u != null ? u.getCampusId() : null;
            }
        } catch (Exception ignored) {
        }

        List<Long> goodsIdsFilter = null;
        if (!sanitizedTagIds.isEmpty()) {
            List<Long> goodsIds = goodsTagRepository.findGoodsIdsByAllTagIds(
                    sanitizedTagIds,
                    sanitizedTagIds.size()
            );
            if (goodsIds.isEmpty()) {
                return Page.empty(pageable);
            }
            goodsIdsFilter = goodsIds.stream().distinct().toList();
        }

        Page<Goods> goodsPage = goodsIdsFilter == null
                ? goodsRepository.findByConditionsWithCampus(
                GoodsStatus.APPROVED,
                categoryId,
                minPrice,
                maxPrice,
                keyword,
                campusFilter,
                pageable
        )
                : goodsRepository.findByConditionsWithCampusAndIds(
                GoodsStatus.APPROVED,
                categoryId,
                minPrice,
                maxPrice,
                keyword,
                campusFilter,
                goodsIdsFilter,
                pageable
        );

        Map<Long, List<TagResponse>> tagsMap = loadTagsForGoods(
                goodsPage.getContent().stream().map(Goods::getId).toList()
        );

        return goodsPage.map(goods -> convertToResponse(goods, tagsMap));
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

        // 2. 校区鉴权：无跨校权限的用户仅可访问同校区资源
        if (SecurityUtil.isAuthenticated() && !SecurityUtil.hasAuthority(PermissionCodes.SYSTEM_CAMPUS_CROSS)) {
            String username = SecurityUtil.getCurrentUsername();
            User current = userRepository.findByUsername(username).orElse(null);
            if (current != null && goods.getCampusId() != null && current.getCampusId() != null
                    && !goods.getCampusId().equals(current.getCampusId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "跨校区访问被禁止");
            }
        }

        // 3. 增加浏览量
        goods.incrementViewCount();
        goodsRepository.save(goods);

        // 4. 转换为响应 DTO
        return convertToDetailResponse(goods);
    }

    /**
     * 转换为列表响应 DTO（不包含标签）
     */
    private GoodsResponse convertToResponse(Goods goods) {
        String categoryName = categoryRepository.findById(goods.getCategoryId())
                .map(Category::getName)
                .orElse("未知分类");
        String sellerUsername = userRepository.findById(goods.getSellerId())
                .map(User::getUsername)
                .orElse("未知用户");
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
        if (seller == null && goods.getSellerId() != null) {
            seller = userRepository.findById(goods.getSellerId())
                    .orElse(null);
        }
        if (seller == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "卖家信息缺失");
        }
        GoodsDetailResponse.SellerInfo sellerInfo = GoodsDetailResponse.SellerInfo.builder()
                .id(seller.getId())
                .username(seller.getUsername())
                .avatar(seller.getAvatar())
                .points(seller.getPoints())
                .phone(seller.getPhone() != null ? encryptUtil.maskPhone(seller.getPhone()) : null)
                .email(seller.getEmail() != null ? encryptUtil.maskEmail(seller.getEmail()) : null)
                .build();

        // 转换图片数组为列表
        List<String> images = goods.getImages() != null 
                ? Arrays.asList(goods.getImages()) 
                : List.of();

        List<TagResponse> tags = loadTagsForGoods(List.of(goods.getId()))
                .getOrDefault(goods.getId(), List.of());

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
                .tags(tags)
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

        Map<Long, List<TagResponse>> tagsMap = loadTagsForGoods(
                goodsPage.getContent().stream().map(Goods::getId).toList()
        );

        // 转换为响应 DTO
        return goodsPage.map(goods -> convertToResponse(goods, tagsMap));
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

        // 7. 发送系统通知给发布者
        try {
            String notificationContent = approved
                    ? String.format("您发布的物品《%s》已审核通过，现已上架！", goods.getTitle())
                    : String.format("您发布的物品《%s》未通过审核。原因：%s", goods.getTitle(), rejectReason);

            SendMessageRequest messageRequest = new SendMessageRequest(
                    goods.getSellerId(),
                    com.campus.marketplace.common.enums.MessageType.SYSTEM,
                    notificationContent
            );

            // 注意：此处发送系统消息可能因为 SecurityContext 是管理员而失败
            // MessageService 需要允许系统发送消息（无需验证发送者）
            messageService.sendMessage(messageRequest);
            
            log.info("✅ 审核通知已发送: sellerId={}, approved={}", goods.getSellerId(), approved);
        } catch (Exception e) {
            // 通知失败不影响审核流程
            log.error("⚠️ 发送审核通知失败: sellerId={}, goodsId={}", goods.getSellerId(), id, e);
        }

        if (approved) {
            followService.notifyFollowersOnGoodsApproved(goods);
            subscriptionService.notifySubscribersOnGoodsApproved(goods);
        }

        log.info("物品审核完成: goodsId={}, status={}", id, goods.getStatus());
    }

    private Map<Long, List<TagResponse>> loadTagsForGoods(List<Long> goodsIds) {
        if (goodsIds == null || goodsIds.isEmpty()) {
            return Map.of();
        }
        List<GoodsTag> bindings = goodsTagRepository.findByGoodsIdIn(goodsIds);
        if (bindings.isEmpty()) {
            return Map.of();
        }
        Set<Long> tagIds = bindings.stream()
                .map(GoodsTag::getTagId)
                .collect(Collectors.toCollection(HashSet::new));
        Map<Long, Tag> tagMap = StreamSupport.stream(
                        tagRepository.findAllById(tagIds).spliterator(), false)
                .collect(Collectors.toMap(Tag::getId, tag -> tag));

        Map<Long, List<TagResponse>> result = new HashMap<>();
        for (GoodsTag binding : bindings) {
            Tag tag = tagMap.get(binding.getTagId());
            if (tag == null) {
                continue;
            }
            result.computeIfAbsent(binding.getGoodsId(), key -> new ArrayList<>())
                    .add(toTagResponse(tag));
        }
        result.values().forEach(list -> list.sort(Comparator.comparing(TagResponse::name)));
        return result;
    }

    private TagResponse toTagResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .enabled(tag.getEnabled())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }

    private void syncGoodsTags(Long goodsId, List<Long> tagIds) {
        goodsTagRepository.deleteByGoodsId(goodsId);
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<Long> distinct = tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (distinct.size() > 10) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "最多绑定 10 个标签");
        }
        List<Tag> tags = StreamSupport.stream(
                        tagRepository.findAllById(distinct).spliterator(), false)
                .toList();
        if (tags.size() != distinct.size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND, "存在已失效的标签");
        }
        tags.forEach(tag -> {
            if (Boolean.FALSE.equals(tag.getEnabled())) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "标签已被禁用: " + tag.getName());
            }
        });
        distinct.forEach(tagId -> goodsTagRepository.save(
                GoodsTag.builder().goodsId(goodsId).tagId(tagId).build()
        ));
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

    

    private GoodsResponse convertToResponse(Goods goods, Map<Long, List<TagResponse>> tagsMap) {
        GoodsResponse base = convertToResponse(goods);
        base.setTags(tagsMap.getOrDefault(goods.getId(), List.of()));
        return base;
    }
}
