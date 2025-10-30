package com.campus.marketplace.service;

import com.campus.marketplace.common.component.ImageScanProvider;
import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.repository.ComplianceWhitelistRepository;
import com.campus.marketplace.service.impl.ComplianceServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("内容合规服务测试：命中/误报、降级与人工审核")
class ComplianceServiceImplTest {

    @Mock
    SensitiveWordFilter filter;
    @Mock
    ImageScanProvider imageScan;
    @Mock
    ComplianceWhitelistRepository whitelistRepo;
    @Mock
    com.campus.marketplace.repository.ComplianceAuditLogRepository auditRepo;

    @InjectMocks
    ComplianceServiceImpl service;

    @Test
    @DisplayName("moderateText: 命中并根据策略返回 REVIEW/BLOCK/PASS，且过滤文本")
    void moderateText_policies() {
        // 默认策略设为 REVIEW，因 @InjectMocks 不注入 @Value
        try { var f = ComplianceServiceImpl.class.getDeclaredField("textAction"); f.setAccessible(true); f.set(service, "REVIEW"); } catch (Exception ignored) {}
        when(filter.contains("bad word")).thenReturn(true);
        when(filter.filter("bad word")).thenReturn("*** word");
        when(filter.getSensitiveWords("bad word")).thenReturn(Set.of("bad"));

        var r1 = service.moderateText("bad word", "post");
        assertThat(r1.hit()).isTrue();
        assertThat(r1.action()).isEqualTo(com.campus.marketplace.common.enums.ComplianceAction.REVIEW);
        assertThat(r1.filteredText()).contains("***");

        // 通过反射调整策略为 BLOCK
        try { var f = ComplianceServiceImpl.class.getDeclaredField("textAction"); f.setAccessible(true); f.set(service, "BLOCK"); } catch (Exception ignored) {}
        var r2 = service.moderateText("bad word", "post");
        assertThat(r2.action()).isEqualTo(com.campus.marketplace.common.enums.ComplianceAction.BLOCK);

        // 策略 PASS
        try { var f = ComplianceServiceImpl.class.getDeclaredField("textAction"); f.setAccessible(true); f.set(service, "PASS"); } catch (Exception ignored) {}
        var r3 = service.moderateText("bad word", "post");
        assertThat(r3.action()).isEqualTo(com.campus.marketplace.common.enums.ComplianceAction.PASS);
    }

    @Test
    @DisplayName("scanImages: REJECT/REVIEW/PASS 结果映射正确")
    void scanImages_decisions() {
        when(imageScan.scanUrl("reject"))
                .thenReturn(new ImageScanProvider.Result(ImageScanProvider.Decision.REJECT, "nsfw"));
        var r1 = service.scanImages(List.of("reject"), "post");
        assertThat(r1.action()).isEqualTo(com.campus.marketplace.service.ComplianceService.ImageAction.REJECT);

        when(imageScan.scanUrl("review"))
                .thenReturn(new ImageScanProvider.Result(ImageScanProvider.Decision.REVIEW, "maybe"));
        var r2 = service.scanImages(List.of("review"), "post");
        assertThat(r2.action()).isEqualTo(com.campus.marketplace.service.ComplianceService.ImageAction.REVIEW);

        when(imageScan.scanUrl("pass")).thenReturn(new ImageScanProvider.Result(ImageScanProvider.Decision.PASS, null));
        var r3 = service.scanImages(List.of("pass"), "post");
        assertThat(r3.action()).isEqualTo(com.campus.marketplace.service.ComplianceService.ImageAction.PASS);
    }
}
