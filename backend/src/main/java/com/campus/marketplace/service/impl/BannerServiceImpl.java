package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.Banner;
import com.campus.marketplace.common.enums.BannerStatus;
import com.campus.marketplace.repository.BannerRepository;
import com.campus.marketplace.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 轮播图服务实现类
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    @Override
    @Cacheable(value = "banners:active", unless = "#result == null || #result.isEmpty()")
    public List<Banner> getActiveBanners() {
        log.debug("从数据库获取启用的轮播图");
        
        // 获取所有启用的轮播图
        List<Banner> allBanners = bannerRepository.findByStatusOrderBySortOrderAsc(BannerStatus.ENABLED);
        
        // 过滤出在有效期内的轮播图
        List<Banner> activeBanners = allBanners.stream()
            .filter(Banner::isInValidPeriod)
            .collect(Collectors.toList());
        
        log.info("获取到 {} 个启用的轮播图", activeBanners.size());
        return activeBanners;
    }

    @Override
    @Transactional
    public void recordClick(Long id) {
        bannerRepository.findById(id).ifPresent(banner -> {
            banner.incrementClickCount();
            bannerRepository.save(banner);
            log.debug("轮播图点击次数 +1: id={}, clickCount={}", id, banner.getClickCount());
        });
    }

    @Override
    @Transactional
    public void recordView(Long id) {
        bannerRepository.findById(id).ifPresent(banner -> {
            banner.incrementViewCount();
            bannerRepository.save(banner);
            log.trace("轮播图展示次数 +1: id={}, viewCount={}", id, banner.getViewCount());
        });
    }
}
