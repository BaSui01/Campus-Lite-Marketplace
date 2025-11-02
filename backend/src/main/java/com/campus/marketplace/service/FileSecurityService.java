package com.campus.marketplace.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件安全检查服务接口
 * 
 * 提供文件上传前的安全验证功能，包括：
 * - 文件类型验证
 * - 文件大小验证
 * - 文件名安全检查
 * - 病毒扫描
 * - 文件哈希计算
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
public interface FileSecurityService {

    /**
     * 验证文件类型是否合法
     * 
     * 检查文件的MIME类型是否在允许的列表中
     * 
     * @param file 待验证的文件
     * @throws IllegalArgumentException 如果文件类型不合法
     */
    void validateFileType(MultipartFile file);

    /**
     * 验证文件大小是否在限制范围内
     * 
     * @param file 待验证的文件
     * @param maxSize 最大允许大小（字节）
     * @throws IllegalArgumentException 如果文件超过大小限制
     */
    void validateFileSize(MultipartFile file, long maxSize);

    /**
     * 验证文件名安全性
     * 
     * 检查文件名中是否包含路径遍历字符（../)、特殊字符等
     * 
     * @param file 待验证的文件
     * @throws IllegalArgumentException 如果文件名包含非法字符
     */
    void validateFileName(MultipartFile file);

    /**
     * 验证文件不为空
     * 
     * @param file 待验证的文件
     * @throws IllegalArgumentException 如果文件为空
     */
    void validateNotEmpty(MultipartFile file);

    /**
     * 验证文件扩展名与MIME类型是否匹配
     * 
     * 防止文件伪装（例如将exe文件改名为jpg）
     * 
     * @param file 待验证的文件
     * @throws IllegalArgumentException 如果扩展名与MIME类型不匹配
     */
    void validateExtensionMatchesMimeType(MultipartFile file);

    /**
     * 执行完整的文件安全检查
     * 
     * 综合执行所有安全验证，包括：
     * - 文件非空检查
     * - 文件名安全检查
     * - 文件类型验证
     * - 扩展名与MIME类型匹配检查
     * 
     * @param file 待验证的文件
     * @throws IllegalArgumentException 如果任何安全检查失败
     */
    void performSecurityCheck(MultipartFile file);

    /**
     * 计算文件的SHA-256哈希值
     * 
     * 用于：
     * - 检测重复文件
     * - 文件完整性验证
     * 
     * @param file 待计算的文件
     * @return 文件的SHA-256哈希值（64位十六进制字符串）
     */
    String calculateFileHash(MultipartFile file);

    /**
     * 对文件进行病毒扫描
     * 
     * 注意：当前实现为模拟扫描，生产环境应集成专业病毒扫描服务
     * 
     * @param file 待扫描的文件
     * @return 扫描结果：CLEAN（清洁）、INFECTED（感染）、ERROR（扫描失败）
     */
    String scanForVirus(MultipartFile file);
}
