package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Payment Log Repository
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {
}
