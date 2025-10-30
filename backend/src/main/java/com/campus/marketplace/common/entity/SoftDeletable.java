package com.campus.marketplace.common.entity;

import java.time.LocalDateTime;

/**
 * 软删除能力接口
 *
 * @author BaSui
 * @date 2025-10-29
 */

public interface SoftDeletable {
    boolean isDeleted();
    void setDeleted(boolean deleted);
    LocalDateTime getDeletedAt();
    void setDeletedAt(LocalDateTime deletedAt);

    default void markDeleted() {
        setDeleted(true);
        setDeletedAt(LocalDateTime.now());
    }
}
