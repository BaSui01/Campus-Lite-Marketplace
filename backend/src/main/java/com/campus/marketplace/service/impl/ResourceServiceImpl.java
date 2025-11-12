package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.Resource;
import com.campus.marketplace.common.enums.ResourceType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.ResourceRepository;
import com.campus.marketplace.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学习资源服务实现
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Resource> listResources(int page, int size, String type, String category, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (keyword != null && !keyword.isEmpty()) {
            return resourceRepository.searchByKeyword(keyword, pageable);
        } else if (type != null) {
            return resourceRepository.findByType(ResourceType.valueOf(type), pageable);
        } else if (category != null) {
            return resourceRepository.findByCategory(category, pageable);
        } else {
            return resourceRepository.findAll(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getResourceDetail(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "资源不存在"));

        // 增加浏览量
        resource.setViewCount(resource.getViewCount() + 1);
        resourceRepository.save(resource);

        return resource;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordDownload(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "资源不存在"));

        // 增加下载次数
        resource.setDownloadCount(resource.getDownloadCount() + 1);
        resourceRepository.save(resource);

        log.info("资源下载记录: resourceId={}, downloadCount={}", resourceId, resource.getDownloadCount());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Resource> getHotResources(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return resourceRepository.findAllByOrderByDownloadCountDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Resource> getMyResources(int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return resourceRepository.findByUploaderId(userId, pageable);
    }
}
