# Spring App

[English](README.md) | 简体中文

轻量级 Spring Boot REST API 开发模板，提供常见后端基础能力，不预先绑定数据库、认证授权或部署架构。

**技术栈：** Java 21 · Spring Boot 4.1.1 · Maven Wrapper 3.9.15

## 核心能力

- Spring MVC 与 Jakarta Validation，支持 HTTP API 和请求参数校验。
- 统一错误码与 BusinessException，支持中英文错误响应。
- 仅开发环境启用的 Swagger UI 和 OpenAPI 文档。
- SLF4J + Logback、生产 JSON 控制台日志及 Request ID 日志关联。
- JUnit、MockMvc、Spotless 和 GitHub Actions CI。

## 快速开始

安装 JDK 21。项目提供的 Wrapper 会下载 Maven，无需单独安装 Maven。

```bash
./mvnw clean verify
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

默认端口为 **8080**。启用 dev 后可访问：

- Swagger UI：<http://localhost:8080/swagger-ui.html>
- OpenAPI：<http://localhost:8080/v3/api-docs>
- 示例接口：<http://localhost:8080/examples/success>

Windows 使用 `mvnw.cmd` 替代 `./mvnw`，PowerShell 使用 `.\mvnw.cmd`。

## 环境配置

| Profile | Swagger / 示例接口 | 控制台日志 |
|---|---|---|
| 默认 | 关闭 | 文本 |
| dev | 开启 | 文本 |
| prod | 关闭 | JSON |

`ExampleController` 和 Swagger 仅在 dev 启用。生产环境禁止激活 dev，包括同时使用 dev 和 prod。生产日志采用 Logstash JSON 格式并输出至控制台，默认不创建日志文件。

## API 约定

统一错误响应仅包含 `code` 和 `message`：

```json
{
  "code": "COMMON_NOT_FOUND",
  "message": "Resource not found"
}
```

`ErrorCode` 定义对外错误码、HTTP 状态和国际化消息标识，`BusinessException` 用于预期业务错误。`Accept-Language: zh-CN` 选择中文，`en-US` 选择英文；未提供或不支持的语言回退英文。未知系统异常返回安全文案，不向客户端暴露内部细节。

服务端生成 Request ID，通过成功响应及应用处理的错误响应头 `X-Request-ID` 返回，用于关联已有日志；客户端传入的 ID 不被信任。它不是身份凭证。浏览器跨域读取时，需按应用的 CORS 策略暴露该响应头。

详细开发与日志规范见 [AGENTS.md](AGENTS.md)。提交代码或创建 PR 前运行 `./mvnw clean verify`；CI 在 main push、目标为 main 的 PR 及手动触发时执行相同检查。

## 从模板创建项目

1. 仓库已启用模板功能时，使用 GitHub 的 **Use this template**，或将代码复制到新仓库。
2. 修改 `pom.xml` 的项目坐标、`name`、`description`（保留 Boot parent），`src/main/resources/application.yml` 的 `spring.application.name`，以及 `org.example` 包名。同步修改源码和测试包、导入及 `package-info.java`，保持 `Application` 位于合适的组件扫描根包。
3. 按业务需要替换或删除 dev 示例，同步调整相关测试、OpenAPI 断言和 README 内容。
4. 执行 `./mvnw clean verify`，再以目标 Profile 启动检查。本仓库不会自动重命名项目。

数据库、认证授权、缓存、消息队列、审计存储、Docker 部署及分布式追踪由实际业务按需引入，模板未内置这些组件。
