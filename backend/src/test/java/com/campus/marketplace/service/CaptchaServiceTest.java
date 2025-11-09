package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.CaptchaResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证码服务测试类
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("验证码服务测试")
class CaptchaServiceTest {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("生成图形验证码 - 应该返回验证码ID和Base64图片")
    void generateImageCaptcha_shouldReturnCaptchaIdAndImage() {
        // When: 生成图形验证码
        CaptchaResponse response = captchaService.generateImageCaptcha();

        // Then: 验证响应
        assertThat(response).isNotNull();
        assertThat(response.getCaptchaId()).isNotNull().isNotEmpty();
        assertThat(response.getImageBase64()).isNotNull().startsWith("data:image/png;base64,");
        assertThat(response.getExpiresIn()).isEqualTo(300); // 5分钟

        // Then: 验证 Redis 中存储了验证码
        String key = "captcha:" + response.getCaptchaId();
        String storedCode = redisTemplate.opsForValue().get(key);
        assertThat(storedCode).isNotNull().hasSize(4); // 4位验证码

        // 清理
        redisTemplate.delete(key);
    }

    @Test
    @DisplayName("验证图形验证码 - 正确的验证码应该验证通过")
    void verifyImageCaptcha_withCorrectCode_shouldReturnTrue() {
        // Given: 生成验证码
        CaptchaResponse response = captchaService.generateImageCaptcha();
        String captchaId = response.getCaptchaId();

        // 从 Redis 获取正确的验证码
        String key = "captcha:" + captchaId;
        String correctCode = redisTemplate.opsForValue().get(key);

        // When: 验证正确的验证码
        boolean isValid = captchaService.verifyImageCaptcha(captchaId, correctCode);

        // Then: 应该验证通过
        assertThat(isValid).isTrue();

        // Then: 验证后验证码应该被删除
        String storedCode = redisTemplate.opsForValue().get(key);
        assertThat(storedCode).isNull();
    }

    @Test
    @DisplayName("验证图形验证码 - 错误的验证码应该验证失败")
    void verifyImageCaptcha_withWrongCode_shouldReturnFalse() {
        // Given: 生成验证码
        CaptchaResponse response = captchaService.generateImageCaptcha();
        String captchaId = response.getCaptchaId();

        // When: 验证错误的验证码
        boolean isValid = captchaService.verifyImageCaptcha(captchaId, "WRONG");

        // Then: 应该验证失败
        assertThat(isValid).isFalse();

        // 清理
        redisTemplate.delete("captcha:" + captchaId);
    }

    @Test
    @DisplayName("验证图形验证码 - 不存在的验证码ID应该验证失败")
    void verifyImageCaptcha_withNonExistentId_shouldReturnFalse() {
        // When: 验证不存在的验证码ID
        boolean isValid = captchaService.verifyImageCaptcha("non-existent-id", "1234");

        // Then: 应该验证失败
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("验证图形验证码 - 验证码不区分大小写")
    void verifyImageCaptcha_shouldBeCaseInsensitive() {
        // Given: 生成验证码
        CaptchaResponse response = captchaService.generateImageCaptcha();
        String captchaId = response.getCaptchaId();

        // 从 Redis 获取正确的验证码
        String key = "captcha:" + captchaId;
        String correctCode = redisTemplate.opsForValue().get(key);

        // When: 使用不同大小写验证
        boolean isValid = captchaService.verifyImageCaptcha(captchaId, correctCode.toLowerCase());

        // Then: 应该验证通过（不区分大小写）
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("生成滑块验证码 - 应该返回滑块ID和目标位置")
    void generateSlideCaptcha_shouldReturnSlideIdAndPosition() {
        // When: 生成滑块验证码
        CaptchaResponse response = captchaService.generateSlideCaptcha();

        // Then: 验证响应
        assertThat(response).isNotNull();
        assertThat(response.getCaptchaId()).isNotNull().isNotEmpty();
        assertThat(response.getImageBase64()).isNotNull(); // 临时存储目标位置
        assertThat(response.getExpiresIn()).isEqualTo(300); // 5分钟

        // Then: 验证 Redis 中存储了目标位置
        String key = "slide:" + response.getCaptchaId();
        String storedPosition = redisTemplate.opsForValue().get(key);
        assertThat(storedPosition).isNotNull();

        int position = Integer.parseInt(storedPosition);
        assertThat(position).isBetween(50, 250); // 50-250px

        // 清理
        redisTemplate.delete(key);
    }

    @Test
    @DisplayName("验证滑块验证码 - 正确的位置应该验证通过")
    void verifySlideCaptcha_withCorrectPosition_shouldReturnTrue() {
        // Given: 生成滑块验证码
        CaptchaResponse response = captchaService.generateSlideCaptcha();
        String slideId = response.getCaptchaId();

        // 从 Redis 获取目标位置
        String key = "slide:" + slideId;
        int targetPosition = Integer.parseInt(redisTemplate.opsForValue().get(key));

        // When: 验证正确的位置（允许±5px误差）
        boolean isValid = captchaService.verifySlideCaptcha(slideId, targetPosition);

        // Then: 应该验证通过
        assertThat(isValid).isTrue();

        // Then: 验证后滑块验证码应该被删除
        String storedPosition = redisTemplate.opsForValue().get(key);
        assertThat(storedPosition).isNull();
    }

    @Test
    @DisplayName("验证滑块验证码 - 允许±5px误差")
    void verifySlideCaptcha_shouldAllowFivePixelTolerance() {
        // Given: 生成滑块验证码
        CaptchaResponse response1 = captchaService.generateSlideCaptcha();
        CaptchaResponse response2 = captchaService.generateSlideCaptcha();

        String slideId1 = response1.getCaptchaId();
        String slideId2 = response2.getCaptchaId();

        int targetPosition1 = Integer.parseInt(redisTemplate.opsForValue().get("slide:" + slideId1));
        int targetPosition2 = Integer.parseInt(redisTemplate.opsForValue().get("slide:" + slideId2));

        // When: 验证±5px误差内的位置
        boolean isValid1 = captchaService.verifySlideCaptcha(slideId1, targetPosition1 + 5);
        boolean isValid2 = captchaService.verifySlideCaptcha(slideId2, targetPosition2 - 5);

        // Then: 应该验证通过
        assertThat(isValid1).isTrue();
        assertThat(isValid2).isTrue();
    }

    @Test
    @DisplayName("验证滑块验证码 - 超过±5px误差应该验证失败")
    void verifySlideCaptcha_withLargeError_shouldReturnFalse() {
        // Given: 生成滑块验证码
        CaptchaResponse response = captchaService.generateSlideCaptcha();
        String slideId = response.getCaptchaId();

        int targetPosition = Integer.parseInt(redisTemplate.opsForValue().get("slide:" + slideId));

        // When: 验证超过±5px误差的位置
        boolean isValid = captchaService.verifySlideCaptcha(slideId, targetPosition + 10);

        // Then: 应该验证失败
        assertThat(isValid).isFalse();
    }
}
