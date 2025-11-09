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
     * 上传文件 - 支持业务场景分类
     *
     * @param file 上传的文件
     * @param category 业务场景（avatar/goods/post/message/general）
     * @return 文件访问URL
     * @throws IOException 上传失败
     */
    String uploadFile(MultipartFile file, String category) throws IOException;

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

    /**
     * 上传头像并生成多尺寸缩略图
     * <p>
     * 自动生成以下尺寸的缩略图：
     * - 原图（自动压缩，质量 85%）
     * - 256x256（中等尺寸，用于个人中心）
     * - 128x128（小尺寸，用于评论列表）
     * - 64x64（超小尺寸，用于消息列表）
     * </p>
     *
     * @param file 上传的头像文件
     * @return 包含所有尺寸图片 URL 的 Map（original, large, medium, small）
     * @throws IOException 上传失败
     */
    java.util.Map<String, String> uploadAvatarWithMultipleSizes(MultipartFile file) throws IOException;

    /**
     * 上传 Base64 编码的图片
     * <p>
     * 支持场景：
     * - 图片裁剪后上传
     * - 剪贴板图片上传
     * - Canvas 绘图上传
     * </p>
     *
     * @param base64Data Base64 编码的图片数据（支持 data:image/png;base64,xxx 格式）
     * @param category 业务场景（avatar/goods/post/message/general）
     * @return 文件访问URL
     * @throws IOException 上传失败
     */
    String uploadBase64Image(String base64Data, String category) throws IOException;
}
