# 后端代码风格约定（基于 `sky-take-out` 项目）

## 0. 文档目的

本文档用于沉淀当前仓库后续后端开发时应优先靠拢的代码风格。

风格来源不是抽象的“最佳实践”，而是用户过往后端学习项目 `sky-take-out` 中稳定出现的写法。本文基于以下代表性文件做归纳：

- `sky-server/src/main/java/com/sky/controller/admin/EmployeeController.java`
- `sky-server/src/main/java/com/sky/service/impl/EmployeeServiceImpl.java`
- `sky-server/src/main/java/com/sky/service/impl/UserServiceImpl.java`
- `sky-server/src/main/java/com/sky/handler/GlobalExceptionHandler.java`
- `sky-server/src/main/java/com/sky/config/WebMvcConfiguration.java`
- `sky-server/src/main/java/com/sky/mapper/EmployeeMapper.java`
- `sky-server/src/main/resources/mapper/EmployeeMapper.xml`
- `sky-common/src/main/java/com/sky/result/Result.java`
- `sky-common/src/main/java/com/sky/result/PageResult.java`
- `sky-common/src/main/java/com/sky/constant/MessageConstant.java`

本文档的目标是让后续代码“看起来像你自己写的”，而不是只是“功能正确”。

---

## 1. 风格总原则

你的后端写法有几个很明确的特征：

- 分层直白，不追求花哨抽象。
- 业务语义优先，代码先让人看懂再谈技巧。
- 类名、方法名、包结构都偏教学型、显式型。
- 关键流程喜欢按 `1. 2. 3.` 的步骤式注释展开。
- 常量、异常、配置、返回结构会单独归类，不混在业务类里。
- 简单 CRUD 倾向直接写清楚，不喜欢过度包装。

可以把这种风格概括为：

> 朴素、直接、分层清楚、业务步骤显式、方便回读和讲解。

---

## 2. 包结构风格

你偏好的包结构是按职责硬分层，而不是按复杂 DDD 概念拆得很细。

推荐保持以下组织方式：

```text
common/
  constant/
  context/
  enumeration/
  exception/
  properties/
  result/
  utils/

config/
controller/
handler/
interceptor/
mapper/
service/
service/impl/
dto/
entity/
vo/
```

风格重点：

- `constant` 放可复用文案和固定值。
- `exception` 放明确语义的业务异常类型。
- `result` 放统一返回结构。
- `config`、`interceptor`、`handler` 单独成层，不和业务类混放。
- `service` 与 `service/impl` 成对出现。
- `mapper` 接口和 `resources/mapper/*.xml` 配套出现。

---

## 3. 命名风格

### 3.1 类命名

你偏好的命名非常稳定：

- 请求入参：`XxxDTO`
- 响应出参：`XxxVO`
- 数据库实体：`Xxx`
- 数据访问：`XxxMapper`
- 业务接口：`XxxService`
- 业务实现：`XxxServiceImpl`
- 统一返回：`Result`、`PageResult`
- 配置类：`XxxConfiguration`
- 拦截器：`XxxInterceptor`
- 异常处理器：`GlobalExceptionHandler`

这类命名的特点是：

- 一眼能看出职责
- 读代码时不需要猜“这个类到底是干什么的”

### 3.2 方法命名

你更偏好简单、传统、业务直译的方法名：

- `login`
- `save`
- `update`
- `getById`
- `getByUsername`
- `pageQuery`
- `startOrStop`
- `countByMap`

风格约束：

- 方法名优先表达业务动作
- 少用过于抽象的泛化命名
- 不为了“优雅”牺牲直观性

---

## 4. Controller 风格

你的 Controller 写法有很强的固定模式。

### 4.1 结构特征

- 类上放 `@RestController`、`@RequestMapping`
- 通常搭配 `@Slf4j`
- 依赖集中声明在类顶部
- 每个接口方法都很短
- Controller 只做接参、记录日志、调用 Service、封装返回

### 4.2 代码组织偏好

每个接口通常遵循：

1. `log.info(...)`
2. 调用 `service`
3. 如有需要在 Controller 层做简单组装
4. 返回 `Result.success(...)`

示例风格：

```java
@GetMapping("/{id}")
public Result<Employee> getById(@PathVariable Long id) {
    log.info("根据id查询员工：{}", id);
    Employee employee = employeeService.getById(id);
    return Result.success(employee);
}
```

### 4.3 约束

- Controller 不写核心业务判断
- Controller 不直接操作数据库
- Controller 方法尽量保持“读一屏就懂”

---

## 5. Service 风格

这是你风格最鲜明的部分。

### 5.1 业务流程按步骤展开

你明显偏好把业务过程拆成线性的步骤，并用注释标出来：

```java
// 1、根据用户名查询数据库中的数据
// 2、处理各种异常情况
// 3、返回实体对象
```

这种风格在后续项目中建议保留，尤其适合：

- 登录流程
- 导入流程
- 审核/状态流转
- 聚合保存流程

### 5.2 Service 负责业务，不负责花哨抽象

你偏好的 Service 有这些特点：

- 局部变量命名直接
- 先查、再判、再写
- 异常分支写清楚
- 不强行提炼很多小函数把主流程打散

推荐保持：

- 主流程完整地留在当前方法里
- 只有当某段逻辑明显可复用时再抽私有方法
- Service 接口入参优先使用业务变量，不直接把 Controller 的 DTO/请求对象传入 Service。
- 只有散参数过多、多个方法共享同一组参数，或对象本身已经是明确业务命令时，才考虑封装入参对象。
- 查询业务方法不加 `@Transactional`；新增、修改、删除、合并等写业务方法都加 `@Transactional`，即使当前只有一次数据库操作。

### 5.3 DTO 转实体风格

你常用：

- 手动 `new Entity()`
- `BeanUtils.copyProperties(...)`
- 再补业务字段

例如：

```java
Employee employee = new Employee();
BeanUtils.copyProperties(employeeDTO, employee);
employee.setStatus(StatusConstant.ENABLE);
```

这说明你喜欢：

- 对象转换显式可见
- 不引入额外 mapping 框架

---

## 6. Mapper 与 SQL 风格

你的数据访问风格不是“全注解”也不是“全 XML”，而是明确分工。

### 6.1 简单 SQL

简单单表 CRUD 优先使用 MyBatis-Plus 的通用方法，例如 `selectById`、`selectOne`、`insert`、`updateById`。

当 SQL 超过一两行、涉及多表查询、聚合统计、批量迁移或需要清晰表达业务条件时，写到 `resources/mapper/*.xml` 中，Mapper 接口只保留方法签名和 `@Param`。

### 6.2 动态 SQL 和分页

复杂查询、动态更新、分页条件，偏好写在 XML：

- `<where>`
- `<set>`
- `<if>`

这类写法是你风格里非常核心的一点，后续建议继续保留：

- Java 接口保持干净
- SQL 复杂性放回 XML

### 6.3 SQL 可读性偏好

你历史项目里的 SQL 风格特点：

- SQL 关键字小写
- 条件缩进清楚
- 一个 XML 只服务一个 Mapper
- `pageQuery`、`update` 这类方法名和 XML `id` 严格对应

---

## 7. 返回结构风格

你偏好非常朴素的统一返回结构：

- `Result<T>`
- `PageResult`

特点：

- 成功失败入口简单
- 静态工厂方法短小
- 不追求复杂泛型封装

风格重点不是“功能多”，而是“接口返回一眼能懂”。

建议后续统一保持：

- 成功：`success(...)`
- 失败：`error(...)`
- 分页：单独 `PageResult`

---

## 8. 异常处理风格

你的项目里，异常风格不是一个 `BusinessException` 走天下，而是：

- 基础父异常
- 若干语义明确的子异常
- 全局异常处理器兜底转换

例如：

- `AccountNotFoundException`
- `PasswordErrorException`
- `LoginFailedException`
- `DeletionNotAllowedException`

这说明你的审美偏向：

- 异常类型有业务语义
- 读抛异常的地方时，能直接看懂失败原因

配套风格：

- 错误文案集中放在 `MessageConstant`
- Handler 里统一转返回体

---

## 9. 常量与配置风格

你的旧项目里，常量管理非常集中：

- `MessageConstant`
- `StatusConstant`
- `PasswordConstant`
- `JwtClaimsConstant`

这类风格说明：

- 你不喜欢魔法字符串散落在业务代码中
- 文案、状态值、固定 key 倾向单独抽出来

后续建议保持：

- 业务提示文案统一进常量类
- 状态值和固定 key 不在 Service 中硬编码
- 配置参数单独放 `properties` 类

---

## 10. 日志风格

你的日志有两个很明显的偏好：

- 入口日志一定要有
- 日志文案直接写业务动作

典型格式：

```java
log.info("员工登录：{}", employeeLoginDTO);
log.info("分页查询员工：{}", employeePageQueryDTO);
log.info("【用户端】根据openid查询用户:{}", openid);
```

后续建议：

- Controller 入口保留 `info`
- Service 中关键分支和外部调用前后保留 `info`
- 日志内容优先写“业务动作 + 关键参数”

---

## 11. 注释风格

你喜欢两类注释：

### 11.1 类和方法说明注释

- 说明类职责
- 说明方法用途
- 说明参数和返回值

### 11.2 步骤式行内注释

- `1.`
- `2.`
- `3.`

这种注释风格很适合你的代码气质，建议继续保留，尤其是在：

- 登录
- 导入
- 审核
- 统计
- 状态流转

不建议为了“简洁”把这类注释全部删掉，因为它本来就是你个人代码可读性的一部分。

---

## 12. 风格关键词清单

如果后续要判断一段代码“像不像你过去的风格”，可以用下面这些关键词检查：

- 显式
- 直白
- 分层清楚
- 业务名词优先
- 步骤式展开
- 轻抽象
- Mapper + XML 分工明确
- 常量集中
- 异常语义明确
- 返回结构统一

如果一段代码出现下面这些倾向，就通常不太像你过去的写法：

- 过度函数式链式调用
- 为了抽象而抽象
- 小函数拆得过碎，主流程消失
- 大量框架技巧盖过业务语义
- 命名过泛，读不出业务对象

---

## 13. 在当前 LinkVault 项目中的落地建议

虽然当前项目技术栈、包名和返回结构与 `sky-take-out` 不完全相同，但后续若想让代码更贴近你的个人风格，建议优先往这些方向靠：

1. 业务主流程写得更线性，少一点散碎辅助函数打断阅读。
2. 常量、错误文案、状态值进一步集中管理。
3. 复杂查询逐步迁回 XML，减少把查询逻辑堆在 Java 代码里。
4. 异常语义可以更细，不必所有分支都压成一个通用异常。
5. 注释允许更“教学式”，尤其是核心业务方法。
6. Controller 层继续保持薄，Service 层承担清晰业务编排。

---

## 14. 一句话版本

你的后端代码风格，不是“框架炫技型”，而是：

> 用最直白的分层、最明确的命名、最线性的业务步骤，把接口和业务规则写清楚。
