# Spring App

Java 21 / Spring Boot Web 示例，推荐使用项目提供的 Maven Wrapper 构建。

## 开发环境要求

- 必须安装 JDK 21，不限制 JDK 供应商或补丁版本。
- 项目支持 Maven 3.9.x，Maven Enforcer 在 `validate` 阶段检查 Java 与 Maven 版本，不满足要求时构建失败。
- 推荐使用 Maven Wrapper，固定运行 Maven 3.9.15，无需预先安装 Maven。
- Wrapper 不负责安装 JDK；首次使用需要联网下载 Maven，下载内容缓存在用户目录，不提交到仓库。
- 不限制操作系统，源码、资源及报告编码统一为 UTF-8。

检查环境：

```sh
java -version
./mvnw -version
```

标准构建、格式化与运行：

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw spotless:apply
./mvnw clean verify
```

Windows 用户将 `./mvnw` 替换为 `mvnw.cmd`（PowerShell 使用 `.\mvnw.cmd`），例如 `mvnw.cmd clean verify`。

Swagger 仅在 `dev` profile 启用，地址为 `/swagger-ui.html`。

## 持续集成（CI）

CI 在推送到 `main`、创建或更新以 `main` 为目标的 Pull Request，以及手动触发时运行。
工作流使用 GitHub 托管的 Ubuntu Runner、Temurin JDK 21 和 Maven Wrapper 3.9.15，并启用 Maven 依赖缓存。
核心验证命令为 `./mvnw clean verify`（CI 附加 `--batch-mode --no-transfer-progress`），自动执行环境检查、编译、测试、Spring Boot 打包和 Spotless 格式检查；任一步骤失败都会使构建失败。

在仓库的 **Actions → CI** 页面查看执行结果，进入 **Build and Test** 查看各步骤日志。
工作流进入默认分支后，可通过 **Run workflow** 手动触发。无需配置额外 Secrets。

## 日志策略

使用 Spring Boot 默认的 SLF4J + Logback。默认及 dev 环境输出文本控制台日志，prod 环境输出 Logstash 风格的 JSON 控制台日志。项目默认不创建日志文件；Docker 部署时推荐通过 stdout/stderr 交由外部日志平台采集。

```sh
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

未知系统异常在处理边界记录 ERROR 和完整堆栈；预期业务异常、请求参数错误默认不打印 ERROR 堆栈，普通 400/404 不自动视为系统故障。系统异常详情不会暴露给 API 客户端。

禁止主动记录密码、Token、认证头、Cookie/Session 凭据、API Key、私钥等敏感信息，也不要整体输出未经审查的请求/响应对象。完整 Throwable 的 message 和堆栈仍可能包含敏感内容：内部异常不要嵌入秘密，第三方异常按风险选择原始 Throwable 或受控摘要与异常类型。详细级别和敏感信息规范见 AGENTS.md。

## Request ID

每个进入应用 Filter 的新 HTTP 请求都由服务端生成 UUID v4 requestId，忽略客户端传入的 `X-Request-ID`。成功及应用处理的错误响应通过 `X-Request-ID` 响应头返回该 ID，业务 JSON 结构不变。

```sh
curl -i http://localhost:8080/examples/success
```

示例响应头：

```http
X-Request-ID: 7d9c25a8-f03b-4e61-8d12-10e847ae6b02
```

前端可用 `response.headers.get("X-Request-ID")` 读取并展示问题编号，开发者据此查询已有日志。MDC Key 为 `requestId`；默认/dev 文本日志显示该字段，prod JSON 日志通过原生 MDC 支持输出同名字段。非请求日志没有 ID 是正常的，成功请求不会因此自动生成访问日志。

同一请求的 REQUEST、ERROR（含嵌套 ERROR）及 ASYNC 再分派复用请求属性中的 ID；每次分派结束会清理或恢复当前线程 MDC。当前不自动传播到 Callable、线程池、CompletableFuture 等异步任务线程，也不提供分布式 traceId/spanId。未进入 Filter 的请求（例如代理拒绝）不保证返回应用 ID；响应已经提交时无法补写 Header。

当前未配置全局 CORS。未来跨域部署时，需按既有来源与凭据策略将 `X-Request-ID` 加入 `exposedHeaders`，浏览器才能读取，不能为此全局放行跨域。requestId 不是身份凭证，不能作为授权依据或业务审计记录。

## 错误响应国际化

错误响应保持 `{ "code": "...", "message": "..." }`，HTTP 状态由 `ErrorCode` 定义。
`code` 是稳定的 API 错误标识，`messageKey` 是内部国际化资源标识，两者独立。
所有 ErrorCode 的 messageKey 统一采用 `error.<module>.<error_name>`，例如 `error.common.not_found`、`error.request.validation_failed`。
对外 code 统一采用 `MODULE_ERROR_NAME`：CommonError 使用 `COMMON_` 前缀，RequestError 使用 `REQUEST_` 前缀。
未来业务模块使用对应的 `USER_`、`ORDER_`、`PAYMENT_` 等前缀，无需添加 `error.` 前缀。
例如：

```text
code       = COMMON_NOT_FOUND
messageKey = error.common.not_found
status     = 404
```

`ErrorCode` 实现 `getCode()`、`getStatus()`、`getMessageKey()`。

`Accept-Language` 选择中文或英文，支持 `zh-CN`、`en-US` 等地区语言；未提供请求头或没有匹配的翻译资源时使用英文，不依赖服务器默认 Locale。
Spring Boot 原生 MessageSource 使用 UTF-8 的 `messages.properties`（英文兜底）与 `messages_zh.properties`，禁用系统 Locale 回退。
Locale 解析使用 Spring Boot 自动配置，在 `application.yml` 中设置 `spring.web.locale: en`，无需自定义 LocaleResolver 配置类。

```sh
curl -H 'Accept-Language: zh-CN' http://localhost:8080/examples/business-error
# HTTP 404: {"code":"COMMON_NOT_FOUND","message":"资源不存在"}
curl -H 'Accept-Language: en-US' http://localhost:8080/examples/business-error
# HTTP 404: {"code":"COMMON_NOT_FOUND","message":"Resource not found"}
```

抛出业务异常时使用 `BusinessException.of(CommonError.NOT_FOUND)`。
动态消息使用 `BusinessException.of(error, "alice")`，对应资源 Key 例如 `error.test.user_not_found`，模板例如中文 `用户 {0} 不存在`、英文 `User {0} not found`。
参数数组会进行防御性复制；参数对象本身不做深拷贝，应使用字符串、数字等不可变值。
业务异常不解析请求语言，`getMessage()` 仅提供诊断用 Key；最终文案在 HTTP 异常响应边界生成。

新增错误必须补齐中英文资源，业务 code 推荐使用 `USER_INVALID_EMAIL` 等模块前缀。
缺失或格式错误的 Key 使用当前语言的通用内部错误文案，保持原始错误码和 HTTP 状态；通用文案本身缺失或格式错误时固定回退为“Internal server error”。客户端不会收到异常详情或缺失 Key。
未知异常仍完整记录服务端日志，客户端只收到 HTTP 500、`COMMON_INTERNAL_SERVER_ERROR` 和安全通用消息。

迁移说明：旧 `ErrorCode.getMessage()` 改为 `getMessageKey()`；删除接受原始 code/message/status 的异常构造函数与自定义完整消息重载。单个 String 参数现在仅代表模板参数，原有中文覆盖消息调用应改为默认错误模板或新增明确的模板 Key。HTTP 状态和 JSON 结构保持不变；示例业务错误中文文案统一为“资源不存在”。

API code 迁移说明：CommonError 的 6 个 code 添加 `COMMON_` 前缀，RequestError 的 5 个 code 添加 `REQUEST_` 前缀。此次变更影响对外 API，依赖旧 code 的客户端需同步迁移；枚举常量名称、HTTP 状态、messageKey、消息文案及 JSON 结构保持不变。
