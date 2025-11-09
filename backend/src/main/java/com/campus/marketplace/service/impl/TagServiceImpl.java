package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateTagRequest;
import com.campus.marketplace.common.dto.request.MergeTagRequest;
import com.campus.marketplace.common.dto.request.UpdateTagRequest;
import com.campus.marketplace.common.dto.response.TagResponse;
import com.campus.marketplace.common.dto.response.TagStatisticsResponse;
import com.campus.marketplace.common.entity.GoodsTag;
import com.campus.marketplace.common.entity.Tag;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.GoodsTagRepository;
import com.campus.marketplace.repository.TagRepository;
import com.campus.marketplace.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 标签服务实现
 *
 * 实现标签的创建、更新、删除、合并与查询
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final GoodsTagRepository goodsTagRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTag(CreateTagRequest request) {
        String normalizedName = normalizeName(request.name());
        tagRepository.findByNameIgnoreCase(normalizedName).ifPresent(tag -> {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "标签已存在");
        });

        Tag tag = Tag.builder()
                .name(normalizedName)
                .description(request.description())
                .enabled(request.enabled() == null || request.enabled())
                .build();
        tagRepository.save(tag);
        log.info("创建标签成功 tagId={}, name={}", tag.getId(), tag.getName());
        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(Long id, UpdateTagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));

        String normalizedName = normalizeName(request.name());
        tagRepository.findByNameIgnoreCase(normalizedName)
                .filter(existing -> !Objects.equals(existing.getId(), id))
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "标签名称重复");
                });

        tag.setName(normalizedName);
        tag.setDescription(request.description());
        if (request.enabled() != null) {
            tag.setEnabled(request.enabled());
        }
        tagRepository.save(tag);
        log.info("更新标签成功 tagId={}, name={}", id, normalizedName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));

        long bindingCount = goodsTagRepository.countByTagId(id);
        if (bindingCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "仍有商品绑定该标签,无法删除");
        }

        tagRepository.delete(tag);
        log.info("删除标签成功 tagId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeTag(MergeTagRequest request) {
        if (request.sourceTagId().equals(request.targetTagId())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "源标签和目标标签不能相同");
        }
        Tag source = tagRepository.findById(request.sourceTagId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND, "源标签不存在"));
        Tag target = tagRepository.findById(request.targetTagId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND, "目标标签不存在"));

        List<GoodsTag> bindings = goodsTagRepository.findByTagId(source.getId());
        bindings.forEach(binding -> {
            goodsTagRepository.findByGoodsIdAndTagId(binding.getGoodsId(), target.getId())
                    .ifPresentOrElse(existing -> {
                        goodsTagRepository.deleteById(binding.getId());
                    }, () -> {
                        binding.setTagId(target.getId());
                        goodsTagRepository.save(binding);
                    });
        });

        // 删除源标签
        tagRepository.delete(source);
        log.info("合并标签成功 sourceTagId={} -> targetTagId={}, affectedGoods={}",
                source.getId(), target.getId(), bindings.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> listAllTags() {
        return tagRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<TagResponse> listTags(String keyword, Boolean enabled, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Tag> tagPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            tagPage = tagRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        } else if (enabled != null) {
            tagPage = tagRepository.findByEnabled(enabled, pageable);
        } else {
            tagPage = tagRepository.findAll(pageable);
        }

        return tagPage.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagStatisticsResponse> getHotTags(int limit) {
        List<Object[]> results = goodsTagRepository.findTopTagsByUsageCount(org.springframework.data.domain.PageRequest.of(0, limit));
        return results.stream()
                .map(row -> {
                    Long tagId = (Long) row[0];
                    Long count = (Long) row[1];
                    Tag tag = tagRepository.findById(tagId).orElse(null);
                    if (tag == null) return null;
                    return TagStatisticsResponse.builder()
                            .tagId(tagId)
                            .tagName(tag.getName())
                            .goodsCount(count)
                            .enabled(tag.getEnabled())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    // 🎯 BaSui 新增方法实现（标签管理扩展）

    @Override
    @Transactional(readOnly = true)
    public Tag getById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND, "标签不存在"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleEnabled(Long id) {
        Tag tag = getById(id);
        tag.setEnabled(!tag.getEnabled());
        tagRepository.save(tag);
        log.info("切换标签启用状态成功 tagId={}, enabled={}", id, tag.getEnabled());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        int successCount = 0;
        for (Long id : ids) {
            try {
                deleteTag(id);
                successCount++;
            } catch (BusinessException e) {
                log.warn("批量删除标签失败: tagId={}, error={}", id, e.getMessage());
            }
        }
        return successCount;
    }

    @Override
    @Transactional(readOnly = true)
    public TagStatisticsResponse getStatistics(Long id) {
        Tag tag = getById(id);
        long goodsCount = goodsTagRepository.countByTagId(id);

        return TagStatisticsResponse.builder()
                .tagId(id)
                .tagName(tag.getName())
                .goodsCount(goodsCount)
                .enabled(tag.getEnabled())
                .build();
    }

    // 🔧 私有辅助方法

    private String normalizeName(String name) {
        return name.trim();
    }

    private TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .enabled(tag.getEnabled())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}
