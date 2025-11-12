package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.CaptchaResponse;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
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

    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * 构造函数注入（推荐方式，避免循环依赖）
     */
    @Autowired
    public CaptchaServiceImpl(@Qualifier("customStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ========== 过期时间配置 ==========
    private static final int CAPTCHA_EXPIRE_SECONDS = 300; // 5分钟
    private static final int SLIDE_EXPIRE_SECONDS = 300; // 5分钟
    private static final int ROTATE_EXPIRE_SECONDS = 300; // 5分钟
    private static final int CLICK_EXPIRE_SECONDS = 300; // 5分钟
    
    // ========== Redis Key前缀 ==========
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final String SLIDE_KEY_PREFIX = "slide:";
    private static final String ROTATE_KEY_PREFIX = "rotate:";
    private static final String CLICK_KEY_PREFIX = "click:";
    private static final String CAPTCHA_TOKEN_PREFIX = "captcha:token:";
    
    // ========== 验证容差配置（优化用户体验）==========
    private static final int SLIDE_TOLERANCE_SIMPLE = 10;      // 滑块验证码简单版容差：±10px
    private static final int SLIDE_TOLERANCE_WITH_TRACK = 15;  // 滑块验证码轨迹版容差：±15px
    private static final int ROTATE_TOLERANCE = 15;            // 旋转验证码容差：±15度
    private static final int CLICK_TOLERANCE = 30;             // 点选验证码容差：±30px
    
    // ========== 验证码通行证配置 ==========
    private static final int CAPTCHA_TOKEN_EXPIRE_SECONDS = 60; // 验证码通行证有效期：60秒

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
     * 验证图形验证码（内部方法）
     */
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
        
        // 增强日志：显示实际对比的值
        if (!isValid) {
            log.warn("❌ 验证码不匹配: captchaId={}, expected={}, actual={}", 
                    captchaId, storedCode, code);
        } else {
            log.info("✅ 验证码验证成功: captchaId={}", captchaId);
        }

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
     * 验证滑块验证码（内部方法）
     */
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

        // 验证位置（使用配置的容差范围）
        int diff = Math.abs(targetPosition - userPosition);
        boolean isValid = diff <= SLIDE_TOLERANCE_SIMPLE;
        
        log.info("滑块验证结果: slideId={}, targetPosition={}, userPosition={}, diff={}, tolerance={}, isValid={}",
                slideId, targetPosition, userPosition, diff, SLIDE_TOLERANCE_SIMPLE, isValid);

        return isValid;
    }

    // ========== 私有方法：验证码生成工具 ==========

    /**
     * 生成随机验证码（数字+字母）
     * 改进点：
     * 1. 去掉所有小写字母，只用大写字母+数字（更清晰）
     * 2. 去掉易混淆字符：0/O、1/I/l
     */
    private String generateRandomCode(int length) {
        String chars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"; // 只用大写字母+数字，去掉0、1、I、O
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

        // 2. 绘制干扰线（减少到3条，降低干扰）
        for (int i = 0; i < 3; i++) {
            g.setColor(new Color(
                    random.nextInt(50) + 150,  // 更浅的颜色
                    random.nextInt(50) + 150,
                    random.nextInt(50) + 150
            ));
            g.setStroke(new BasicStroke(1.0f));  // 更细的线条
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        // 3. 绘制干扰点（减少到30个，降低干扰）
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(
                    random.nextInt(100) + 155,  // 更浅的颜色
                    random.nextInt(100) + 155,
                    random.nextInt(100) + 155
            ));
            g.fillOval(random.nextInt(width), random.nextInt(height), 2, 2);
        }

        // 4. 绘制验证码文字（改进：更清晰的显示）
        g.setFont(new Font("Arial", Font.BOLD, 32));  // 更大的字体
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色（深色，但更统一）
            g.setColor(new Color(
                    random.nextInt(50),   // 0-49，更深的颜色
                    random.nextInt(50),
                    random.nextInt(50)
            ));

            // 随机旋转角度（减小旋转幅度：-10° ~ 10°）
            int angle = random.nextInt(20) - 10;
            g.rotate(Math.toRadians(angle), 20 + i * 25, 28);

            // 绘制字符
            g.drawString(String.valueOf(code.charAt(i)), 20 + i * 25, 28);

            // 恢复旋转
            g.rotate(-Math.toRadians(angle), 20 + i * 25, 28);
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
     * 生成滑块验证码（完整版本，包含拼图图片） - 真实裁剪版本！✨
     */
    @Override
    public com.campus.marketplace.common.dto.response.SlideCaptchaResponse generateSlideCaptchaWithImage() {
        String slideId = UUID.randomUUID().toString();
        Random random = new Random();

        // 1. 生成目标X轴位置（50-250px）
        int targetX = random.nextInt(200) + 50;

        // 2. 生成Y轴位置（随机，让滑块在不同高度）
        int yposition = random.nextInt(100) + 50; // 50-150px

        log.info("生成滑块验证码（带图片）: slideId={}, targetX={}, yposition={}", slideId, targetX, yposition);

        // 3. 先生成完整的原始图片（不带缺口）
        BufferedImage originalImage = createOriginalImage(300, 200);

        // 4. 从原始图片中裁剪出滑块（真实裁剪！）
        BufferedImage sliderImage = cutPuzzlePiece(originalImage, targetX, yposition, 50);

        // 5. 在原始图片上绘制缺口，生成背景图
        BufferedImage backgroundImage = drawPuzzleHole(originalImage, targetX, yposition, 50);

        String backgroundBase64 = imageToBase64(backgroundImage);
        String sliderBase64 = imageToBase64(sliderImage);

        // 6. 存储到 Redis（5分钟过期）
        String key = SLIDE_KEY_PREFIX + slideId;
        redisTemplate.opsForValue().set(key, String.valueOf(targetX), SLIDE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return com.campus.marketplace.common.dto.response.SlideCaptchaResponse.builder()
                .slideId(slideId)
                .backgroundImage(backgroundBase64)
                .sliderImage(sliderBase64)
                .yposition(yposition)
                .expiresIn(SLIDE_EXPIRE_SECONDS)
                .build();
    }

    /**
     * 验证滑块验证码（完整版本，包含轨迹分析）内部方法
     */
    private boolean verifySlideCaptchaWithTrack(com.campus.marketplace.common.dto.request.SlideVerifyRequest request) {
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

        // 1. 验证X轴位置（使用配置的容差范围）
        int positionDiff = Math.abs(targetPosition - request.getXPosition());
        boolean positionValid = positionDiff <= SLIDE_TOLERANCE_WITH_TRACK;

        // 2. 验证滑动轨迹（防作弊）
        boolean trackValid = true;
        if (request.getTrack() != null && !request.getTrack().isEmpty()) {
            trackValid = analyzeSlideTrack(request.getTrack(), targetPosition);
        }

        boolean isValid = positionValid && trackValid;
        log.info("滑块验证结果（轨迹版）: slideId={}, targetPosition={}, userPosition={}, positionDiff={}, tolerance={}, positionValid={}, trackValid={}, isValid={}",
                request.getSlideId(), targetPosition, request.getXPosition(), positionDiff, SLIDE_TOLERANCE_WITH_TRACK, positionValid, trackValid, isValid);

        return isValid;
    }

    /**
     * 创建滑块背景图片（带缺口） - 真实拼图形状！🧩
     * 
     * 注：此方法保留以备将来滑块验证码功能扩展使用
     */
    @SuppressWarnings("unused")
    private BufferedImage createSlideBackgroundImage(int width, int height, int targetX, int yposition) {
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

        // 3. 绘制拼图缺口（真实凹凸形状！）
        int puzzleSize = 50;
        java.awt.geom.Path2D.Double puzzlePath = createPuzzleShape(targetX, yposition, puzzleSize);

        // 填充缺口（半透明白色）
        g.setColor(new Color(255, 255, 255, 200));
        g.fill(puzzlePath);

        // 绘制缺口边框（深色）
        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(2));
        g.draw(puzzlePath);

        g.dispose();
        return image;
    }

    /**
     * 创建原始图片（用于裁剪拼图）
     */
    private BufferedImage createOriginalImage(int width, int height) {
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

        g.dispose();
        return image;
    }

    /**
     * 从原始图片裁剪拼图块（真实裁剪！）
     */
    private BufferedImage cutPuzzlePiece(BufferedImage originalImage, int x, int y, int size) {
        BufferedImage puzzlePiece = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = puzzlePiece.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 创建拼图形状作为裁剪区域
        java.awt.geom.Path2D.Double puzzlePath = createPuzzleShape(0, 0, size);
        g.setClip(puzzlePath);

        // 2. 从原始图片裁剪出对应区域
        g.drawImage(originalImage, -x, -y, null);

        // 3. 绘制边框（让拼图更清晰）
        g.setClip(null);
        g.setColor(new Color(60, 110, 160));
        g.setStroke(new BasicStroke(2));
        g.draw(puzzlePath);

        g.dispose();
        return puzzlePiece;
    }

    /**
     * 在背景图上绘制缺口
     */
    private BufferedImage drawPuzzleHole(BufferedImage originalImage, int x, int y, int size) {
        // 复制原始图片
        BufferedImage backgroundImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g = backgroundImage.createGraphics();

        // 绘制原始图片
        g.drawImage(originalImage, 0, 0, null);

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 创建拼图形状
        java.awt.geom.Path2D.Double puzzlePath = createPuzzleShape(x, y, size);

        // 填充缺口（半透明白色，显示为缺口效果）
        g.setColor(new Color(255, 255, 255, 200));
        g.fill(puzzlePath);

        // 绘制缺口边框（深色）
        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(2));
        g.draw(puzzlePath);

        g.dispose();
        return backgroundImage;
    }

    /**
     * 创建拼图形状（真实凹凸拼图！）🧩
     * 
     * @param x 起始X坐标
     * @param y 起始Y坐标
     * @param size 拼图大小
     * @return 拼图路径
     */
    private java.awt.geom.Path2D.Double createPuzzleShape(int x, int y, int size) {
        java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
        
        int bulgeDiameter = size / 5; // 凸起/凹陷的直径（拼图特征）
        int bulgeRadius = bulgeDiameter / 2;
        
        // 从左上角开始绘制拼图形状
        path.moveTo(x, y);
        
        // 顶边 - 中间有凸起
        path.lineTo(x + size / 2 - bulgeRadius, y);
        path.quadTo(x + size / 2, y - bulgeRadius, x + size / 2 + bulgeRadius, y); // 凸起的贝塞尔曲线
        path.lineTo(x + size, y);
        
        // 右边 - 中间有凸起
        path.lineTo(x + size, y + size / 2 - bulgeRadius);
        path.quadTo(x + size + bulgeRadius, y + size / 2, x + size, y + size / 2 + bulgeRadius); // 凸起
        path.lineTo(x + size, y + size);
        
        // 底边 - 中间有凹陷
        path.lineTo(x + size / 2 + bulgeRadius, y + size);
        path.quadTo(x + size / 2, y + size + bulgeRadius, x + size / 2 - bulgeRadius, y + size); // 凹陷
        path.lineTo(x, y + size);
        
        // 左边 - 中间有凹陷
        path.lineTo(x, y + size / 2 + bulgeRadius);
        path.quadTo(x - bulgeRadius, y + size / 2, x, y + size / 2 - bulgeRadius); // 凹陷
        path.lineTo(x, y);
        
        path.closePath();
        return path;
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

    // ========== 旋转验证码功能 ==========

    /**
     * 生成旋转验证码 🔄
     */
    @Override
    public com.campus.marketplace.common.dto.response.RotateCaptchaResponse generateRotateCaptcha() {
        String rotateId = UUID.randomUUID().toString();
        Random random = new Random();

        // 1. 生成随机旋转角度（0-359度）
        int targetAngle = random.nextInt(360);

        log.info("生成旋转验证码: rotateId={}, targetAngle={}", rotateId, targetAngle);

        // 2. 生成原始图片（带明显方向特征）
        BufferedImage originalImage = createRotatableImage(200, 200);

        // 3. 生成旋转后的图片
        BufferedImage rotatedImage = rotateImage(originalImage, targetAngle);

        String originalBase64 = imageToBase64(originalImage);
        String rotatedBase64 = imageToBase64(rotatedImage);

        // 4. 存储到 Redis（5分钟过期）
        String key = ROTATE_KEY_PREFIX + rotateId;
        redisTemplate.opsForValue().set(key, String.valueOf(targetAngle), ROTATE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return com.campus.marketplace.common.dto.response.RotateCaptchaResponse.builder()
                .rotateId(rotateId)
                .originalImage(originalBase64)
                .rotatedImage(rotatedBase64)
                .expiresIn(ROTATE_EXPIRE_SECONDS)
                .build();
    }

    /**
     * 验证旋转验证码（内部方法）
     */
    private boolean verifyRotateCaptcha(com.campus.marketplace.common.dto.request.RotateVerifyRequest request) {
        if (request.getRotateId() == null || request.getAngle() == null) {
            log.warn("旋转验证参数为空");
            return false;
        }

        String key = ROTATE_KEY_PREFIX + request.getRotateId();
        String storedAngle = redisTemplate.opsForValue().get(key);

        if (storedAngle == null) {
            log.warn("旋转验证码不存在或已过期: rotateId={}", request.getRotateId());
            return false;
        }

        // 验证后立即删除（防止重复使用）
        redisTemplate.delete(key);

        int targetAngle = Integer.parseInt(storedAngle);

        // 计算角度差（处理跨越0度的情况）
        int angleDiff = Math.abs(targetAngle - request.getAngle());
        // 例如：350度和10度实际只差20度
        if (angleDiff > 180) {
            angleDiff = 360 - angleDiff;
        }

        // 验证角度（使用配置的容差范围）
        boolean isValid = angleDiff <= ROTATE_TOLERANCE;
        log.info("旋转验证结果: rotateId={}, targetAngle={}, userAngle={}, angleDiff={}, tolerance={}, isValid={}",
                request.getRotateId(), targetAngle, request.getAngle(), angleDiff, ROTATE_TOLERANCE, isValid);

        return isValid;
    }

    /**
     * 创建可旋转的图片（带箭头或其他方向性标志）
     */
    private BufferedImage createRotatableImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 绘制渐变背景
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(230, 240, 250),
                width, height, new Color(200, 220, 240)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);

        // 2. 绘制一个大箭头（明显的方向特征）
        int centerX = width / 2;
        int centerY = height / 2;

        // 箭头主体（矩形）
        g.setColor(new Color(100, 150, 200));
        int arrowWidth = 40;
        int arrowLength = 80;
        g.fillRect(centerX - arrowWidth / 2, centerY - arrowLength / 2, arrowWidth, arrowLength);

        // 箭头头部（三角形）
        int[] xPoints = {centerX - 30, centerX, centerX + 30};
        int[] yPoints = {centerY - arrowLength / 2, centerY - arrowLength / 2 - 40, centerY - arrowLength / 2};
        g.fillPolygon(xPoints, yPoints, 3);

        // 3. 绘制一些装饰性元素（增加识别度）
        g.setColor(new Color(80, 130, 180));
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("↑", centerX - 10, centerY + arrowLength / 2 + 40);

        // 4. 绘制边框
        g.setColor(new Color(150, 180, 210));
        g.setStroke(new BasicStroke(3));
        g.drawRect(0, 0, width - 1, height - 1);

        g.dispose();
        return image;
    }

    /**
     * 旋转图片
     */
    private BufferedImage rotateImage(BufferedImage src, double angle) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage rotated = new BufferedImage(width, height, src.getType());
        Graphics2D g = rotated.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 以图片中心为旋转点
        g.rotate(Math.toRadians(angle), width / 2.0, height / 2.0);

        // 绘制原始图片
        g.drawImage(src, 0, 0, null);
        g.dispose();

        return rotated;
    }

    // ========== 点选验证码功能 ==========

    /**
     * 生成点选验证码 👆
     */
    @Override
    public com.campus.marketplace.common.dto.response.ClickCaptchaResponse generateClickCaptcha() {
        String clickId = UUID.randomUUID().toString();
        Random random = new Random();

        // 1. 随机选择需要点击的文字（2-4个字）
        String[] wordPool = {"春", "夏", "秋", "冬", "梅", "兰", "竹", "菊", "红", "绿", "蓝", "黄"};
        int wordCount = random.nextInt(3) + 2; // 2-4个字
        java.util.List<String> targetWords = new java.util.ArrayList<>();
        java.util.List<java.awt.Point> targetPositions = new java.util.ArrayList<>();

        for (int i = 0; i < wordCount; i++) {
            targetWords.add(wordPool[random.nextInt(wordPool.length)]);
        }

        log.info("生成点选验证码: clickId={}, targetWords={}", clickId, targetWords);

        // 2. 生成背景图片并绘制文字
        BufferedImage backgroundImage = createClickableImage(300, 200, targetWords, targetPositions);
        String backgroundBase64 = imageToBase64(backgroundImage);

        // 3. 生成提示文字
        String hint = "请依次点击【" + String.join("】【", targetWords) + "】";

        // 4. 存储到 Redis（5分钟过期）
        String key = CLICK_KEY_PREFIX + clickId;
        // 存储格式：x1,y1;x2,y2;x3,y3
        String positionsStr = targetPositions.stream()
                .map(p -> p.x + "," + p.y)
                .reduce((a, b) -> a + ";" + b)
                .orElse("");
        redisTemplate.opsForValue().set(key, positionsStr, CLICK_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return com.campus.marketplace.common.dto.response.ClickCaptchaResponse.builder()
                .clickId(clickId)
                .backgroundImage(backgroundBase64)
                .targetWords(targetWords)
                .hint(hint)
                .expiresIn(CLICK_EXPIRE_SECONDS)
                .build();
    }

    /**
     * 验证点选验证码（内部方法）
     */
    private boolean verifyClickCaptcha(com.campus.marketplace.common.dto.request.ClickVerifyRequest request) {
        if (request.getClickId() == null || request.getClickPoints() == null || request.getClickPoints().isEmpty()) {
            log.warn("点选验证参数为空");
            return false;
        }

        String key = CLICK_KEY_PREFIX + request.getClickId();
        String storedPositions = redisTemplate.opsForValue().get(key);

        if (storedPositions == null) {
            log.warn("点选验证码不存在或已过期: clickId={}", request.getClickId());
            return false;
        }

        // 解析存储的位置
        String[] positions = storedPositions.split(";");
        if (positions.length != request.getClickPoints().size()) {
            log.warn("点选数量不匹配: expected={}, actual={}", positions.length, request.getClickPoints().size());
            return false;
        }

        // 验证每个点击位置（使用配置的容差范围）
        for (int i = 0; i < positions.length; i++) {
            String[] xy = positions[i].split(",");
            int targetX = Integer.parseInt(xy[0]);
            int targetY = Integer.parseInt(xy[1]);

            com.campus.marketplace.common.dto.request.ClickVerifyRequest.ClickPoint userPoint = request.getClickPoints().get(i);

            int diffX = Math.abs(targetX - userPoint.getX());
            int diffY = Math.abs(targetY - userPoint.getY());

            if (diffX > CLICK_TOLERANCE || diffY > CLICK_TOLERANCE) {
                log.warn("点选位置不匹配: index={}, target=({},{}), user=({},{}), diff=({},{}), tolerance={}",
                        i, targetX, targetY, userPoint.getX(), userPoint.getY(), diffX, diffY, CLICK_TOLERANCE);
                return false;
            }
            
            log.debug("点选位置匹配: index={}, target=({},{}), user=({},{}), diff=({},{})",
                    i, targetX, targetY, userPoint.getX(), userPoint.getY(), diffX, diffY);
        }

        // 验证成功后才删除（防止重复使用）
        redisTemplate.delete(key);
        log.info("点选验证通过: clickId={}, points={}", request.getClickId(), request.getClickPoints().size());
        return true;
    }

    /**
     * 创建点选验证码背景图片（带文字）
     */
    private BufferedImage createClickableImage(int width, int height, java.util.List<String> targetWords, java.util.List<java.awt.Point> targetPositions) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Random random = new Random();

        // 1. 绘制渐变背景
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(245, 250, 255),
                width, height, new Color(230, 240, 250)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);

        // 2. 绘制干扰文字（随机汉字）
        String[] noiseWords = {"福", "禄", "寿", "喜", "财", "贵", "吉", "祥", "安", "康", "乐", "和"};
        g.setFont(new Font("SimSun", Font.BOLD, 28));

        for (int i = 0; i < 15; i++) {
            g.setColor(new Color(
                    random.nextInt(100) + 155,
                    random.nextInt(100) + 155,
                    random.nextInt(100) + 155
            ));
            int x = random.nextInt(width - 40) + 10;
            int y = random.nextInt(height - 40) + 30;
            g.drawString(noiseWords[random.nextInt(noiseWords.length)], x, y);
        }

        // 3. 绘制目标文字（需要点击的文字，用明显颜色）
        g.setFont(new Font("SimSun", Font.BOLD, 32));
        for (String word : targetWords) {
            // 随机位置（但确保不重叠）
            int finalX = random.nextInt(width - 50) + 10;
            int finalY = random.nextInt(height - 50) + 35;

            // 确保不与已有目标重叠
            final int checkX = finalX;
            final int checkY = finalY;
            boolean overlaps = targetPositions.stream()
                    .anyMatch(p -> Math.abs(p.x - checkX) < 40 && Math.abs(p.y - checkY) < 40);

            if (overlaps) {
                // 如果重叠，重新生成位置
                finalX = random.nextInt(width - 50) + 10;
                finalY = random.nextInt(height - 50) + 35;
            }

            targetPositions.add(new java.awt.Point(finalX, finalY));

            // 绘制目标文字（深色，更醒目）
            g.setColor(new Color(random.nextInt(50), random.nextInt(50), random.nextInt(50)));
            g.drawString(word, finalX, finalY);
        }

        g.dispose();
        return image;
    }

    // ========== 统一验证码验证接口（新增 - BaSui 2025-11-11） ==========

    /**
     * 统一验证码验证接口（支持四种验证码类型）
     *
     * @param request 统一验证码验证请求
     * @return 验证码通行证（临时token）
     */
    @Override
    public com.campus.marketplace.common.dto.response.CaptchaVerifyResponse verifyUnifiedCaptcha(
            com.campus.marketplace.common.dto.request.UnifiedCaptchaVerifyRequest request
    ) {
        log.info("收到统一验证码验证请求: type={}, captchaId={}", request.getType(), request.getCaptchaId());

        boolean isValid = false;

        // 根据验证码类型调用对应的验证方法
        switch (request.getType().toLowerCase()) {
            case "image":
                // 图形验证码验证
                if (request.getCaptchaCode() == null) {
                    throw new BusinessException(
                            ErrorCode.PARAM_ERROR,
                            "图形验证码输入不能为空"
                    );
                }
                isValid = verifyImageCaptcha(request.getCaptchaId(), request.getCaptchaCode());
                break;

            case "slider":
                // 滑块验证码验证
                if (request.getSlidePosition() == null) {
                    throw new BusinessException(
                            ErrorCode.PARAM_ERROR,
                            "滑块位置不能为空"
                    );
                }
                // 如果有轨迹数据，使用完整验证
                if (request.getTrack() != null && !request.getTrack().isEmpty()) {
                    com.campus.marketplace.common.dto.request.SlideVerifyRequest slideRequest =
                            new com.campus.marketplace.common.dto.request.SlideVerifyRequest();
                    slideRequest.setSlideId(request.getCaptchaId());
                    slideRequest.setXPosition(request.getSlidePosition());
                    // 转换轨迹数据
                    java.util.List<com.campus.marketplace.common.dto.request.SlideVerifyRequest.TrackPoint> trackPoints =
                            request.getTrack().stream()
                                    .map(p -> {
                                        com.campus.marketplace.common.dto.request.SlideVerifyRequest.TrackPoint tp =
                                                new com.campus.marketplace.common.dto.request.SlideVerifyRequest.TrackPoint();
                                        tp.setX(p.getX());
                                        tp.setY(p.getY());
                                        tp.setT(p.getT());
                                        return tp;
                                    })
                                    .collect(java.util.stream.Collectors.toList());
                    slideRequest.setTrack(trackPoints);
                    isValid = verifySlideCaptchaWithTrack(slideRequest);
                } else {
                    // 简单验证
                    isValid = verifySlideCaptcha(request.getCaptchaId(), request.getSlidePosition());
                }
                break;

            case "rotate":
                // 旋转验证码验证
                if (request.getRotateAngle() == null) {
                    throw new BusinessException(
                            ErrorCode.PARAM_ERROR,
                            "旋转角度不能为空"
                    );
                }
                com.campus.marketplace.common.dto.request.RotateVerifyRequest rotateRequest =
                        new com.campus.marketplace.common.dto.request.RotateVerifyRequest(
                                request.getCaptchaId(),
                                request.getRotateAngle()
                        );
                isValid = verifyRotateCaptcha(rotateRequest);
                break;

            case "click":
                // 点击验证码验证
                if (request.getClickPoints() == null || request.getClickPoints().isEmpty()) {
                    throw new BusinessException(
                            ErrorCode.PARAM_ERROR,
                            "点击坐标不能为空"
                    );
                }
                java.util.List<com.campus.marketplace.common.dto.request.ClickVerifyRequest.ClickPoint> clickPoints =
                        request.getClickPoints().stream()
                                .map(p -> new com.campus.marketplace.common.dto.request.ClickVerifyRequest.ClickPoint(p.getX(), p.getY()))
                                .collect(java.util.stream.Collectors.toList());
                com.campus.marketplace.common.dto.request.ClickVerifyRequest clickRequest =
                        new com.campus.marketplace.common.dto.request.ClickVerifyRequest(
                                request.getCaptchaId(),
                                clickPoints
                        );
                isValid = verifyClickCaptcha(clickRequest);
                break;

            default:
                throw new BusinessException(
                        ErrorCode.PARAM_ERROR,
                        "不支持的验证码类型: " + request.getType()
                );
        }

        // 验证失败
        if (!isValid) {
            log.warn("❌ 验证码验证失败: type={}, captchaId={}", request.getType(), request.getCaptchaId());
            throw new BusinessException(
                    ErrorCode.CAPTCHA_ERROR,
                    "验证码验证失败，请重试"
            );
        }

        // 验证成功，生成验证码通行证（临时token）
        String captchaToken = UUID.randomUUID().toString();
        String key = CAPTCHA_TOKEN_PREFIX + captchaToken;
        redisTemplate.opsForValue().set(key, "verified", CAPTCHA_TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);

        log.info("✅ 验证码验证成功，生成通行证: captchaToken={}", captchaToken);

        return com.campus.marketplace.common.dto.response.CaptchaVerifyResponse.builder()
                .captchaToken(captchaToken)
                .expiresIn(CAPTCHA_TOKEN_EXPIRE_SECONDS)
                .message("验证码验证成功")
                .build();
    }

    /**
     * 验证验证码通行证（临时token）
     *
     * @param captchaToken 验证码通行证
     * @return 验证是否通过
     */
    @Override
    public boolean verifyCaptchaToken(String captchaToken) {
        if (captchaToken == null || captchaToken.isEmpty()) {
            log.warn("验证码通行证为空");
            return false;
        }

        String key = CAPTCHA_TOKEN_PREFIX + captchaToken;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            log.warn("验证码通行证不存在或已过期: captchaToken={}", captchaToken);
            return false;
        }

        // 验证通过后删除token（防止重复使用）
        redisTemplate.delete(key);
        log.info("✅ 验证码通行证验证成功: captchaToken={}", captchaToken);

        return true;
    }
}
