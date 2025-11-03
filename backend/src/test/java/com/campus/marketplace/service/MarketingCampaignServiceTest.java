package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.MarketingCampaign;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.MarketingCampaignRepository;
import com.campus.marketplace.service.impl.MarketingCampaignServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 营销活动服务单元测试
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("营销活动服务测试")
class MarketingCampaignServiceTest {

    @Mock
    private MarketingCampaignRepository marketingCampaignRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private MarketingCampaignServiceImpl marketingCampaignService;

    private MarketingCampaign testCampaign;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testCampaign = MarketingCampaign.builder()
                .merchantId(100L)
                .campaignName("双十一秒杀")
                .campaignType("FLASH_SALE")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status("PENDING")
                .stockLimit(100)
                .stockRemaining(100)
                .goodsIds(Arrays.asList(1L, 2L, 3L))
                .participationCount(0)
                .salesAmount(BigDecimal.ZERO)
                .build();
        
        // 手动设置ID（因为Builder不支持）
        testCampaign.setId(1L);

        // Mock RedisTemplate
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("创建活动 - 成功")
    void testCreateCampaign_Success() {
        // Arrange
        when(marketingCampaignRepository.save(any(MarketingCampaign.class)))
                .thenReturn(testCampaign);

        // Act
        MarketingCampaign result = marketingCampaignService.createCampaign(testCampaign);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getParticipationCount()).isEqualTo(0);
        verify(marketingCampaignRepository).save(any(MarketingCampaign.class));
    }

    @Test
    @DisplayName("创建活动 - 活动名称为空")
    void testCreateCampaign_EmptyName() {
        // Arrange
        testCampaign.setCampaignName("");

        // Act & Assert
        assertThatThrownBy(() -> marketingCampaignService.createCampaign(testCampaign))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("活动名称不能为空");
    }

    @Test
    @DisplayName("创建活动 - 秒杀活动无库存限制")
    void testCreateCampaign_FlashSaleWithoutStock() {
        // Arrange
        testCampaign.setStockLimit(null);

        // Act & Assert
        assertThatThrownBy(() -> marketingCampaignService.createCampaign(testCampaign))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("秒杀活动必须设置库存限制");
    }

    @Test
    @DisplayName("创建活动 - 开始时间晚于结束时间")
    void testCreateCampaign_InvalidTime() {
        // Arrange
        testCampaign.setStartTime(LocalDateTime.now().plusDays(2));
        testCampaign.setEndTime(LocalDateTime.now().plusDays(1));

        // Act & Assert
        assertThatThrownBy(() -> marketingCampaignService.createCampaign(testCampaign))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("活动开始时间不能晚于结束时间");
    }

    @Test
    @DisplayName("审核通过活动 - 成功")
    void testApproveCampaign_Success() {
        // Arrange
        when(marketingCampaignRepository.findById(1L))
                .thenReturn(Optional.of(testCampaign));
        when(marketingCampaignRepository.save(any(MarketingCampaign.class)))
                .thenReturn(testCampaign);

        // Act
        marketingCampaignService.approveCampaign(1L);

        // Assert
        verify(marketingCampaignRepository).save(argThat(campaign ->
                "APPROVED".equals(campaign.getStatus())
        ));
    }

    @Test
    @DisplayName("审核通过活动 - 活动不存在")
    void testApproveCampaign_NotFound() {
        // Arrange
        when(marketingCampaignRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> marketingCampaignService.approveCampaign(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("活动不存在");
    }

    @Test
    @DisplayName("审核通过活动 - 状态不是待审核")
    void testApproveCampaign_InvalidStatus() {
        // Arrange
        testCampaign.setStatus("RUNNING");
        when(marketingCampaignRepository.findById(1L))
                .thenReturn(Optional.of(testCampaign));

        // Act & Assert
        assertThatThrownBy(() -> marketingCampaignService.approveCampaign(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只有待审核的活动才能审核通过");
    }

    @Test
    @DisplayName("暂停活动 - 成功")
    void testPauseCampaign_Success() {
        // Arrange
        testCampaign.setStatus("RUNNING");
        when(marketingCampaignRepository.findById(1L))
                .thenReturn(Optional.of(testCampaign));
        when(marketingCampaignRepository.save(any(MarketingCampaign.class)))
                .thenReturn(testCampaign);

        // Act
        marketingCampaignService.pauseCampaign(1L);

        // Assert
        verify(marketingCampaignRepository).save(argThat(campaign ->
                "PAUSED".equals(campaign.getStatus())
        ));
    }

    @Test
    @DisplayName("恢复活动 - 成功")
    void testResumeCampaign_Success() {
        // Arrange
        testCampaign.setStatus("PAUSED");
        testCampaign.setStartTime(LocalDateTime.now().minusHours(1));
        testCampaign.setEndTime(LocalDateTime.now().plusHours(1));
        when(marketingCampaignRepository.findById(1L))
                .thenReturn(Optional.of(testCampaign));
        when(marketingCampaignRepository.save(any(MarketingCampaign.class)))
                .thenReturn(testCampaign);

        // Act
        marketingCampaignService.resumeCampaign(1L);

        // Assert
        verify(marketingCampaignRepository).save(argThat(campaign ->
                "RUNNING".equals(campaign.getStatus())
        ));
    }

    @Test
    @DisplayName("恢复活动 - 活动已过期")
    void testResumeCampaign_Expired() {
        // Arrange
        testCampaign.setStatus("PAUSED");
        testCampaign.setStartTime(LocalDateTime.now().minusDays(2));
        testCampaign.setEndTime(LocalDateTime.now().minusDays(1));
        when(marketingCampaignRepository.findById(1L))
                .thenReturn(Optional.of(testCampaign));

        // Act & Assert
        assertThatThrownBy(() -> marketingCampaignService.resumeCampaign(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("活动不在有效时间范围内");
    }

    @Test
    @DisplayName("结束活动 - 成功")
    void testEndCampaign_Success() {
        // Arrange
        testCampaign.setStatus("RUNNING");
        when(marketingCampaignRepository.findById(1L))
                .thenReturn(Optional.of(testCampaign));
        when(marketingCampaignRepository.save(any(MarketingCampaign.class)))
                .thenReturn(testCampaign);

        // Act
        marketingCampaignService.endCampaign(1L);

        // Assert
        verify(marketingCampaignRepository).save(argThat(campaign ->
                "ENDED".equals(campaign.getStatus())
        ));
    }

    @Test
    @DisplayName("获取商家活动列表")
    void testGetMerchantCampaigns() {
        // Arrange
        List<MarketingCampaign> campaigns = Arrays.asList(testCampaign);
        when(marketingCampaignRepository.findByMerchantIdOrderByCreatedAtDesc(100L))
                .thenReturn(campaigns);

        // Act
        List<MarketingCampaign> result = marketingCampaignService.getMerchantCampaigns(100L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMerchantId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("获取进行中的活动")
    void testGetRunningCampaigns() {
        // Arrange
        testCampaign.setStatus("RUNNING");
        List<MarketingCampaign> campaigns = Arrays.asList(testCampaign);
        when(marketingCampaignRepository.findRunningCampaigns(any(LocalDateTime.class)))
                .thenReturn(campaigns);

        // Act
        List<MarketingCampaign> result = marketingCampaignService.getRunningCampaigns();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("RUNNING");
    }

    @Test
    @DisplayName("扣减库存 - 成功")
    void testDeductStock_Success() {
        // Arrange
        testCampaign.setStatus("RUNNING");
        when(marketingCampaignRepository.findById(1L))
                .thenReturn(Optional.of(testCampaign));
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        when(redisTemplate.execute(any(), anyList(), any())).thenReturn(1L);

        // Act
        boolean result = marketingCampaignService.deductStock(1L, 1);

        // Assert
        assertThat(result).isTrue();
        verify(redisTemplate).execute(any(), anyList(), any());
    }

    @Test
    @DisplayName("扣减库存 - 活动不存在")
    void testDeductStock_CampaignNotFound() {
        // Arrange
        when(marketingCampaignRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> marketingCampaignService.deductStock(1L, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("活动不存在");
    }

    @Test
    @DisplayName("扣减库存 - 活动未进行")
    void testDeductStock_CampaignNotRunning() {
        // Arrange
        testCampaign.setStatus("PENDING");
        when(marketingCampaignRepository.findById(1L))
                .thenReturn(Optional.of(testCampaign));

        // Act & Assert
        assertThatThrownBy(() -> marketingCampaignService.deductStock(1L, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("活动未在进行中");
    }

    @Test
    @DisplayName("扣减库存 - 库存不足")
    void testDeductStock_InsufficientStock() {
        // Arrange
        testCampaign.setStatus("RUNNING");
        when(marketingCampaignRepository.findById(1L))
                .thenReturn(Optional.of(testCampaign));
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        when(redisTemplate.execute(any(), anyList(), any())).thenReturn(0L); // 库存不足

        // Act
        boolean result = marketingCampaignService.deductStock(1L, 1);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("自动更新活动状态 - 启动和结束")
    void testAutoUpdateCampaignStatus() {
        // Arrange
        MarketingCampaign upcomingCampaign = MarketingCampaign.builder()
                .status("APPROVED")
                .startTime(LocalDateTime.now().minusMinutes(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .stockLimit(100)
                .stockRemaining(100)
                .build();
        upcomingCampaign.setId(1L);

        MarketingCampaign expiredCampaign = MarketingCampaign.builder()
                .status("RUNNING")
                .startTime(LocalDateTime.now().minusDays(2))
                .endTime(LocalDateTime.now().minusDays(1))
                .build();
        expiredCampaign.setId(2L);

        when(marketingCampaignRepository.findUpcomingCampaigns(any(), any()))
                .thenReturn(Arrays.asList(upcomingCampaign));
        when(marketingCampaignRepository.findExpiredCampaigns(any()))
                .thenReturn(Arrays.asList(expiredCampaign));
        when(marketingCampaignRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(valueOperations.setIfAbsent(anyString(), any(), any())).thenReturn(true);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // Act
        marketingCampaignService.autoUpdateCampaignStatus();

        // Assert
        verify(marketingCampaignRepository, times(2)).save(any(MarketingCampaign.class));
        verify(redisTemplate, atLeast(1)).delete(anyString());
    }
}
