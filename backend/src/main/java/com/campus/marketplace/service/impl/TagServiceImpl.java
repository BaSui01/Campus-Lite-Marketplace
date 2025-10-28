package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateTagRequest;
import com.campus.marketplace.common.dto.request.MergeTagRequest;
import com.campus.marketplace.common.dto.request.UpdateTagRequest;
import com.campus.marketplace.common.dto.response.TagResponse;
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
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "仍有商品绑定该标签，无法删除");
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
