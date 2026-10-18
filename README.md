# Spring App

轻量级 Spring Boot REST API 开发模板，提供 HTTP API 基础结构、参数校验、统一错误处理、中英文国际化、开发环境 Swagger、日志规范、Request ID、自动化测试及 GitHub Actions CI。

技术栈：Java 21、Spring Boot 4.1.1、Spring MVC、Jakarta Validation、SpringDoc OpenAPI、SLF4J + Logback、JUnit/MockMvc、Spotless、Maven Wrapper 3.9.15。

## 开发环境要求

- 必须安装 JDK 21，不限制操作系统、JDK 供应商或补丁版本。
- 支持 Maven 3.9.x，推荐使用固定 Maven 3.9.15 的 Wrapper，无需预先安装 Maven。
- Wrapper 不负责安装 JDK；首次使用需联网下载 Maven，缓存位于用户目录，不提交到仓库。
- Enforcer 在 `validate` 阶段检查 Java/Maven 版本；源码、资源与报告统一使用 UTF-8。

```sh
java -version
./mvnw -version
```

## 快速启动

```sh
./mvnw clean verify
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

默认端口为 8080。启用 dev 后可访问：

- 示例：`http://localhost:8080/examples/success`
- Swagger：`http://localhost:8080/swagger-ui.html`
- OpenAPI：`http://localhost:8080/v3/api-docs`

Windows 将 `./mvnw` 替换为 `mvnw.cmd`，PowerShell 使用 `.\mvnw.cmd`。例如 `mvnw.cmd clean verify`。

## Profile 说明

| Profile | 演示接口 `/examples/**` | Swagger / OpenAPI | 控制台日志 |
|---|---|---|---|
| 默认 | 不注册 | 关闭 | 文本 |
| dev | 注册 | 开启 | 文本 |
| prod | 不注册 | 关闭 | Logstash JSON |

`ExampleController` 仅在 dev 注册；默认和 prod 访问原示例路径按正常 MVC 行为返回 404。不要在生产部署启用 dev，包括与 prod 同时启用。业务接口应根据实际需求独立开发，不应依赖演示 Controller。

生产模式启动示例（POSIX shell）：

```sh
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

## 从模板创建新项目

1. 如果仓库已开启 GitHub **Template repository**，使用 **Use this template → Create a new repository** 创建项目。若未开启，维护者可在仓库 **Settings → General → Template repository** 中手动启用，也可先复制代码再初始化自己的仓库。
2. 修改 `pom.xml` 中项目自身的 `groupId`、`artifactId`、`name` 和 `description`；保留 Spring Boot parent 坐标。同步更新依赖这些名称的文档和 JAR 运行命令。
3. 将 `org.example` 重命名为业务根包，同步调整 `src/main/java` 和 `src/test/java` 下的目录、package 声明、导入、相关测试与配置，以及各级 `package-info.java`。保持 `Application` 位于能够扫描业务组件的根包，核对 Request ID 等基础配置仍能被扫描。
4. 修改 `src/main/resources/application.yml` 的 `spring.application.name`。
5. 根据需求保留或删除 dev 专用 `ExampleController`；删除时同步调整依赖示例路径的测试、OpenAPI 断言及文档，保留基础异常处理和 Request ID 的有效测试。
6. 更新 README 中项目定位、名称、接口示例及使用说明，执行 `./mvnw clean verify`，再以目标 Profile 启动检查。

本仓库保留 `spring-app`、`org.example` 作为模板示例，不会自动替换项目坐标或包名。

## 统一错误处理与国际化

`ErrorResponse` 仅包含 `code` 与 `message`。`ErrorCode` 的职责独立：

```text
code       = COMMON_NOT_FOUND          # 对外稳定的 API 错误标识
status     = 404                       # HTTP 状态
messageKey = error.common.not_found    # 内部国际化资源标识
```

CommonError 使用 `COMMON_` 前缀，RequestError 使用 `REQUEST_` 前缀；业务模块使用 `USER_`、`ORDER_`、`PAYMENT_` 等前缀。messageKey 统一采用 `error.<module>.<error_name>`，新增错误需补齐中英文资源。

使用 `BusinessException.of(CommonError.NOT_FOUND)`；动态消息使用 `BusinessException.of(error, "alice")`，模板采用 `{0}`、`{1}`。单个 String 参数是模板参数，不是完整文案覆盖。异常不提前翻译，`getMessage()` 仅用于诊断，最终文案在 HTTP 响应边界解析；参数数组采用防御性复制，参数对象建议使用不可变值。

`Accept-Language: zh-CN` 返回中文，`en-US` 返回英文；无请求头或没有匹配的翻译资源时回退英文，不依赖系统 Locale。UTF-8 资源为 `messages.properties`（英文默认）和 `messages_zh.properties`。

以下命令需先启用 dev：

```sh
curl -H 'Accept-Language: zh-CN' http://localhost:8080/examples/business-error
# HTTP 404: {"code":"COMMON_NOT_FOUND","message":"资源不存在"}
curl -H 'Accept-Language: en-US' http://localhost:8080/examples/business-error
# HTTP 404: {"code":"COMMON_NOT_FOUND","message":"Resource not found"}
```

缺失或无法格式化的消息使用安全通用错误文案，保持原 code 与状态；通用资源也不可用时回退为 `Internal server error`。未知异常返回 HTTP 500、`COMMON_INTERNAL_SERVER_ERROR` 和本地化通用消息，不向客户端暴露异常详情。

## 日志与 Request ID

使用 Spring Boot 默认 SLF4J + Logback，prod 通过原生结构化日志输出 Logstash JSON。默认不创建日志文件；Docker 部署可使用 stdout/stderr，由外部平台采集。

未知系统异常在处理边界记录一次 ERROR 和完整堆栈。预期业务异常及请求参数错误默认不记录 ERROR 堆栈，普通 400/404 不自动视为系统故障。

禁止主动记录密码、Token、Authorization、Cookie/Session 凭据、API Key、私钥及未经审查的敏感字段，不整体输出请求/响应对象。Throwable 的 message 和堆栈仍可能包含敏感内容：内部异常不要嵌入秘密，第三方异常按风险选择原始 Throwable 或受控摘要与异常类型。具体规范见 AGENTS.md。

进入应用 Filter 的每个新请求由服务端生成 UUID v4，忽略客户端 `X-Request-ID`，并通过同名响应头返回。成功及应用处理的错误响应均使用此机制，业务 JSON 不增加字段。

启用 dev 后：

```sh
curl -i http://localhost:8080/examples/success
```

示例响应头：

```http
X-Request-ID: 7d9c25a8-f03b-4e61-8d12-10e847ae6b02
```

前端可通过 `response.headers.get("X-Request-ID")` 展示或保存问题编号，开发者据此查询已有日志。MDC Key 为 `requestId`，默认/dev 文本及 prod JSON 请求日志自动包含它；启动日志没有 ID 属正常情况，不会为了成功请求自动生成访问日志。

REQUEST、ERROR（含嵌套 ERROR）、ASYNC 再分派复用 ID，每次分派结束清理或恢复 MDC。不自动传播到 Callable、线程池或 CompletableFuture，不提供分布式 traceId/spanId。未进入 Filter 的请求不保证应用 ID，响应提交后无法补写 Header。

当前没有全局 CORS；跨域部署时按实际来源和凭据策略将 `X-Request-ID` 加入 `exposedHeaders`，不能为此默认全局允许跨域。Request ID 不是身份凭证，不用作授权或替代业务审计记录。

## 测试与 CI

```sh
./mvnw test
./mvnw spotless:apply
./mvnw spotless:check
./mvnw clean verify
```

提交代码或创建 PR 前执行 `./mvnw clean verify`，覆盖 Enforcer、编译、测试、Boot 打包和 Spotless 检查。新增业务接口应补充测试；依赖示例接口的测试明确启用 dev，默认/prod 测试验证隔离。

CI 在 main push、目标为 main 的 PR 及手动触发时运行，使用 Ubuntu、Temurin JDK 21、Maven Wrapper 和 Maven 缓存，核心命令为 `./mvnw --batch-mode --no-transfer-progress clean verify`。在仓库 **Actions → CI → Build and Test** 查看结果与日志。工作流进入默认分支后可通过 **Run workflow** 手动触发，无需额外 Secrets。

## 按需扩展

模板未内置数据库、ORM/SQL 框架、认证授权、Redis、消息队列、审计日志存储、Docker 部署配置或分布式追踪。这些组件由实际业务项目按需引入，不作为运行模板的前置条件。
