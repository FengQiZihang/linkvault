# LinkVault Agent Guide

本文件是 Codex / AI 后续开发当前后端项目时的入口约束。实现前先读本文件，再按权威文档顺序查证，不要跳过现有文档直接发明规则。

## 权威文档顺序

1. `../../docs/README.md`
2. `../../docs/engineering/接口文档.md`
3. `../../docs/engineering/openapi.yaml`
4. `../../docs/engineering/数据库字段设计.md`
5. `../../docs/sql/001_schema.sql`
6. `../../docs/engineering/技术栈规范与环境基线.md`
7. `../../docs/engineering/后端代码风格约定-基于sky-take-out项目.md`

当文档冲突时，先判断冲突所在层级，再同步修正文档和代码。接口字段、VO、错误码以前端联调文档为准；数据库结构以 `../../docs/sql/001_schema.sql` 为最终可执行落点。

## 后端开发规则

- 后端工程位于 `backend/linkvault-server`，是 Spring Boot 单模块单体。
- 分层固定为 `controller -> service -> mapper -> MySQL`，Controller 只接参、记录日志、调用 Service、返回 `ApiResponse`。
- Controller 的 `@RequestMapping` 只写资源路径，例如 `/auth`、`/me`、`/tags`；不要在后端代码中添加 `/api`，API 代理前缀由前端代理、Apifox 代理或网关 rewrite 处理。
- Entity 只映射数据库表，DTO 只做请求入参，VO 只做接口出参，禁止把 Entity 直接返回给前端。
- Service 接口和 `service/impl` 成对出现；`ServiceImpl` 的 public 业务方法使用 `// 1、...`、`// 2、...` 顺序注释展开主流程。
- 如果某个步骤下代码变多，抽成私有方法；主流程保持一屏内可读。
- 数据库操作优先使用 MyBatis-Plus；复杂动态 SQL 或多表查询再补 XML。
- 查询当前用户私有数据时必须显式带 `user_id` 条件，不能只凭 `bookmarkId`、`tagId` 查询后再信任结果。

## 通用代码约束

- 统一响应使用 `ApiResponse<T>`。
- 业务异常必须使用 `new BusinessException(ErrorCode.Xxx)`；禁止在业务代码中散落 `40001`、`40101`、`42901` 等数字错误码。
- 新增错误码必须先更新 `../../docs/engineering/接口文档.md` 的错误码表，再更新 `ErrorCode` 枚举。
- 分页请求统一使用 `page`、`pageSize`；分页响应统一使用 `PageResponse<T>` 的 `items`、`page`、`pageSize`、`total`。
- 默认分页参数为 `page=1`、`pageSize=20`；首页最近保存等特殊场景在接口实现中显式设置 `pageSize=5`。
- Entity 中的 `createdAt`、`updatedAt` 由 MyBatis-Plus `MetaObjectHandler` 自动填充，业务代码不要手动设置。
- 所有写入数据库的业务时间使用 UTC；优先使用 `TimeUtils.nowUtc()`，不要直接使用 `LocalDateTime.now()`。
- `lv_bookmark.note` 未填写时保存空字符串，不使用 `null`。
- `lv_link.original_url` 仅用于排查；接口展示和打开原始链接必须使用 `lv_bookmark.original_url`。

## 日志和注释风格

- Controller 打印接口入口和完成日志；ServiceImpl 只打印关键业务完成点，避免每个私有步骤都打日志。
- 日志禁止打印完整 token、验证码明文和其它敏感信息。
- 基础数据类成员属性使用行末注释。
- 工具/配置/支撑类方法注释两种风格：
  - 简单职责方法使用 `/** ... */`。
  - 有明确执行流程的方法在方法内部使用分点独立行注释。
- 不为显而易见的代码添加空洞注释；注释要解释字段含义、业务边界或流程意图。

## 接口和文档同步

- 后续功能开发必须按 `../../docs/engineering/接口文档.md` 的路径、请求、响应、VO、错误码实现。
- 不接入 Knife4j / Swagger 作为新的接口权威源；当前接口权威仍是 Markdown 文档和 `openapi.yaml`。
- 如接口实现需要变更请求或响应结构，必须同步更新接口文档和 `openapi.yaml`。
- 如数据库字段或索引变化，必须同步更新 `../../docs/engineering/数据库字段设计.md` 和 `../../docs/sql/001_schema.sql`。

## 验证要求

- 后端代码改动后至少运行编译与单测：

```bash
cd backend/linkvault-server
mvn test
```

- 涉及接口行为时，继续按 `../../docs/testing/后端接口验证闭环.md` 启动服务并执行 `curl` 接口验证；不能只停在 `mvn test`。
- 启动服务前必须先按顺序执行 `../../docs/sql/001_schema.sql` 和 `../../docs/sql/002_seed_test_data.sql` 重置本地测试数据。
- 如果接口验证失败，修复后重新执行 `mvn test -> 重置测试数据 -> 启动服务 -> curl 验证`，直到通过或明确说明阻塞原因。
- 验证通过后不要清空数据库，保留测试结果供用户用 Apifox 和数据库工具查看。
- 验证完成后关闭本地后端进程，避免占用 `8080` 端口。
- 最终说明必须列出实际请求过的接口路径、HTTP 状态码和业务 `code`；Apifox 手工验证点可作为用户复核项，但不能替代 AI 自己执行的 curl 验证。
- 不要回滚用户已有改动；如果工作区有无关脏文件，只处理当前任务相关文件。
