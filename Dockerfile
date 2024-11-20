# 构建阶段
FROM maven:3.8-openjdk-8 AS builder

WORKDIR /build

# 复制 pom.xml 并下载依赖（利用缓存）
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:8-jdk-slim

WORKDIR /app

# 从构建阶段复制jar包
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8081

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
