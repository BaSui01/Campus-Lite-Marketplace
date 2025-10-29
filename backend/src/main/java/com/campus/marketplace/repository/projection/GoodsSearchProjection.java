package com.campus.marketplace.repository.projection;

import java.math.BigDecimal;

/**
 * Goods 全文检索投影
 *
 * @author BaSui
 * @date 2025-10-29
 */

public interface GoodsSearchProjection {
    Long getId();
    String getTitle();
    String getSnippet();
    Double getRank();
    Long getCampusId();
    BigDecimal getPrice();
}
