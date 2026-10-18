# Spring App

Java 21 / Spring Boot Web 示例，使用 Maven 构建。运行与验证：

```sh
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spotless:apply
mvn clean verify
```

Swagger 仅在 `dev` profile 启用，地址为 `/swagger-ui.html`。

## 错误响应国际化

错误响应保持 `{ "code": "...", "message": "..." }`，HTTP 状态由 `ErrorCode` 定义。
`code` 是稳定的 API 错误标识，`messageKey` 是内部国际化资源标识，两者独立。
所有 ErrorCode 的 messageKey 统一采用 `error.<module>.<error_name>`，例如 `error.common.not_found`、`error.request.validation_failed`。
对外的 code 保持 `NOT_FOUND`、`VALIDATION_FAILED` 等原值，不要求添加 `error.` 前缀。
`ErrorCode` 实现 `getCode()`、`getStatus()`、`getMessageKey()`。

`Accept-Language` 选择中文或英文，支持 `zh-CN`、`en-US` 等地区语言；未提供请求头或没有匹配的翻译资源时使用英文，不依赖服务器默认 Locale。
Spring Boot 原生 MessageSource 使用 UTF-8 的 `messages.properties`（英文兜底）与 `messages_zh.properties`，禁用系统 Locale 回退。
Locale 解析使用 Spring Boot 自动配置，在 `application.yml` 中设置 `spring.web.locale: en`，无需自定义 LocaleResolver 配置类。

```sh
curl -H 'Accept-Language: zh-CN' http://localhost:8080/examples/business-error
# HTTP 404: {"code":"NOT_FOUND","message":"资源不存在"}
curl -H 'Accept-Language: en-US' http://localhost:8080/examples/business-error
# HTTP 404: {"code":"NOT_FOUND","message":"Resource not found"}
```

抛出业务异常时使用 `BusinessException.of(CommonError.NOT_FOUND)`。
动态消息使用 `BusinessException.of(error, "alice")`，对应资源 Key 例如 `error.test.user_not_found`，模板例如中文 `用户 {0} 不存在`、英文 `User {0} not found`。
参数数组会进行防御性复制；参数对象本身不做深拷贝，应使用字符串、数字等不可变值。
业务异常不解析请求语言，`getMessage()` 仅提供诊断用 Key；最终文案在 HTTP 异常响应边界生成。

新增错误必须补齐中英文资源，业务 code 推荐使用 `USER_INVALID_EMAIL` 等模块前缀。
缺失或格式错误的 Key 使用当前语言的通用内部错误文案，保持原始错误码和 HTTP 状态；通用文案本身缺失或格式错误时固定回退为“Internal server error”。客户端不会收到异常详情或缺失 Key。
未知异常仍完整记录服务端日志，客户端只收到 HTTP 500、`INTERNAL_SERVER_ERROR` 和安全通用消息。

迁移说明：旧 `ErrorCode.getMessage()` 改为 `getMessageKey()`；删除接受原始 code/message/status 的异常构造函数与自定义完整消息重载。单个 String 参数现在仅代表模板参数，原有中文覆盖消息调用应改为默认错误模板或新增明确的模板 Key。现有 code、HTTP 状态和 JSON 结构保持不变；示例业务错误中文文案统一为“资源不存在”。
