package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.component.ImageScanProvider;
import com.campus.marketplace.common.enums.ComplianceAction;
import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.service.ComplianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceServiceImpl implements ComplianceService {

    private final SensitiveWordFilter sensitiveWordFilter;
    private final ImageScanProvider imageScanProvider;

    @Value("${compliance.text.action:REVIEW}")
    private String textAction; // BLOCK/REVIEW/PASS

    @Override
    public TextResult moderateText(String text, String scene) {
        if (text == null || text.isEmpty()) {
            return new TextResult(false, ComplianceAction.PASS, text, Set.of());
        }
        boolean hit = sensitiveWordFilter.contains(text);
        if (!hit) {
            return new TextResult(false, ComplianceAction.PASS, text, Set.of());
        }
        String filtered = sensitiveWordFilter.filter(text);
        Set<String> words = sensitiveWordFilter.getSensitiveWords(text);
        ComplianceAction action = switch (textAction.toUpperCase()) {
            case "BLOCK" -> ComplianceAction.BLOCK;
            case "PASS" -> ComplianceAction.PASS;
            default -> ComplianceAction.REVIEW;
        };
        log.info("文本合规模块命中：scene={}, action={}, words={}", scene, action, words);
        return new TextResult(true, action, filtered, new HashSet<>(words));
    }

    @Override
    public ImageResult scanImages(List<String> imageUrls, String scene) {
        if (imageUrls == null || imageUrls.isEmpty()) return new ImageResult(ImageAction.PASS, null);
        ImageScanProvider.Result worst = null;
        for (String url : imageUrls) {
            ImageScanProvider.Result r = imageScanProvider.scanUrl(url);
            if (r.decision == ImageScanProvider.Decision.REJECT) { worst = r; break; }
            if (r.decision == ImageScanProvider.Decision.REVIEW) { worst = r; }
        }
        if (worst == null) return new ImageResult(ImageAction.PASS, null);
        return switch (worst.decision) {
            case REJECT -> new ImageResult(ImageAction.REJECT, worst.reason);
            case REVIEW -> new ImageResult(ImageAction.REVIEW, worst.reason);
            default -> new ImageResult(ImageAction.PASS, null);
        };
    }
}
