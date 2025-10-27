package com.campus.marketplace.repository.projection;

/**
 * Post 全文检索投影
 */
public interface PostSearchProjection {
    Long getId();
    String getTitle();
    String getSnippet();
    Double getRank();
    Long getCampusId();
}
