package com.campus.marketplace.service;

import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.security.RoleDefinition;
import com.campus.marketplace.common.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 批量操作限制服务
 * 根据用户角色限制批量操作数量
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchLimitService {

    // 批量操作数量限制
    private static final int REGULAR_USER_LIMIT = 500;
    private static final int VIP_USER_LIMIT = 2000;
    private static final int ADMIN_LIMIT = Integer.MAX_VALUE;

    /**
     * 验证批量操作数量
     * 
     * @param count 操作数量
     * @throws BusinessException 如果超出限制
     */
    public void validateBatchLimit(int count) {
        int limit = getBatchLimit();
        
        if (count > limit) {
            String message = String.format(
                "批量操作数量超出限制，您的限制为%d个，当前请求%d个",
                limit, count
            );
            log.warn("批量操作数量超限: userId={}, limit={}, requested={}", 
                SecurityUtil.getCurrentUserId(), limit, count);
            throw new BusinessException(ErrorCode.INVALID_OPERATION, message);
        }
        
        log.debug("批量操作数量验证通过: userId={}, count={}, limit={}", 
            SecurityUtil.getCurrentUserId(), count, limit);
    }

    /**
     * 获取用户的批量操作限制
     */
    public int getBatchLimit() {
        if (SecurityUtil.hasRole(RoleDefinition.ADMIN.name())) {
            return ADMIN_LIMIT;
        } else if (SecurityUtil.hasRole("VIP")) {
            return VIP_USER_LIMIT;
        } else {
            return REGULAR_USER_LIMIT;
        }
    }

    /**
     * 获取用户批量限制信息
     */
    public String getLimitInfo() {
        int limit = getBatchLimit();
        if (limit == Integer.MAX_VALUE) {
            return "无限制";
        }
        return limit + "个";
    }
}
