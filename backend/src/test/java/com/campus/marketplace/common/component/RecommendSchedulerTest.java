package com.campus.marketplace.common.component;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.repository.CampusRepository;
import com.campus.marketplace.service.RecommendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendScheduler 测试")
class RecommendSchedulerTest {

    @Mock
    private RecommendService recommendService;
    @Mock
    private CampusRepository campusRepository;

    private RecommendScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new RecommendScheduler(recommendService, campusRepository);
        ReflectionTestUtils.setField(scheduler, "defaultTopN", 20);
    }

    @Test
    @DisplayName("刷新任务会覆盖全局与活跃校区榜单")
    void refreshHotRankingJob_shouldProcessActiveCampuses() {
        Campus active = Campus.builder().code("DEFAULT").name("默认").status(CampusStatus.ACTIVE).build();
        active.setId(1L);
        Campus inactive = Campus.builder().code("CLOSED").name("关闭").status(CampusStatus.INACTIVE).build();
        inactive.setId(2L);
        when(campusRepository.findAll()).thenReturn(List.of(active, inactive));

        scheduler.refreshHotRankingJob();

        verify(recommendService).refreshHotRanking(null, 20);
        verify(recommendService).refreshHotRanking(1L, 20);
        verify(recommendService, never()).refreshHotRanking(eq(2L), anyInt());
    }

    @Test
    @DisplayName("刷新任务遇到异常时记录日志但不抛出")
    void refreshHotRankingJob_shouldSwallowExceptions() {
        when(recommendService.refreshHotRanking(null, 20)).thenThrow(new IllegalStateException("redis down"));

        scheduler.refreshHotRankingJob();

        verify(recommendService).refreshHotRanking(null, 20);
        verifyNoInteractions(campusRepository);
    }
}
