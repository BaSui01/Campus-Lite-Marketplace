package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ExportJob;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.ExportJobRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.service.impl.ExportServiceImpl;
import com.campus.marketplace.service.task.TaskRunner;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExportService 单元测试")
class ExportServiceTest {

    @Mock
    ExportJobRepository jobRepo;
    @Mock
    GoodsRepository goodsRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    TaskService taskService;

    @InjectMocks
    ExportServiceImpl exportService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void init() {
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(() -> SecurityUtil.isAuthenticated()).thenReturn(true);
        securityUtilMock.when(() -> SecurityUtil.getCurrentUsername()).thenReturn("admin");
        securityUtilMock.when(() -> SecurityUtil.hasRole(anyString())).thenReturn(true);
    }

    @AfterEach
    void cleanup() {
        if (securityUtilMock != null) securityUtilMock.close();
    }

    @Test
    @DisplayName("requestExport 非管理员禁止")
    void requestExport_forbidden_whenNotAdmin() {
        securityUtilMock.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);
        assertThatThrownBy(() -> exportService.requestExport("GOODS", null))
                .isInstanceOf(BusinessException.class);
        verify(jobRepo, never()).save(any());
        verify(taskService, never()).trigger(anyString(), anyString());
    }

    @Test
    @DisplayName("requestExport 成功并触发任务")
    void requestExport_success_and_trigger() {
        // save 时赋 ID
        Mockito.lenient().when(jobRepo.save(any())).thenAnswer(inv -> {
            ExportJob j = inv.getArgument(0);
            j.setId(123L);
            return j;
        });
        Long id = exportService.requestExport("GOODS", null);
        assertThat(id).isEqualTo(123L);
        verify(taskService).trigger(eq("export.run"), eq("123"));
    }

    @Test
    @DisplayName("执行 GOODS 导出并生成下载 token")
    void runExport_goods_success() throws Exception {
        // 捕获注册的任务
        Map<String, TaskRunner> map = new HashMap<>();
        doAnswer(inv -> { map.put(inv.getArgument(0), inv.getArgument(2)); return null; })
                .when(taskService).register(anyString(), anyString(), any());
        exportService.afterPropertiesSet();

        // 准备 Job 存储
        AtomicReference<ExportJob> holder = new AtomicReference<>();
        ExportJob job = ExportJob.builder().id(1L).type("GOODS").status("PENDING").requestedBy("admin").createdAt(Instant.now()).build();
        holder.set(job);
        when(jobRepo.findById(1L)).thenAnswer(inv -> Optional.of(holder.get()));
        when(jobRepo.save(any())).thenAnswer(inv -> { holder.set(inv.getArgument(0)); return holder.get(); });

        // 数据页
        Goods g = Goods.builder()
                .title("iPhone")
                .price(new java.math.BigDecimal("99.99"))
                .status(com.campus.marketplace.common.enums.GoodsStatus.APPROVED)
                .build();
        g.setId(10L);
        when(goodsRepository.findAll(PageRequest.of(0, 1000)))
                .thenReturn(new PageImpl<>(List.of(g)));
        when(goodsRepository.findAll(PageRequest.of(1, 1000)))
                .thenReturn(Page.empty());

        // 运行任务
        TaskRunner runner = map.get("export.run");
        assertThat(runner).isNotNull();
        runner.run("1");

        ExportJob saved = holder.get();
        assertThat(saved.getStatus()).isEqualTo("SUCCESS");
        assertThat(saved.getFilePath()).isNotBlank();
        assertThat(saved.getDownloadToken()).isNotBlank();
        assertThat(saved.getExpireAt()).isAfter(Instant.now());
        assertThat(new File(saved.getFilePath())).exists();
        // 简单校验文件内容
        String content = java.nio.file.Files.readString(new File(saved.getFilePath()).toPath(), StandardCharsets.UTF_8);
        assertThat(content).contains("id,title,price,status,createdAt");
        assertThat(content).contains("iPhone");
    }

    @Test
    @DisplayName("download 过期 token 返回 NOT_FOUND")
    void download_expired_token() {
        ExportJob job = ExportJob.builder()
                .id(2L)
                .downloadToken("abc")
                .expireAt(Instant.now().minusSeconds(10))
                .build();
        when(jobRepo.findByDownloadToken("abc")).thenReturn(Optional.of(job));
        assertThatThrownBy(() -> exportService.download("abc")).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("取消导出：非所有者且非管理员禁止，所有者允许")
    void cancel_rules() {
        ExportJob job = ExportJob.builder().id(3L).requestedBy("owner").status("PENDING").build();
        when(jobRepo.findById(3L)).thenReturn(Optional.of(job));
        when(jobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // 非所有者且非管理员
        securityUtilMock.when(SecurityUtil::getCurrentUsername).thenReturn("other");
        securityUtilMock.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);
        assertThatThrownBy(() -> exportService.cancel(3L)).isInstanceOf(BusinessException.class);

        // 所有者允许
        securityUtilMock.when(SecurityUtil::getCurrentUsername).thenReturn("owner");
        exportService.cancel(3L);
        verify(jobRepo, times(1)).save(any());
    }
}
