package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.PrivacyRequest;
import com.campus.marketplace.common.enums.PrivacyRequestStatus;
import com.campus.marketplace.common.enums.PrivacyRequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 隐私请求仓库
 *
 * 管理导出/删除请求的持久化查询
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface PrivacyRequestRepository extends JpaRepository<PrivacyRequest, Long> {

    Optional<PrivacyRequest> findFirstByUserIdAndTypeAndStatusIn(Long userId,
                                                                PrivacyRequestType type,
                                                                List<PrivacyRequestStatus> statuses);

    List<PrivacyRequest> findByStatus(PrivacyRequestStatus status);

    List<PrivacyRequest> findByUserId(Long userId);
}
