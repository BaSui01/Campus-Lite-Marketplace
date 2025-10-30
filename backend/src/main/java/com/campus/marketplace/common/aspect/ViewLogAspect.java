package com.campus.marketplace.common.aspect;

import com.campus.marketplace.common.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import com.campus.marketplace.service.ViewLogService;

import java.time.LocalDateTime;

/**
 * 浏览日志切面
 * 
 * 使用 AOP 记录用户浏览物品的日志
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ViewLogAspect {

    private final ViewLogService viewLogService;

    /**
     * 定义切点：GoodsService.getGoodsDetail 方法
     */
    @Pointcut("execution(* com.campus.marketplace.service.GoodsService.getGoodsDetail(..))")
    public void viewGoodsPointcut() {
    }

    /**
     * 后置通知：记录浏览日志
     */
    @AfterReturning(pointcut = "viewGoodsPointcut()", returning = "result")
    public void logViewGoods(JoinPoint joinPoint, Object result) {
        try {
            // 获取物品 ID（方法参数）
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof Long goodsId) {
                
                // 获取当前用户（如果已登录）
                String username = "匿名用户";
                try {
                    if (SecurityUtil.isAuthenticated()) {
                        username = SecurityUtil.getCurrentUsername();
                    }
                } catch (Exception e) {
                    // 未登录用户，使用匿名
                }

                // 记录浏览日志
                log.info("【浏览日志】用户: {}, 物品ID: {}, 时间: {}", 
                        username, goodsId, LocalDateTime.now());

                // 异步写入数据库
                viewLogService.saveAsync(username, goodsId, System.currentTimeMillis());
            }
        } catch (Exception e) {
            log.error("记录浏览日志失败", e);
        }
    }
}
