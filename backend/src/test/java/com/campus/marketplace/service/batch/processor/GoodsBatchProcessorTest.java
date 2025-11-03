package com.campus.marketplace.service.batch.processor;

import com.campus.marketplace.common.dto.request.GoodsBatchRequest;
import com.campus.marketplace.common.entity.BatchTaskItem;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.BatchOperationType;
import com.campus.marketplace.common.enums.BatchType;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.service.AuditLogService;
import com.campus.marketplace.service.CacheService;
import com.campus.marketplace.service.batch.BatchProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("商品批量处理器测试")
class GoodsBatchProcessorTest {

    @Mock
    private GoodsRepository goodsRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CacheService cacheService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GoodsBatchProcessor processor;

    @BeforeEach
    void setUp() {
        // 准备工作
    }

    @Test
    @DisplayName("应该返回正确的支持类型")
    void shouldReturnCorrectSupportedType() {
        // Act
        BatchType type = processor.getSupportedType();

        // Assert
        assertThat(type).isEqualTo(BatchType.GOODS_BATCH);
    }

    @Test
    @DisplayName("应该能成功批量上架商品")
    void shouldBatchOnlineGoodsSuccessfully() throws Exception {
        // Arrange
        Long goodsId = 1L;
        GoodsBatchRequest request = GoodsBatchRequest.builder()
                .operation(BatchOperationType.BATCH_ONLINE)
                .targetIds(List.of(goodsId))
                .reason("测试上架")
                .build();

        BatchTaskItem item = BatchTaskItem.builder()
                .targetId(goodsId)
                .inputData("{\"operation\":\"BATCH_ONLINE\"}")
                .build();

        Goods goods = Goods.builder()
                .title("测试商品")
                .price(BigDecimal.valueOf(99.99))
                .status(GoodsStatus.PENDING)
                .build();
        goods.setId(goodsId);

        when(objectMapper.readValue(anyString(), eq(GoodsBatchRequest.class)))
                .thenReturn(request);
        when(goodsRepository.findById(goodsId)).thenReturn(Optional.of(goods));
        when(goodsRepository.save(any(Goods.class))).thenReturn(goods);

        // Act & Assert
        try (MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> securityUtil = 
                mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class)) {
            securityUtil.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUserId)
                    .thenReturn(1L);
            securityUtil.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                    .thenReturn("admin");

            BatchProcessor.BatchItemResult result = processor.processItem(item);

            // Assert
            assertThat(result.success()).isTrue();
            assertThat(result.message()).contains("上架成功");
            verify(goodsRepository).save(argThat(g -> 
                g.getStatus() == GoodsStatus.APPROVED
            ));
            verify(cacheService).delete("goods:" + goodsId);
            verify(auditLogService).logEntityChange(anyLong(), anyString(), any(), anyString(), 
                anyLong(), anyMap(), anyMap());
        }
    }

    @Test
    @DisplayName("应该能成功批量下架商品")
    void shouldBatchOfflineGoodsSuccessfully() throws Exception {
        // Arrange
        Long goodsId = 1L;
        GoodsBatchRequest request = GoodsBatchRequest.builder()
                .operation(BatchOperationType.BATCH_OFFLINE)
                .targetIds(List.of(goodsId))
                .reason("测试下架")
                .build();

        BatchTaskItem item = BatchTaskItem.builder()
                .targetId(goodsId)
                .inputData("{\"operation\":\"BATCH_OFFLINE\"}")
                .build();

        Goods goods = Goods.builder()
                .title("测试商品")
                .price(BigDecimal.valueOf(99.99))
                .status(GoodsStatus.APPROVED)
                .build();
        goods.setId(goodsId);

        when(objectMapper.readValue(anyString(), eq(GoodsBatchRequest.class)))
                .thenReturn(request);
        when(goodsRepository.findById(goodsId)).thenReturn(Optional.of(goods));
        when(goodsRepository.save(any(Goods.class))).thenReturn(goods);

        // Act & Assert
        try (MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> securityUtil = 
                mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class)) {
            securityUtil.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUserId)
                    .thenReturn(1L);
            securityUtil.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                    .thenReturn("admin");

            BatchProcessor.BatchItemResult result = processor.processItem(item);

            // Assert
            assertThat(result.success()).isTrue();
            assertThat(result.message()).contains("下架成功");
            verify(goodsRepository).save(argThat(g -> 
                g.getStatus() == GoodsStatus.OFFLINE
            ));
            verify(cacheService).delete("goods:" + goodsId);
        }
    }

    @Test
    @DisplayName("商品不存在时应返回失败")
    void shouldReturnFailureWhenGoodsNotFound() throws Exception {
        // Arrange
        Long goodsId = 999L;
        GoodsBatchRequest request = GoodsBatchRequest.builder()
                .operation(BatchOperationType.BATCH_ONLINE)
                .targetIds(List.of(goodsId))
                .build();

        BatchTaskItem item = BatchTaskItem.builder()
                .targetId(goodsId)
                .inputData("{\"operation\":\"BATCH_ONLINE\"}")
                .build();

        when(objectMapper.readValue(anyString(), eq(GoodsBatchRequest.class)))
                .thenReturn(request);
        when(goodsRepository.findById(goodsId)).thenReturn(Optional.empty());

        // Act
        BatchProcessor.BatchItemResult result = processor.processItem(item);

        // Assert
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("商品不存在");
        verify(goodsRepository, never()).save(any());
    }

    @Test
    @DisplayName("已上架商品再次上架应返回失败")
    void shouldReturnFailureWhenGoodsAlreadyOnline() throws Exception {
        // Arrange
        Long goodsId = 1L;
        GoodsBatchRequest request = GoodsBatchRequest.builder()
                .operation(BatchOperationType.BATCH_ONLINE)
                .targetIds(List.of(goodsId))
                .build();

        BatchTaskItem item = BatchTaskItem.builder()
                .targetId(goodsId)
                .inputData("{\"operation\":\"BATCH_ONLINE\"}")
                .build();

        Goods goods = Goods.builder()
                .title("测试商品")
                .price(BigDecimal.valueOf(99.99))
                .status(GoodsStatus.APPROVED)
                .build();
        goods.setId(goodsId);

        when(objectMapper.readValue(anyString(), eq(GoodsBatchRequest.class)))
                .thenReturn(request);
        when(goodsRepository.findById(goodsId)).thenReturn(Optional.of(goods));

        // Act
        BatchProcessor.BatchItemResult result = processor.processItem(item);

        // Assert
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("已上架");
        verify(goodsRepository, never()).save(any());
    }

    @Test
    @DisplayName("应该能成功批量删除商品")
    void shouldBatchDeleteGoodsSuccessfully() throws Exception {
        // Arrange
        Long goodsId = 1L;
        GoodsBatchRequest request = GoodsBatchRequest.builder()
                .operation(BatchOperationType.BATCH_DELETE)
                .targetIds(List.of(goodsId))
                .reason("测试删除")
                .build();

        BatchTaskItem item = BatchTaskItem.builder()
                .targetId(goodsId)
                .inputData("{\"operation\":\"BATCH_DELETE\"}")
                .build();

        Goods goods = Goods.builder()
                .title("测试商品")
                .price(BigDecimal.valueOf(99.99))
                .status(GoodsStatus.OFFLINE)
                .build();
        goods.setId(goodsId);

        when(objectMapper.readValue(anyString(), eq(GoodsBatchRequest.class)))
                .thenReturn(request);
        when(goodsRepository.findById(goodsId)).thenReturn(Optional.of(goods));
        when(goodsRepository.save(any(Goods.class))).thenReturn(goods);

        // Act & Assert
        try (MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> securityUtil = 
                mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class)) {
            securityUtil.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUserId)
                    .thenReturn(1L);
            securityUtil.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                    .thenReturn("admin");

            BatchProcessor.BatchItemResult result = processor.processItem(item);

            // Assert
            assertThat(result.success()).isTrue();
            assertThat(result.message()).contains("删除成功");
            verify(goodsRepository).save(argThat(Goods::isDeleted));
            verify(cacheService).delete("goods:" + goodsId);
        }
    }

    @Test
    @DisplayName("已售出商品无法删除")
    void shouldNotDeleteSoldGoods() throws Exception {
        // Arrange
        Long goodsId = 1L;
        GoodsBatchRequest request = GoodsBatchRequest.builder()
                .operation(BatchOperationType.BATCH_DELETE)
                .targetIds(List.of(goodsId))
                .build();

        BatchTaskItem item = BatchTaskItem.builder()
                .targetId(goodsId)
                .inputData("{\"operation\":\"BATCH_DELETE\"}")
                .build();

        Goods goods = Goods.builder()
                .title("测试商品")
                .price(BigDecimal.valueOf(99.99))
                .status(GoodsStatus.SOLD)
                .build();
        goods.setId(goodsId);

        when(objectMapper.readValue(anyString(), eq(GoodsBatchRequest.class)))
                .thenReturn(request);
        when(goodsRepository.findById(goodsId)).thenReturn(Optional.of(goods));

        // Act
        BatchProcessor.BatchItemResult result = processor.processItem(item);

        // Assert
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("已售出商品无法删除");
        verify(goodsRepository, never()).save(any());
    }
}
