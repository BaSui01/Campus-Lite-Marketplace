package com.campus.marketplace.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * IP 地理位置工具类
 * <p>
 * 使用 ip2region 离线库解析 IP 地址的地理位置信息。
 * 数据格式：国家|区域|省份|城市|ISP
 * </p>
 *
 * @author BaSui 😎 - IP定位神器，离线查询贼快！
 * @since 2025-11-08
 */
@Slf4j
@Component
public class IpLocationUtil {

    private Searcher searcher;
    private static final String DB_PATH = "ip2region/ip2region.xdb";

    /**
 * 初始化 IP2Region 搜索器
     * <p>
     * 从 classpath 加载 ip2region.xdb 数据库文件。
     * 如果文件不存在，会记录警告日志，但不会抛出异常。
     * </p>
     */
    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(DB_PATH);
            if (!resource.exists()) {
                log.warn("⚠️ IP2Region 数据库文件不存在: {}", DB_PATH);
                log.warn("💡 请从 https://github.com/lionsoul2014/ip2region/tree/master/data 下载 ip2region.xdb");
                log.warn("💡 并放置到 src/main/resources/ip2region/ 目录下");
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                byte[] dbBinStr = inputStream.readAllBytes();
                searcher = Searcher.newWithBuffer(dbBinStr);
                log.info("✅ IP2Region 搜索器初始化成功！");
            }
        } catch (IOException e) {
            log.error("❌ IP2Region 搜索器初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据 IP 地址查询地理位置
     * <p>
     * 返回格式：国家|区域|省份|城市|ISP
     * 示例：中国|0|广东省|深圳市|电信
     * </p>
     *
     * @param ip IP 地址（支持 IPv4）
     * @return 地理位置字符串，如果查询失败返回 "未知"
     */
    public String getLocation(String ip) {
        if (searcher == null) {
            log.warn("⚠️ IP2Region 搜索器未初始化，无法查询 IP: {}", ip);
            return "未知";
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return "未知";
        }

        // 处理本地 IP
        if (isLocalIp(ip)) {
            return "本地";
        }

        try {
            String region = searcher.search(ip);
            if (region == null || region.isEmpty()) {
                return "未知";
            }

            // 格式化输出：只保留省份和城市
            return formatLocation(region);
        } catch (Exception e) {
            log.error("❌ 查询 IP 地理位置失败: ip={}, error={}", ip, e.getMessage());
            return "未知";
        }
    }

    /**
     * 格式化地理位置信息
     * <p>
     * 输入：中国|0|广东省|深圳市|电信
     * 输出：广东省深圳市
     * </p>
     *
     * @param region 原始地理位置字符串
     * @return 格式化后的地理位置
     */
    private String formatLocation(String region) {
        if (region == null || region.isEmpty()) {
            return "未知";
        }

        String[] parts = region.split("\\|");
        if (parts.length < 4) {
            return region;
        }

        String country = parts[0];
        String province = parts[2];
        String city = parts[3];

        // 如果是中国，只返回省份+城市
        if ("中国".equals(country)) {
            if ("0".equals(province) || province.isEmpty()) {
                return city.isEmpty() || "0".equals(city) ? "中国" : city;
            }
            if ("0".equals(city) || city.isEmpty()) {
                return province;
            }
            return province + city;
        }

        // 如果是国外，返回国家+城市
        if (city.isEmpty() || "0".equals(city)) {
            return country;
        }
        return country + " " + city;
    }

    /**
     * 判断是否为本地 IP
     *
     * @param ip IP 地址
     * @return 是否为本地 IP
     */
    private boolean isLocalIp(String ip) {
        return "127.0.0.1".equals(ip)
                || "localhost".equalsIgnoreCase(ip)
                || "0:0:0:0:0:0:0:1".equals(ip)
                || "::1".equals(ip);
    }

    /**
     * 关闭搜索器（释放资源）
     */
    public void close() {
        if (searcher != null) {
            try {
                searcher.close();
                log.info("✅ IP2Region 搜索器已关闭");
            } catch (IOException e) {
                log.error("❌ 关闭 IP2Region 搜索器失败: {}", e.getMessage());
            }
        }
    }
}
