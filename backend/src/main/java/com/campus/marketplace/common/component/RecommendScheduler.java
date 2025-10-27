package com.campus.marketplace.common.component;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.repository.CampusRepository;
import com.campus.marketplace.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 热门榜单刷新任务
 *
 * - 定时按校区刷新热门榜单，结果写入 Redis
 * - 使用 RecommendService 内部的分布式锁保障幂等
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendScheduler {

    private final RecommendService recommendService;
    private final CampusRepository campusRepository;

    @Value("${recommend.hot.topN:50}")
    private int defaultTopN;

    /**
     * 每5分钟刷新一次各校区热榜
     */
    @Scheduled(fixedDelayString = "${recommend.hot.refresh.interval.ms:300000}")
    public void refreshHotRankingJob() {
        try {
            // 刷新全局热榜
            recommendService.refreshHotRanking(null, defaultTopN);

            // 刷新各校区热榜（仅ACTIVE）
            List<Campus> campuses = campusRepository.findAll();
            for (Campus c : campuses) {
                if (c.getStatus() == CampusStatus.ACTIVE) {
                    recommendService.refreshHotRanking(c.getId(), defaultTopN);
                }
            }
            log.debug("热门榜单刷新任务完成: topN={}", defaultTopN);
        } catch (Exception e) {
            log.error("热门榜单刷新任务失败", e);
        }
    }
}
