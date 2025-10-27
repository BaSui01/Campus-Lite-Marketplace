package com.campus.marketplace.common.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoopImageScanProvider implements ImageScanProvider {

    @Value("${compliance.image.enabled:false}")
    private boolean enabled;

    @Override
    public Result scanUrl(String imageUrl) {
        if (!enabled) {
            log.debug("图片扫描已禁用，返回 PASS: {}", imageUrl);
            return Result.pass();
        }
        // 预留第三方接入，默认放行
        return Result.pass();
    }
}
