package com.campus.marketplace.service;

import java.util.Map;

/**
 * 系统指标采集服务接口
 * 
 * 功能：
 * 1. JVM 内存指标采集
 * 2. CPU 使用率统计
 * 3. 线程信息采集
 * 4. 垃圾回收统计
 * 5. 系统健康状态检测
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface MetricsService {

    /**
     * 获取 JVM 内存指标
     * 
     * @return 内存指标数据
     */
    Map<String, Object> getJvmMemoryMetrics();

    /**
     * 获取 CPU 指标
     * 
     * @return CPU 指标数据
     */
    Map<String, Object> getCpuMetrics();

    /**
     * 获取线程指标
     * 
     * @return 线程指标数据
     */
    Map<String, Object> getThreadMetrics();

    /**
     * 获取垃圾回收统计
     * 
     * @return GC 指标数据
     */
    Map<String, Object> getGarbageCollectionMetrics();

    /**
     * 获取 JVM 运行时信息
     * 
     * @return 运行时指标数据
     */
    Map<String, Object> getJvmRuntimeMetrics();

    /**
     * 获取系统信息
     * 
     * @return 系统指标数据
     */
    Map<String, Object> getSystemMetrics();

    /**
     * 获取全部系统指标
     * 
     * @return 所有指标数据
     */
    Map<String, Object> getAllMetrics();

    /**
     * 获取健康状态
     * 
     * @return 健康状态数据
     */
    Map<String, Object> getHealthStatus();
}
