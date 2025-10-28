package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.component.ImageScanProvider;
import com.campus.marketplace.common.enums.ComplianceAction;
import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.service.ComplianceService;
import com.campus.marketplace.repository.ComplianceWhitelistRepository;
import com.campus.marketplace.common.utils.SecurityUtil;
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
    private final ComplianceWhitelistRepository whitelistRepository;
    private final com.campus.marketplace.repository.ComplianceAuditLogRepository auditRepository;

    @Value("${compliance.text.action:REVIEW}")
    private String textAction; // BLOCK/REVIEW/PASS

    @Override
    public TextResult moderateText(String text, String scene) {
        if (text == null || text.isEmpty()) {
            return new TextResult(false, ComplianceAction.PASS, text, Set.of());
        }
        // 白名单：用户豁免
        try {
            Long uid = SecurityUtil.getCurrentUserId();
            if (uid != null && whitelistRepository.existsByTypeAndTargetId("USER", uid)) {
                return new TextResult(false, ComplianceAction.PASS, text, Set.of());
            }
        } catch (Exception ignored) {}
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
        // 审计
        try {
            auditRepository.save(com.campus.marketplace.common.entity.ComplianceAuditLog.builder()
                    .scene(scene)
                    .action(action)
                    .targetType("TEXT")
                    .targetId(null)
                    .operatorId(SecurityUtil.getCurrentUserId())
                    .operatorName(SecurityUtil.getCurrentUsername())
                    .hitWords(String.join(",", words))
                    .snippet(text.length() > 200 ? text.substring(0, 200) : text)
                    .build());
        } catch (Exception ignored) {}
        return new TextResult(true, action, filtered, new HashSet<>(words));
    }

    @Override
    public ImageResult scanImages(List<String> imageUrls, String scene) {
        if (imageUrls == null || imageUrls.isEmpty()) return new ImageResult(ImageAction.PASS, null);
        // 白名单：用户豁免
        try {
            Long uid = SecurityUtil.getCurrentUserId();
            if (uid != null && whitelistRepository.existsByTypeAndTargetId("USER", uid)) {
                return new ImageResult(ImageAction.PASS, null);
            }
        } catch (Exception ignored) {}
        ImageScanProvider.Result worst = null;
        for (String url : imageUrls) {
            ImageScanProvider.Result r = imageScanProvider.scanUrl(url);
            if (r.decision == ImageScanProvider.Decision.REJECT) { worst = r; break; }
            if (r.decision == ImageScanProvider.Decision.REVIEW) { worst = r; }
        }
        if (worst == null) return new ImageResult(ImageAction.PASS, null);
        ImageResult result = switch (worst.decision) {
            case REJECT -> new ImageResult(ImageAction.REJECT, worst.reason);
            case REVIEW -> new ImageResult(ImageAction.REVIEW, worst.reason);
            default -> new ImageResult(ImageAction.PASS, null);
        };
        // 审计
        try {
            auditRepository.save(com.campus.marketplace.common.entity.ComplianceAuditLog.builder()
                    .scene(scene)
                    .action(result.action() == ImageAction.REJECT ? ComplianceAction.BLOCK
                            : (result.action() == ImageAction.REVIEW ? ComplianceAction.REVIEW : ComplianceAction.PASS))
                    .targetType("IMAGE")
                    .targetId(null)
                    .operatorId(SecurityUtil.getCurrentUserId())
                    .operatorName(SecurityUtil.getCurrentUsername())
                    .hitWords(null)
                    .snippet(result.reason())
                    .build());
        } catch (Exception ignored) {}
        return result;
    }
}
