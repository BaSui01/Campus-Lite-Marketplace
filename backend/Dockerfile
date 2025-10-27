# =====================================================
# 校园轻享集市系统 - 后端 Dockerfile（多阶段构建）
# 
# 构建命令：
# docker build -t campus-marketplace-backend:latest .
# 
# 运行命令：
# docker run -d -p 8080:8080 --name campus-backend \
#   -e SPRING_PROFILES_ACTIVE=prod \
#   campus-marketplace-backend:latest
# =====================================================

# ==================== 阶段1：构建阶段 ====================
FROM eclipse-temurin:21-jdk-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制 Maven 包装器和项目配置文件
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# 下载依赖（利用 Docker 缓存）
RUN ./mvnw dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用（跳过测试以加速构建）
RUN ./mvnw clean package -DskipTests -B

# ==================== 阶段2：运行阶段 ====================
FROM eclipse-temurin:21-jre-alpine

# 安装 curl（用于健康检查）
RUN apk add --no-cache curl

# 创建非 root 用户运行应用（安全最佳实践）
RUN addgroup -g 1001 appuser && adduser -u 1001 -G appuser -s /bin/sh -D appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/target/*.jar app.jar

# 修改文件所有者
RUN chown -R appuser:appuser /app

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# JVM 参数优化（生产环境）
ENV JAVA_OPTS="-Xms512m -Xmx2g \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/var/log/campus-marketplace/heap_dump.hprof \
    -Djava.security.egd=file:/dev/./urandom"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
