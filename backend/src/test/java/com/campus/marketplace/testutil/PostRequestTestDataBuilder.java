package com.campus.marketplace.testutil;

import com.campus.marketplace.common.dto.request.CreatePostRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 🧪 BaSui 的帖子测试数据构建器 - 让测试数据更语义化！😎
 *
 * <p>提供流式 API 构建 {@link CreatePostRequest} 测试数据</p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 创建正常帖子
 * CreatePostRequest request = PostRequestTestDataBuilder.aValidPost().build();
 *
 * // 创建无图片帖子
 * CreatePostRequest request = PostRequestTestDataBuilder.aPostWithoutImages().build();
 *
 * // 自定义帖子
 * CreatePostRequest request = PostRequestTestDataBuilder.aValidPost()
 *     .withTitle("自定义标题")
 *     .withImages(3)
 *     .build();
 * }</pre>
 *
 * @author BaSui
 * @date 2025-11-08
 */
public class PostRequestTestDataBuilder {

    // 测试常量（语义化！让人一看就懂！）
    private static final String DEFAULT_TITLE = "二手自行车转让";
    private static final String DEFAULT_CONTENT = "9成新山地自行车，骑行体验良好，价格实惠，仅限校内交易！详细信息请私聊咨询。";
    private static final String DEFAULT_IMAGE_PREFIX = "https://campus-marketplace.test/posts/bike-";
    private static final String DEFAULT_IMAGE_SUFFIX = ".jpg";

    // Builder 字段
    private String title;
    private String content;
    private List<String> images;
    private List<Long> tagIds;

    /**
     * 私有构造器（Builder 模式套路！）
     */
    private PostRequestTestDataBuilder() {
        // 默认值：正常的测试数据
        this.title = DEFAULT_TITLE;
        this.content = DEFAULT_CONTENT;
        this.images = generateImages(2); // 默认2张图片
        this.tagIds = null; // 默认无标签
    }

    /**
     * 🎯 创建一个正常的帖子请求（有图片、有内容）
     */
    public static PostRequestTestDataBuilder aValidPost() {
        return new PostRequestTestDataBuilder();
    }

    /**
     * 🚫 创建一个无图片的帖子请求
     */
    public static PostRequestTestDataBuilder aPostWithoutImages() {
        return new PostRequestTestDataBuilder()
                .withImages(null);
    }

    /**
     * 💣 创建一个包含敏感词的帖子请求（用于测试过滤功能）
     */
    public static PostRequestTestDataBuilder aPostWithSensitiveWord() {
        return new PostRequestTestDataBuilder()
                .withTitle("包含傻逼的标题");
    }

    /**
     * 📝 自定义标题
     */
    public PostRequestTestDataBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * 📄 自定义内容
     */
    public PostRequestTestDataBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * 🖼️ 自定义图片列表
     */
    public PostRequestTestDataBuilder withImages(List<String> images) {
        this.images = images;
        return this;
    }

    /**
     * 🖼️ 自动生成指定数量的图片 URL
     *
     * @param count 图片数量（使用 Integer.valueOf(0) 表示空列表）
     */
    public PostRequestTestDataBuilder withImagesCount(int count) {
        this.images = count == 0 ? List.of() : generateImages(count);
        return this;
    }

    /**
     * 🏷️ 自定义标签ID列表
     */
    public PostRequestTestDataBuilder withTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
        return this;
    }

    /**
     * 🏷️ 自动生成指定数量的标签ID
     */
    public PostRequestTestDataBuilder withTagIds(Integer count) {
        if (count == null || count == 0) {
            this.tagIds = null;
            return this;
        }
        List<Long> tags = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            tags.add(i);
        }
        this.tagIds = tags;
        return this;
    }

    /**
     * ✅ 构建最终的 CreatePostRequest 对象
     */
    public CreatePostRequest build() {
        return new CreatePostRequest(title, content, images, tagIds);
    }

    /**
     * 🛠️ 生成指定数量的图片 URL（工具方法）
     */
    private static List<String> generateImages(int count) {
        List<String> urls = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            urls.add(DEFAULT_IMAGE_PREFIX + i + DEFAULT_IMAGE_SUFFIX);
        }
        return urls;
    }
}
