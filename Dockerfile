# ---- Build Stage ----
# 使用包含 JDK 和 Maven 的镜像作为构建环境
FROM maven:3.9.9-eclipse-temurin-21-jammy AS builder
WORKDIR /workspace/app

COPY pom.xml pom.xml
# 利用 Maven 缓存下载依赖项 (优化层缓存)
# 如果 pom.xml 没有改变，这一层将被缓存
RUN mvn dependency:go-offline

# 复制源代码
COPY src src

# 打包应用，跳过测试 (测试应在 CI 流程的单独步骤中完成)
RUN mvn package -DskipTests

# ---- Package Stage ----
# 使用仅包含 JRE 的轻量级镜像作为最终运行环境
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 定义非 root 用户和组
ARG USER=spring
ARG GROUP=spring
RUN addgroup --system ${GROUP} && adduser --system --ingroup ${GROUP} ${USER}

# 从构建阶段复制打包好的 JAR 文件
COPY --from=builder /workspace/app/target/*.jar app.jar

# 更改 JAR 文件的所有者为非 root 用户
RUN chown ${USER}:${GROUP} /app/app.jar

# 设置QWeather API 私钥的路径
RUN echo "$QWEATHER_PRIVATE_KEY" > /app/qweather_private_key.pem && \
    chown ${USER}:${GROUP} /app/qweather_private_key.pem
ENV QWEATHER_PRIVATE_KEY_PATH  /app/qweather_private_key.pem

# 切换到非 root 用户
USER ${USER}:${GROUP}

# 暴露应用程序端口
EXPOSE 8080

# 设置容器启动命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
