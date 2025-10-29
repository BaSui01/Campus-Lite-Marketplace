package com.campus.marketplace.service;

import com.campus.marketplace.common.enums.ComplianceAction;

import java.util.List;
import java.util.Set;
/**
 * Compliance Service
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface ComplianceService {

    record TextResult(boolean hit, ComplianceAction action, String filteredText, Set<String> words) {}

    TextResult moderateText(String text, String scene);

    enum ImageAction { PASS, REVIEW, REJECT }

    record ImageResult(ImageAction action, String reason) {}

    ImageResult scanImages(List<String> imageUrls, String scene);
}
