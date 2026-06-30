# Lilishop Project Rules

本文件是当前仓库的项目级规则入口。后续所有会话进入 `D:\user_wuyou\lilishop-master\lilishop-master` 后，默认先读取并遵守本文件；如果与全局规则冲突，以更严格者为准。

## 1. 项目定位

- 这是一个 `Java 21 + Spring Boot 3.5 + Maven` 的多模块商城后端项目。
- 父工程在 `pom.xml`，当前模块包括：
  - `framework`：核心业务与公共基础设施
  - `common-api`：公共接口服务
  - `manager-api`：管理端接口
  - `seller-api`：商家端接口
  - `buyer-api`：买家端接口
  - `consumer`：消息消费、延迟任务、定时任务
  - `im-api`：IM/WebSocket 相关服务

## 2. 仓库结构与职责

- API 模块只放启动类、安全配置和控制器，不要把核心业务实现塞进 API 模块。
- 主要业务实现集中在 `framework/src/main/java/cn/lili/modules/**`。
- 公共能力位于 `framework/src/main/java/cn/lili/common`、`cache`、`mybatis`、`rocketmq`、`trigger`、`elasticsearch` 等包。
- 配置主入口在 `config/application.yml`，各服务通过各自 `application-dev.yml`、`application-test.yml`、`application-prod.yml` 覆盖。

## 3. 技术栈与基础设施

- ORM 主线是 `MyBatis-Plus`，Service 实现普遍继承 `ServiceImpl<Mapper, Entity>`。
- 数据库连接使用 MySQL，配置经 `ShardingSphere + Druid` 管理。
- 缓存使用 `Redis + Redisson`。
- 异步消息使用 `RocketMQ`。
- 检索能力使用 `Elasticsearch`。
- 定时任务与触发器涉及 `Quartz`、`xxl-job`、自定义 `trigger` 模块。
- 文档能力使用 `SpringDoc OpenAPI`，Swagger UI 路径为 `/swagger-ui.html`。

## 4. 现有代码风格观察

- Controller 普遍使用：
  - `@RestController`
  - `@Validated`
  - `@RequestMapping("/manager/...")`、`/seller/...`、`/buyer/...`、`/common/...`
  - `ResultMessage<T>` 作为统一响应包装
- 分页现状主要使用：
  - `PageVO`
  - `IPage<T>`
- Controller 命名遵循端侧后缀：
  - 管理端：`XxxManagerController`
  - 商家端：`XxxStoreController`
  - 买家端：`XxxBuyerController`
- Service 命名普遍为：
  - 接口：`XxxService`
  - 实现：`XxxServiceImpl`
- Mapper 普遍命名为 `XxxMapper`，通常继承 `BaseMapper<T>`。
- DTO / VO 现状并不完全统一，仓库内已存在：
  - `dto`
  - `vos`
  - `dos`
  - `SearchParams`
  - 直接返回 `Entity` 或 `Map`

## 5. 后续新增代码默认遵守的规则

以下规则用于“新增代码”和“较大重构”，优先级高于仓库中已有的历史松散写法：

### 5.1 分层

- 新增后端功能默认按 `Entity / BO(or DTO) / VO / Mapper / Service / Controller` 分层设计。
- Controller 不直接写业务流程，不直接拼装数据库结构，不直接写 SQL。
- 业务逻辑进入 `Service`，持久化进入 `Mapper`。

### 5.2 数据访问

- 严禁新增 `JdbcTemplate`、`DriverManager`、`Connection`、`PreparedStatement`、`ResultSet` 直连数据库代码。
- 严禁新增字符串拼接 SQL。
- 优先使用 `LambdaQueryWrapper`、`LambdaUpdateWrapper`、`Wrappers.lambdaQuery()` 等强类型条件构造。
- 复杂查询如必须进 XML，返回类型也必须是明确对象，不要新增业务层 `Map<String, Object>` 返回模型。

### 5.3 接口返回

- 继续沿用项目现有统一返回包装：`ResultMessage<T>`。
- 新接口优先返回明确 VO/DTO，不要继续扩大 `ResultMessage<Map<String, Object>>` 的使用范围。
- 分页接口优先保持与项目现有兼容：入参用 `PageVO` 或现有 `SearchParams extends PageVO`，返回 `ResultMessage<IPage<...>>`。

### 5.4 请求模型

- 新增新增/修改类接口时，优先使用独立 DTO/BO 承载请求体。
- 不要让 Controller 直接拿 `Entity` 作为复杂新增/编辑请求模型，除非该模块历史模式极强且改动非常小。
- 所有新增请求模型补充 `jakarta.validation` 校验注解。

### 5.5 命名与目录

- 新业务优先放入 `framework/src/main/java/cn/lili/modules/<domain>` 下已有领域目录。
- 若已有同域实体命名为 `dos`、视图对象命名为 `vos`、入参命名为 `dto`，优先顺着该模块现有风格，不要在同一模块里再发明平行命名。
- 控制器路径按端侧分层：
  - 管理端：`/manager/...`
  - 商家端：`/store/...` 或项目现有 `seller-api` 中的实际 `@RequestMapping`
  - 买家端：`/buyer/...`
  - 公共端：`/common/...`

### 5.6 接口传参约定

- `GET` 接口只承载查询语义，参数放 `path/query`，不使用 `requestBody` 表达写操作。
- `POST`、`PUT`、`PATCH`、`DELETE` 等写接口，多个业务字段或结构化参数默认使用 `@RequestBody DTO`；不要继续把主体业务参数散落在 query/form 中。
- 同一接口在文档中只保留一种正式传参方式；如果为了兼容历史调用保留旧入口，应在 OpenAPI 文档中隐藏旧写法，避免同时暴露 `Params + Body` 两套契约。

## 6. 对历史代码的处理原则

- 本仓库已有不少历史接口直接返回 `Entity`、`IPage<Entity>`、`Map<String, Object>`，这属于现状，不代表后续新增仍应复制。
- 对旧代码做小修时，可先保持兼容，避免无关大重构。
- 对旧模块做中大改动时，应优先朝强类型 DTO/VO、清晰分层方向收敛。
- 不要因为仓库已有历史包袱，就继续扩散更弱的写法。

## 7. 安全、事务、幂等

- 需要参数校验的 Controller 使用 `@Validated`，参数对象或字段使用 `@Valid` / `jakarta.validation` 注解。
- 跨表写入、状态流转、消息一致性相关逻辑必须放在 Service 层并评估 `@Transactional(rollbackFor = Exception.class)`。
- 涉及重复提交风险的写操作，优先复用项目已有注解，如 `@PreventDuplicateSubmissions`。
- 需要演示站限制的场景，优先复用 `@DemoSite`。

## 8. 配置与环境

- 默认配置文件位于 `config/application.yml`，包含数据库、Redis、RocketMQ、Elasticsearch、Swagger、白名单等核心配置。
- 不要在提交中硬编码新的真实生产密钥、账号、令牌。
- 修改配置时优先沿用现有 profile 结构：`application-dev.yml`、`application-test.yml`、`application-prod.yml`。

## 9. 数据与编码

- 所有中文、日文、韩文、emoji、特殊符号相关源码、SQL、JSON、测试数据，必须走 UTF-8 文件链路。
- 禁止在 PowerShell/cmd/Git Bash 中用 `echo`、here-string、管道等方式直接写入含中文 SQL/JSON 到数据库、容器或接口。
- 若修复中文乱码，先区分“显示误解码”与“数据已损坏”，未验证前不得继续批量写入。

## 10. 开发时的默认行动准则

- 开始改代码前，先看目标模块现有 Controller、Service、Entity、DTO/VO 的真实写法。
- 新功能优先复用 `framework` 中已有 Service、工具类、事件、枚举、缓存键、MQ topic/tag。
- 若发现“全局强规范”和“仓库历史实现”冲突：
  - 小改动以兼容为先
  - 新代码以更严格规范为先
  - 大改动先说明迁移边界，再实施

## 11. 参考文档

- 更完整的项目规范摘要见 `docs/project-spec.md`。
