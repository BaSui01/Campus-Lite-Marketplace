# 🦠 ClamAV 病毒扫描集成方案

> **作者**: BaSui 😎 | **创建日期**: 2025-11-08

---

## 🎯 目标

将 ClamAV 开源病毒扫描引擎集成到文件上传流程中，提供真实的病毒扫描能力。

---

## 📋 当前状态

**现有实现：** `FileSecurityServiceImpl.scanForVirus()`
- ✅ 已实现基础框架
- ⚠️ 仅模拟扫描（检测文件名和简单特征）
- ❌ 无法检测真实病毒

---

## 🛠️ 推荐方案：使用 ClamAV Java 客户端

**依赖：**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>xyz.capybara</groupId>
    <artifactId>clamav-client</artifactId>
    <version>2.1.2</version>
</dependency>
```

**Docker 部署：**
```yaml
# docker/docker-compose.yml
services:
  clamav:
    image: clamav/clamav:latest
    container_name: campus-marketplace-clamav
    ports:
      - "3310:3310"
    volumes:
      - clamav-data:/var/lib/clamav
    environment:
      - CLAMAV_NO_FRESHCLAM=false
    restart: unless-stopped

volumes:
  clamav-data:
```

**实现代码：**
```java
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

@Service
public class ClamAVFileSecurityService implements FileSecurityService {
    
    private final ClamavClient clamavClient;
    
    public ClamAVFileSecurityService(
        @Value("${clamav.host:localhost}") String host,
        @Value("${clamav.port:3310}") int port
    ) {
        this.clamavClient = new ClamavClient(host, port);
    }
    
    @Override
    public String scanForVirus(MultipartFile file) {
        try {
            ScanResult result = clamavClient.scan(file.getInputStream());
            
            if (result instanceof ScanResult.OK) {
                return "CLEAN";
            } else if (result instanceof ScanResult.VirusFound) {
                return "INFECTED";
            } else {
                return "ERROR";
            }
        } catch (Exception e) {
            log.error("病毒扫描异常: {}", file.getOriginalFilename(), e);
            return "ERROR";
        }
    }
}
```

**配置：**
```env
# .env
CLAMAV_ENABLED=true
CLAMAV_HOST=localhost
CLAMAV_PORT=3310
```

---

**详细文档请参考项目 Wiki 或联系 BaSui 😎**
