package com.campus.marketplace.common.aspect;

import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计日志切面
 * 
 * 拦截关键操作并记录审计日志
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    /**
     * 拦截用户封禁操作
     */
    @Around("execution(* com.campus.marketplace.service.impl.UserServiceImpl.banUser(..))")
    public Object auditUserBan(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = (Long) joinPoint.getArgs()[0];
        String reason = (String) joinPoint.getArgs()[1];
        
        try {
            Object result = joinPoint.proceed();
            logAction(AuditActionType.USER_BAN, "User", userId, 
                     "封禁用户: " + reason, "SUCCESS");
            return result;
        } catch (Exception e) {
            logAction(AuditActionType.USER_BAN, "User", userId, 
                     "封禁用户失败: " + e.getMessage(), "FAILED");
            throw e;
        }
    }

    /**
     * 拦截物品审核操作
     */
    @Around("execution(* com.campus.marketplace.service.impl.GoodsServiceImpl.approveGoods(..))")
    public Object auditGoodsApprove(ProceedingJoinPoint joinPoint) throws Throwable {
        Long goodsId = (Long) joinPoint.getArgs()[0];
        Boolean approved = (Boolean) joinPoint.getArgs()[1];
        
        try {
            Object result = joinPoint.proceed();
            logAction(AuditActionType.GOODS_APPROVE, "Goods", goodsId,
                     approved ? "审核通过" : "审核拒绝", "SUCCESS");
            return result;
        } catch (Exception e) {
            logAction(AuditActionType.GOODS_APPROVE, "Goods", goodsId,
                     "审核失败: " + e.getMessage(), "FAILED");
            throw e;
        }
    }

    /**
     * 拦截帖子审核操作
     */
    @Around("execution(* com.campus.marketplace.service.impl.PostServiceImpl.approvePost(..))")
    public Object auditPostApprove(ProceedingJoinPoint joinPoint) throws Throwable {
        Long postId = (Long) joinPoint.getArgs()[0];
        Boolean approved = (Boolean) joinPoint.getArgs()[1];
        
        try {
            Object result = joinPoint.proceed();
            logAction(AuditActionType.POST_APPROVE, "Post", postId,
                     approved ? "审核通过" : "审核拒绝", "SUCCESS");
            return result;
        } catch (Exception e) {
            logAction(AuditActionType.POST_APPROVE, "Post", postId,
                     "审核失败: " + e.getMessage(), "FAILED");
            throw e;
        }
    }

    /**
     * 拦截举报处理操作
     */
    @Around("execution(* com.campus.marketplace.service.impl.ReportServiceImpl.handleReport(..))")
    public Object auditReportHandle(ProceedingJoinPoint joinPoint) throws Throwable {
        Long reportId = (Long) joinPoint.getArgs()[0];
        Boolean approved = (Boolean) joinPoint.getArgs()[1];
        
        try {
            Object result = joinPoint.proceed();
            logAction(AuditActionType.REPORT_HANDLE, "Report", reportId,
                     approved ? "举报通过" : "举报驳回", "SUCCESS");
            return result;
        } catch (Exception e) {
            logAction(AuditActionType.REPORT_HANDLE, "Report", reportId,
                     "处理失败: " + e.getMessage(), "FAILED");
            throw e;
        }
    }

    /**
     * 记录审计日志（异步）
     */
    private void logAction(AuditActionType actionType, String targetType, Long targetId,
                          String details, String result) {
        try {
            String username = SecurityUtil.getCurrentUsername();
            Long userId = null;
            
            HttpServletRequest request = null;
            String ipAddress = null;
            String userAgent = null;
            
            try {
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    request = attributes.getRequest();
                    ipAddress = getClientIp(request);
                    userAgent = request.getHeader("User-Agent");
                }
            } catch (Exception e) {
                log.warn("获取请求信息失败: {}", e.getMessage());
            }

            auditLogService.logActionAsync(userId, username, actionType, targetType, targetId,
                                          details, result, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("审计日志异步记录失败: {}", e.getMessage());
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
