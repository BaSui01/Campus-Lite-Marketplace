package com.campus.marketplace.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传服务接口 - 支持本地存储和阿里云 OSS
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
public interface FileService {

    /**
     * 上传文件 - 基础版本（只保存原图）
     *
     * @param file 上传的文件
     * @return 文件访问URL
     * @throws IOException 上传失败
     */
    String uploadFile(MultipartFile file) throws IOException;

    /**
     * 上传文件并生成缩略图 - 增强版本
     *
     * @param file 上传的图片文件
     * @return 文件访问URL
     * @throws IOException 上传失败
     */
    String uploadFileWithThumbnail(MultipartFile file) throws IOException;

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 生成唯一文件名（时间戳 + UUID）
     *
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    String generateUniqueFileName(String originalFilename);
}
