package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.CaptchaResponse;
import com.campus.marketplace.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    private static final int CAPTCHA_EXPIRE_SECONDS = 300; // 5分钟
    private static final int SLIDE_EXPIRE_SECONDS = 300; // 5分钟
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final String SLIDE_KEY_PREFIX = "slide:";

    /**
     * 生成图形验证码
     */
    @Override
    public CaptchaResponse generateImageCaptcha() {
        // 1. 生成4位随机验证码（数字+字母）
        String code = generateRandomCode(4);
        String captchaId = UUID.randomUUID().toString();

        log.info("生成图形验证码: captchaId={}, code={}", captchaId, code);

        // 2. 生成验证码图片（120x40）
        BufferedImage image = createCaptchaImage(code, 120, 40);
        String base64Image = imageToBase64(image);

        // 3. 存储到 Redis（5分钟过期）
        String key = CAPTCHA_KEY_PREFIX + captchaId;
        redisTemplate.opsForValue().set(key, code, CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);

        log.info("验证码已存储到 Redis: key={}, expiresIn={}s", key, CAPTCHA_EXPIRE_SECONDS);

        return CaptchaResponse.builder()
                .captchaId(captchaId)
                .imageBase64(base64Image)
                .expiresIn(CAPTCHA_EXPIRE_SECONDS)
                .build();
    }

    /**
     * 验证图形验证码
     */
    @Override
    public boolean verifyImageCaptcha(String captchaId, String code) {
        if (captchaId == null || code == null) {
            log.warn("验证码参数为空: captchaId={}, code={}", captchaId, code);
            return false;
        }

        String key = CAPTCHA_KEY_PREFIX + captchaId;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("验证码不存在或已过期: captchaId={}", captchaId);
            return false;
        }

        // 验证后立即删除（防止重复使用）
        redisTemplate.delete(key);

        boolean isValid = storedCode.equalsIgnoreCase(code);
        log.info("验证码验证结果: captchaId={}, isValid={}", captchaId, isValid);

        return isValid;
    }

    /**
     * 生成滑块验证码
     */
    @Override
    public CaptchaResponse generateSlideCaptcha() {
        String slideId = UUID.randomUUID().toString();
        Random random = new Random();

        // 生成目标位置（50-250px）
        int targetPosition = random.nextInt(200) + 50;

        log.info("生成滑块验证码: slideId={}, targetPosition={}", slideId, targetPosition);

        // 存储到 Redis（5分钟过期）
        String key = SLIDE_KEY_PREFIX + slideId;
        redisTemplate.opsForValue().set(key, String.valueOf(targetPosition), SLIDE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return CaptchaResponse.builder()
                .captchaId(slideId)
                .imageBase64(String.valueOf(targetPosition)) // 临时使用，前端可以根据这个位置生成拼图
                .expiresIn(SLIDE_EXPIRE_SECONDS)
                .build();
    }

    /**
     * 验证滑块验证码
     */
    @Override
    public boolean verifySlideCaptcha(String slideId, int userPosition) {
        if (slideId == null) {
            log.warn("滑块ID为空");
            return false;
        }

        String key = SLIDE_KEY_PREFIX + slideId;
        String storedPosition = redisTemplate.opsForValue().get(key);

        if (storedPosition == null) {
            log.warn("滑块验证码不存在或已过期: slideId={}", slideId);
            return false;
        }

        // 验证后立即删除（防止重复使用）
        redisTemplate.delete(key);

        int targetPosition = Integer.parseInt(storedPosition);

        // 允许±5px误差
        boolean isValid = Math.abs(targetPosition - userPosition) <= 5;
        log.info("滑块验证结果: slideId={}, targetPosition=, userPosition={}, isValid={}",
                slideId, targetPosition, userPosition, isValid);

        return isValid;
    }

    // ========== 私有方法：验证码生成工具 ==========

    /**
     * 生成随机验证码（数字+字母）
     */
    private String generateRandomCode(int length) {
        String chars = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz"; // 去掉容易混淆的字符（I、O、l）
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    /**
     * 创建验证码图片（使用 Java 原生 API）
     */
    private BufferedImage createCaptchaImage(String code, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Random random = new Random();

        // 1. 绘制背景（渐变色）
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(240, 240, 240),
                width, height, new Color(220, 220, 220)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);

        // 2. 绘制干扰线（5条）
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(
                    random.nextInt(100) + 100,
                    random.nextInt(100) + 100,
                    random.nextInt(100) + 100
            ));
            g.setStroke(new BasicStroke(1.5f));
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        // 3. 绘制干扰点（50个）
        for (int i = 0; i < 50; i++) {
            g.setColor(new Color(
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(255)
            ));
            g.fillOval(random.nextInt(width), random.nextInt(height), 2, 2);
        }

        // 4. 绘制验证码文字
        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色（深色）
            g.setColor(new Color(
                    random.nextInt(100),
                    random.nextInt(100),
                    random.nextInt(100)
            ));

            // 随机旋转角度（-15° ~ 15°）
            int angle = random.nextInt(30) - 15;
            g.rotate(Math.toRadians(angle), 20 + i * 25, 25);

            // 绘制字符
            g.drawString(String.valueOf(code.charAt(i)), 20 + i * 25, 30);

            // 恢复旋转
            g.rotate(-Math.toRadians(angle), 20 + i * 25, 25);
        }

        g.dispose();
        return image;
    }

    /**
     * 将 BufferedImage 转换为 Base64 字符串
     */
    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            log.error("验证码图片转换失败", e);
            throw new RuntimeException("验证码生成失败", e);
        }
    }

    // ========== 滑块验证码增强功能 ==========

    /**
     * 生成滑块验证码（完整版本，包含拼图图片）
     */
    @Override
    public com.campus.marketplace.common.dto.response.SlideCaptchaResponse generateSlideCaptchaWithImage() {
        String slideId = UUID.randomUUID().toString();
        Random random = new Random();

        // 1. 生成目标X轴位置（50-250px）
        int targetX = random.nextInt(200) + 50;

        // 2. 生成Y轴位置（随机，让滑块在不同高度）
        int yPosition = random.nextInt(100) + 50; // 50-150px

        log.info("生成滑块验证码（带图片）: slideId={}, targetX={}, yPosition={}", slideId, targetX, yPosition);

        // 3. 生成背景图片和滑块图片
        BufferedImage backgroundImage = createSlideBackgroundImage(300, 200, targetX, yPosition);
        BufferedImage sliderImage = createSliderImage(50, 50);

        String backgroundBase64 = imageToBase64(backgroundImage);
        String sliderBase64 = imageToBase64(sliderImage);

        // 4. 存储到 Redis（5分钟过期）
        String key = SLIDE_KEY_PREFIX + slideId;
        redisTemplate.opsForValue().set(key, String.valueOf(targetX), SLIDE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return com.campus.marketplace.common.dto.response.SlideCaptchaResponse.builder()
                .slideId(slideId)
                .backgroundImage(backgroundBase64)
                .sliderImage(sliderBase64)
                .yPosition(yPosition)
                .expiresIn(SLIDE_EXPIRE_SECONDS)
                .build();
    }

    /**
     * 验证滑块验证码（完整版本，包含轨迹分析）
     */
    @Override
    public boolean verifySlideCaptchaWithTrack(com.campus.marketplace.common.dto.request.SlideVerifyRequest request) {
        if (request.getSlideId() == null || request.getXPosition() == null) {
            log.warn("滑块验证参数为空");
            return false;
        }

        String key = SLIDE_KEY_PREFIX + request.getSlideId();
        String storedPosition = redisTemplate.opsForValue().get(key);

        if (storedPosition == null) {
            log.warn("滑块验证码不存在或已过期: slideId={}", request.getSlideId());
            return false;
        }

        // 验证后立即删除（防止重复使用）
        redisTemplate.delete(key);

        int targetPosition = Integer.parseInt(storedPosition);

        // 1. 验证X轴位置（允许±5px误差）
        boolean positionValid = Math.abs(targetPosition - request.getXPosition()) <= 5;

        // 2. 验证滑动轨迹（防作弊）
        boolean trackValid = true;
        if (request.getTrack() != null && !request.getTrack().isEmpty()) {
            trackValid = analyzeSlideTrack(request.getTrack(), targetPosition);
        }

        boolean isValid = positionValid && trackValid;
        log.info("滑块验证结果: slideId={}, targetPosition={}, userPosition={}, positionValid={}, trackValid={}, isValid={}",
                request.getSlideId(), targetPosition, request.getXPosition(), positionValid, trackValid, isValid);

        return isValid;
    }

    /**
     * 创建滑块背景图片（带缺口）
     */
    private BufferedImage createSlideBackgroundImage(int width, int height, int targetX, int yPosition) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Random random = new Random();

        // 1. 绘制渐变背景
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(200, 220, 240),
                width, height, new Color(180, 200, 220)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);

        // 2. 绘制一些装饰性图案
        for (int i = 0; i < 10; i++) {
            g.setColor(new Color(
                    random.nextInt(50) + 150,
                    random.nextInt(50) + 150,
                    random.nextInt(50) + 150,
                    100
            ));
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = random.nextInt(30) + 10;
            g.fillOval(x, y, size, size);
        }

        // 3. 绘制缺口（拼图形状）
        int puzzleSize = 50;
        g.setColor(new Color(255, 255, 255, 200)); // 半透明白色
        g.fillRect(targetX, yPosition, puzzleSize, puzzleSize);

        // 绘制缺口边框
        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(2));
        g.drawRect(targetX, yPosition, puzzleSize, puzzleSize);

        g.dispose();
        return image;
    }

    /**
     * 创建滑块图片（拼图块）
     */
    private BufferedImage createSliderImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 绘制拼图块（带渐变）
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(100, 150, 200),
                width, height, new Color(80, 130, 180)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);

        // 2. 绘制边框
        g.setColor(new Color(60, 110, 160));
        g.setStroke(new BasicStroke(2));
        g.drawRect(0, 0, width - 1, height - 1);

        // 3. 绘制一个小图标（表示这是滑块）
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("→", width / 2 - 8, height / 2 + 8);

        g.dispose();
        return image;
    }

    /**
     * 分析滑动轨迹（防作弊）
     *
     * @param track          滑动轨迹
     * @param targetPosition 目标位置
     * @return 轨迹是否合法
     */
    private boolean analyzeSlideTrack(java.util.List<com.campus.marketplace.common.dto.request.SlideVerifyRequest.TrackPoint> track, int targetPosition) {
        if (track.size() < 3) {
            log.warn("滑动轨迹点数过少: {}", track.size());
            return false; // 轨迹点太少，可能是作弊
        }

        // 1. 检查滑动时间（正常人滑动需要一定时间）
        long startTime = track.get(0).getT();
        long endTime = track.get(track.size() - 1).getT();
        long duration = endTime - startTime;

        if (duration < 100) {
            log.warn("滑动时间过短: {}ms", duration);
            return false; // 滑动时间太短，可能是机器人
        }

        if (duration > 10000) {
            log.warn("滑动时间过长: {}ms", duration);
            return false; // 滑动时间太长，可能是超时
        }

        // 2. 检查轨迹是否为直线（机器人通常是直线滑动）
        boolean isStraightLine = checkIfStraightLine(track);
        if (isStraightLine) {
            log.warn("滑动轨迹为直线，可能是机器人");
            return false;
        }

        // 3. 检查滑动速度是否合理
        double avgSpeed = (double) targetPosition / duration * 1000; // px/s
        if (avgSpeed > 1000) {
            log.warn("滑动速度过快: {} px/s", avgSpeed);
            return false; // 速度太快，可能是机器人
        }

        log.info("滑动轨迹验证通过: 点数={}, 时长={}ms, 平均速度={} px/s", track.size(), duration, avgSpeed);
        return true;
    }

    /**
     * 检查轨迹是否为直线
     */
    private boolean checkIfStraightLine(java.util.List<com.campus.marketplace.common.dto.request.SlideVerifyRequest.TrackPoint> track) {
        if (track.size() < 3) {
            return false;
        }

        // 计算Y轴方差，如果方差很小说明是直线
        double sumY = 0;
        for (com.campus.marketplace.common.dto.request.SlideVerifyRequest.TrackPoint point : track) {
            sumY += point.getY();
        }
        double avgY = sumY / track.size();

        double variance = 0;
        for (com.campus.marketplace.common.dto.request.SlideVerifyRequest.TrackPoint point : track) {
            variance += Math.pow(point.getY() - avgY, 2);
        }
        variance /= track.size();

        // 方差小于10认为是直线
        return variance < 10;
    }
}
