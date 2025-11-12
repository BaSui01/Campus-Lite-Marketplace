package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.MarketingCampaign;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.MarketingCampaignRepository;
import com.campus.marketplace.service.MarketingCampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 营销活动服务实现
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketingCampaignServiceImpl implements MarketingCampaignService {

    private final MarketingCampaignRepository marketingCampaignRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CAMPAIGN_STOCK_KEY_PREFIX = "campaign:stock:";
    private static final String CAMPAIGN_LOCK_KEY_PREFIX = "campaign:lock:";

    @Override
    @Transactional
    public MarketingCampaign createCampaign(MarketingCampaign campaign) {
        log.debug("创建营销活动: {}", campaign.getCampaignName());
        
        // 参数校验
        validateCampaign(campaign);
        
        // 设置初始状态
        campaign.setStatus("PENDING");
        campaign.setParticipationCount(0);
        
        // 初始化剩余库存
        if (campaign.getStockLimit() != null) {
            campaign.setStockRemaining(campaign.getStockLimit());
        }
        
        return marketingCampaignRepository.save(campaign);
    }

    @Override
    public List<MarketingCampaign> getMerchantCampaigns(Long merchantId) {
        log.debug("查询商家{}的所有活动", merchantId);
        return marketingCampaignRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    @Override
    public List<MarketingCampaign> getRunningCampaigns() {
        log.debug("查询进行中的活动");
        return marketingCampaignRepository.findRunningCampaigns(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void approveCampaign(Long campaignId) {
        log.debug("审核通过活动: {}", campaignId);
        
        MarketingCampaign campaign = marketingCampaignRepository.findById(campaignId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAMETER, "活动不存在"));
        
        if (!"PENDING".equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "只有待审核的活动才能审核通过");
        }
        
        campaign.setStatus("APPROVED");
        marketingCampaignRepository.save(campaign);
        
        log.info("活动{}审核通过", campaignId);
    }

    @Override
    @Transactional
    public void rejectCampaign(Long campaignId, String reason) {
        log.debug("拒绝活动: {}, 原因: {}", campaignId, reason);
        
        MarketingCampaign campaign = marketingCampaignRepository.findById(campaignId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAMETER, "活动不存在"));
        
        if (!"PENDING".equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "只有待审核的活动才能拒绝");
        }
        
        campaign.setStatus("REJECTED");
        // 拒绝原因可以存储在额外字段中（需要扩展实体）
        marketingCampaignRepository.save(campaign);
        
        log.info("活动{}被拒绝，原因: {}", campaignId, reason);
    }

    @Override
    @Transactional
    public void pauseCampaign(Long campaignId) {
        log.debug("暂停活动: {}", campaignId);
        
        MarketingCampaign campaign = marketingCampaignRepository.findById(campaignId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAMETER, "活动不存在"));
        
        if (!"RUNNING".equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "只有进行中的活动才能暂停");
        }
        
        campaign.setStatus("PAUSED");
        marketingCampaignRepository.save(campaign);
        
        log.info("活动{}已暂停", campaignId);
    }

    @Override
    @Transactional
    public void resumeCampaign(Long campaignId) {
        log.debug("恢复活动: {}", campaignId);
        
        MarketingCampaign campaign = marketingCampaignRepository.findById(campaignId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAMETER, "活动不存在"));
        
        if (!"PAUSED".equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "只有已暂停的活动才能恢复");
        }
        
        // 检查活动时间是否还在有效期内
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(campaign.getStartTime()) || now.isAfter(campaign.getEndTime())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "活动不在有效时间范围内");
        }
        
        campaign.setStatus("RUNNING");
        marketingCampaignRepository.save(campaign);
        
        log.info("活动{}已恢复", campaignId);
    }

    @Override
    @Transactional
    public void endCampaign(Long campaignId) {
        log.debug("结束活动: {}", campaignId);
        
        MarketingCampaign campaign = marketingCampaignRepository.findById(campaignId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAMETER, "活动不存在"));
        
        if ("ENDED".equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "活动已结束");
        }
        
        campaign.setStatus("ENDED");
        marketingCampaignRepository.save(campaign);
        
        log.info("活动{}已结束", campaignId);
    }

    @Override
    @Transactional
    public void autoUpdateCampaignStatus() {
        log.info("开始自动更新活动状态...");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 启动已通过且到达开始时间的活动
        List<MarketingCampaign> upcomingCampaigns = marketingCampaignRepository
            .findUpcomingCampaigns(now, now.plusDays(1));
        
        int startedCount = 0;
        for (MarketingCampaign campaign : upcomingCampaigns) {
            if ("APPROVED".equals(campaign.getStatus()) && 
                !now.isBefore(campaign.getStartTime())) {
                campaign.setStatus("RUNNING");
                marketingCampaignRepository.save(campaign);
                
                // 初始化Redis库存
                if (campaign.getStockLimit() != null && campaign.getStockLimit() > 0) {
                    initializeRedisStock(campaign.getId(), campaign);
                }
                
                startedCount++;
                log.info("活动{}自动启动", campaign.getId());
            }
        }
        
        // 2. 结束已过期的活动
        List<MarketingCampaign> expiredCampaigns = marketingCampaignRepository
            .findExpiredCampaigns(now);
        
        int endedCount = 0;
        for (MarketingCampaign campaign : expiredCampaigns) {
            campaign.setStatus("ENDED");
            marketingCampaignRepository.save(campaign);
            
            // 同步Redis库存到数据库
            syncStockToDatabase(campaign.getId());
            
            // 清理Redis数据
            String stockKey = CAMPAIGN_STOCK_KEY_PREFIX + campaign.getId();
            String counterKey = "campaign:counter:" + campaign.getId();
            redisTemplate.delete(stockKey);
            redisTemplate.delete(counterKey);
            
            endedCount++;
            log.info("活动{}自动结束", campaign.getId());
        }
        
        log.info("活动状态更新完成，启动{}个，结束{}个", startedCount, endedCount);
    }

    @Override
    public boolean deductStock(Long campaignId, int quantity) {
        log.debug("扣减活动{}库存: {}", campaignId, quantity);
        
        // 检查活动状态（从数据库）
        MarketingCampaign campaign = marketingCampaignRepository.findById(campaignId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAMETER, "活动不存在"));
        
        if (!"RUNNING".equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "活动未在进行中");
        }
        
        // 使用Redis原子操作扣减库存（防止超卖）
        String stockKey = CAMPAIGN_STOCK_KEY_PREFIX + campaignId;
        
        // 初始化Redis库存（如果不存在）
        if (!redisTemplate.hasKey(stockKey)) {
            initializeRedisStock(campaignId, campaign);
        }
        
        // 使用Lua脚本原子扣减库存
        String luaScript = 
            "local stock = redis.call('get', KEYS[1]) " +
            "if not stock then " +
            "    return -1 " +  // 库存未初始化
            "end " +
            "stock = tonumber(stock) " +
            "if stock < tonumber(ARGV[1]) then " +
            "    return 0 " +  // 库存不足
            "end " +
            "redis.call('decrby', KEYS[1], ARGV[1]) " +
            "return 1";  // 扣减成功
        
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);
        
        Long result = redisTemplate.execute(
            redisScript,
            Collections.singletonList(stockKey),
            String.valueOf(quantity)
        );
        
        if (result == null || result == -1) {
            log.error("活动{}Redis库存未初始化", campaignId);
            return false;
        }
        
        if (result == 0) {
            log.warn("活动{}库存不足", campaignId);
            return false;
        }
        
        // 异步更新数据库（减少数据库压力）
        asyncUpdateDatabaseStock(campaignId);
        
        log.info("活动{}库存扣减成功", campaignId);
        return true;
    }
    
    /**
     * 初始化Redis库存
     */
    private void initializeRedisStock(Long campaignId, MarketingCampaign campaign) {
        if (campaign.getStockRemaining() == null || campaign.getStockRemaining() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "活动库存无效");
        }
        
        String stockKey = CAMPAIGN_STOCK_KEY_PREFIX + campaignId;
        String lockKey = CAMPAIGN_LOCK_KEY_PREFIX + campaignId;
        
        try {
            // 使用分布式锁防止重复初始化
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                lockKey, "1", java.time.Duration.ofSeconds(10)
            );
            
            if (Boolean.TRUE.equals(locked)) {
                // 再次检查（双重检查）
                if (!redisTemplate.hasKey(stockKey)) {
                    redisTemplate.opsForValue().set(
                        stockKey, 
                        campaign.getStockRemaining(),
                        java.time.Duration.ofDays(7)  // 7天过期
                    );
                    log.info("活动{}Redis库存初始化成功: {}", campaignId, campaign.getStockRemaining());
                }
            }
        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }
    
    /**
     * 异步更新数据库库存（每100次扣减同步一次）
     */
    private void asyncUpdateDatabaseStock(Long campaignId) {
        // 使用Redis计数器，每100次扣减同步一次数据库
        String counterKey = "campaign:counter:" + campaignId;
        Long counter = redisTemplate.opsForValue().increment(counterKey);
        
        if (counter != null && counter % 100 == 0) {
            // 同步到数据库
            syncStockToDatabase(campaignId);
        }
    }
    
    /**
     * 同步Redis库存到数据库
     */
    @Transactional
    public void syncStockToDatabase(Long campaignId) {
        String stockKey = CAMPAIGN_STOCK_KEY_PREFIX + campaignId;
        Object stockObj = redisTemplate.opsForValue().get(stockKey);
        
        if (stockObj != null) {
            int redisStock = Integer.parseInt(stockObj.toString());
            
            MarketingCampaign campaign = marketingCampaignRepository.findById(campaignId)
                .orElse(null);
            
            if (campaign != null) {
                campaign.setStockRemaining(redisStock);
                marketingCampaignRepository.save(campaign);
                log.info("活动{}库存同步到数据库: {}", campaignId, redisStock);
            }
        }
    }
    
    /**
     * 校验活动参数
     */
    private void validateCampaign(MarketingCampaign campaign) {
        if (campaign.getCampaignName() == null || campaign.getCampaignName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "活动名称不能为空");
        }
        
        if (campaign.getCampaignType() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "活动类型不能为空");
        }
        
        if (campaign.getStartTime() == null || campaign.getEndTime() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "活动时间不能为空");
        }
        
        if (campaign.getStartTime().isAfter(campaign.getEndTime())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "活动开始时间不能晚于结束时间");
        }
        
        if (campaign.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "活动开始时间不能早于当前时间");
        }
        
        // 校验活动类型特定字段
        if ("FLASH_SALE".equals(campaign.getCampaignType())) {
            if (campaign.getStockLimit() == null || campaign.getStockLimit() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "秒杀活动必须设置库存限制");
            }
        }
        
        if (campaign.getGoodsIds() == null || campaign.getGoodsIds().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "活动商品列表不能为空");
        }
    }

    @Override
    public MarketingCampaign getCampaignById(Long campaignId) {
        log.debug("获取活动详情: campaignId={}", campaignId);
        
        return marketingCampaignRepository.findById(campaignId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "活动不存在"));
    }

    @Override
    @Transactional
    public MarketingCampaign updateCampaign(Long campaignId, MarketingCampaign campaign) {
        log.debug("更新活动: campaignId={}", campaignId);
        
        MarketingCampaign existingCampaign = marketingCampaignRepository.findById(campaignId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "活动不存在"));
        
        // 只有待审核和已拒绝的活动可以修改
        if (!"PENDING".equals(existingCampaign.getStatus()) && !"REJECTED".equals(existingCampaign.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "只有待审核和已拒绝的活动可以修改");
        }
        
        // 更新字段
        if (campaign.getCampaignName() != null) {
            existingCampaign.setCampaignName(campaign.getCampaignName());
        }
        if (campaign.getCampaignType() != null) {
            existingCampaign.setCampaignType(campaign.getCampaignType());
        }
        if (campaign.getStartTime() != null) {
            existingCampaign.setStartTime(campaign.getStartTime());
        }
        if (campaign.getEndTime() != null) {
            existingCampaign.setEndTime(campaign.getEndTime());
        }
        if (campaign.getDiscountConfig() != null) {
            existingCampaign.setDiscountConfig(campaign.getDiscountConfig());
        }
        if (campaign.getGoodsIds() != null) {
            existingCampaign.setGoodsIds(campaign.getGoodsIds());
        }
        if (campaign.getStockLimit() != null) {
            existingCampaign.setStockLimit(campaign.getStockLimit());
            existingCampaign.setStockRemaining(campaign.getStockLimit());
        }
        
        // 重新校验
        validateCampaign(existingCampaign);
        
        // 重置状态为待审核
        existingCampaign.setStatus("PENDING");
        
        return marketingCampaignRepository.save(existingCampaign);
    }

    @Override
    @Transactional
    public void deleteCampaign(Long campaignId) {
        log.debug("删除活动: campaignId={}", campaignId);
        
        MarketingCampaign campaign = marketingCampaignRepository.findById(campaignId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "活动不存在"));
        
        // 只有待审核、已拒绝、已结束的活动可以删除
        if ("RUNNING".equals(campaign.getStatus()) || "APPROVED".equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "进行中或已通过的活动不能删除");
        }
        
        // 软删除（BaseEntity 已包含 @SQLRestriction(\"deleted = false\")）
        marketingCampaignRepository.delete(campaign);
        
        log.info("活动{}已删除", campaignId);
    }

    @Override
    public MarketingCampaign checkGoodsInCampaign(Long goodsId) {
        log.debug("检查商品{}是否参与活动", goodsId);
        
        LocalDateTime now = LocalDateTime.now();
        
        // 查询进行中的活动
        List<MarketingCampaign> runningCampaigns = marketingCampaignRepository.findRunningCampaigns(now);
        
        // 检查商品是否在活动商品列表中
        for (MarketingCampaign campaign : runningCampaigns) {
            if (campaign.getGoodsIds() != null && campaign.getGoodsIds().contains(goodsId)) {
                log.info("商品{}参与活动: {}", goodsId, campaign.getCampaignName());
                return campaign;
            }
        }
        
        log.debug("商品{}未参与任何活动", goodsId);
        return null;
    }

    @Override
    public java.util.Map<String, Object> getCampaignStatistics(Long merchantId) {
        log.debug("获取活动统计: merchantId={}", merchantId);
        
        List<MarketingCampaign> campaigns;
        if (merchantId != null) {
            campaigns = marketingCampaignRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
        } else {
            campaigns = marketingCampaignRepository.findAll();
        }
        
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        
        // 总活动数
        statistics.put("totalCampaigns", campaigns.size());
        
        // 按状态统计
        long pendingCount = campaigns.stream().filter(c -> "PENDING".equals(c.getStatus())).count();
        long approvedCount = campaigns.stream().filter(c -> "APPROVED".equals(c.getStatus())).count();
        long runningCount = campaigns.stream().filter(c -> "RUNNING".equals(c.getStatus())).count();
        long pausedCount = campaigns.stream().filter(c -> "PAUSED".equals(c.getStatus())).count();
        long endedCount = campaigns.stream().filter(c -> "ENDED".equals(c.getStatus())).count();
        long rejectedCount = campaigns.stream().filter(c -> "REJECTED".equals(c.getStatus())).count();
        
        java.util.Map<String, Long> statusStats = new java.util.HashMap<>();
        statusStats.put("PENDING", pendingCount);
        statusStats.put("APPROVED", approvedCount);
        statusStats.put("RUNNING", runningCount);
        statusStats.put("PAUSED", pausedCount);
        statusStats.put("ENDED", endedCount);
        statusStats.put("REJECTED", rejectedCount);
        statistics.put("statusStats", statusStats);
        
        // 总销售额
        java.math.BigDecimal totalSalesAmount = campaigns.stream()
            .filter(c -> c.getSalesAmount() != null)
            .map(MarketingCampaign::getSalesAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        statistics.put("totalSalesAmount", totalSalesAmount);
        
        // 总参与人数
        int totalParticipation = campaigns.stream()
            .filter(c -> c.getParticipationCount() != null)
            .mapToInt(MarketingCampaign::getParticipationCount)
            .sum();
        statistics.put("totalParticipation", totalParticipation);
        
        // 平均转化率（简化计算：参与人数 / 总活动数）
        double avgConversionRate = campaigns.isEmpty() ? 0.0 : (double) totalParticipation / campaigns.size();
        statistics.put("avgConversionRate", avgConversionRate);
        
        log.info("活动统计完成: totalCampaigns={}, runningCampaigns={}, totalSalesAmount={}", 
            campaigns.size(), runningCount, totalSalesAmount);
        
        return statistics;
    }
}
