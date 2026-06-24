# Lilishop Project Spec

## 目的

本文件记录当前仓库的项目规范摘要、证据来源和后续开发默认约束。它不是脱离仓库的理想规范，而是“基于现有代码观察得到的现状规则 + 后续新增代码应遵守的更严格规则”。

## 1. 技术栈与运行时

- `Java 21`
- `Spring Boot 3.5.6`
- `Maven` 多模块工程
- `MyBatis-Plus 3.5.8`
- `MySQL + ShardingSphere + Druid`
- `Redis + Redisson`
- `RocketMQ`
- `Elasticsearch`
- `Quartz`
- `Spring Security`
- `SpringDoc OpenAPI`

证据：
- `pom.xml`
- `framework/pom.xml`
- `config/application.yml`

## 2. 模块结构

- `framework`
  - 核心业务实现、实体、服务、mapper、公共能力
- `common-api`
  - 公共接口服务
- `manager-api`
  - 管理端接口服务
- `seller-api`
  - 商家端接口服务
- `buyer-api`
  - 买家端接口服务
- `consumer`
  - 消息监听、事件处理、定时任务、延迟队列
- `im-api`
  - IM/WebSocket 服务

证据：
- `pom.xml` 中的 `<modules>`
- 各模块启动类与目录结构

## 3. 启动与服务入口

主要启动类：
- `manager-api/src/main/java/cn/lili/ManagerApiApplication.java`
- `seller-api/src/main/java/cn/lili/StoreApiApplication.java`
- `buyer-api/src/main/java/cn/lili/BuyerApiApplication.java`
- `common-api/src/main/java/cn/lili/CommonApiApplication.java`
- `consumer/src/main/java/cn/lili/ConsumerApplication.java`
- `im-api/src/main/java/cn/lili/ImApiApplication.java`

观察：
- `manager-api`、`seller-api`、`buyer-api` 启用了 `@EnableAsync`
- 三者都显式设置了 RocketMQ 日志与 ES Netty 属性

## 4. 配置规则

- 全局配置主文件是 `config/application.yml`
- 各模块都有自己的：
  - `application.yml`
  - `application-dev.yml`
  - `application-test.yml`
  - `application-prod.yml`
- Swagger UI 路径为 `/swagger-ui.html`
- 白名单 URL、Redis、数据库、ES、RocketMQ、xxl-job 等都在主配置里有统一入口

开发约束：
- 新增配置优先进入已有 profile 文件，不要额外平铺一套配置体系
- 不提交真实生产密钥

## 5. 目录与分层观察

### 5.1 API 模块

API 模块主要承载：
- 启动类
- 安全配置
- Controller

证据：
- `manager-api/src/main/java/cn/lili/controller/**`
- `seller-api/src/main/java/cn/lili/controller/**`
- `buyer-api/src/main/java/cn/lili/controller/**`
- `common-api/src/main/java/cn/lili/controller/**`

### 5.2 framework 模块

`framework` 承载：
- `common`
- `cache`
- `mybatis`
- `rocketmq`
- `trigger`
- `elasticsearch`
- `modules/**` 领域服务

这是仓库真正的业务核心层。

## 6. 接口风格与命名

### 6.1 Controller 命名

- 管理端：`XxxManagerController`
- 商家端：`XxxStoreController`
- 买家端：`XxxBuyerController`
- 公共接口：按具体职责命名，如 `UploadController`

### 6.2 路径风格

- 管理端前缀通常是 `/manager/...`
- 买家端前缀通常是 `/buyer/...`
- 商家端常见前缀为 `/store/...`，但以实际 `@RequestMapping` 为准
- 公共接口通常为 `/common/...`

### 6.3 返回结构

统一响应包装是：
- `cn.lili.common.vo.ResultMessage<T>`

证据：
- `framework/src/main/java/cn/lili/common/vo/ResultMessage.java`
- 大量 Controller 返回 `ResultMessage<T>`

### 6.4 分页风格

现状主流是：
- 入参使用 `PageVO` 或 `SearchParams extends PageVO`
- 返回 `ResultMessage<IPage<T>>`

证据：
- `framework/src/main/java/cn/lili/common/vo/PageVO.java`
- 多个 Controller 的分页接口签名

## 7. 数据访问与服务层观察

- Service 实现普遍继承 `ServiceImpl<Mapper, Entity>`
- Mapper 普遍继承 `BaseMapper<T>`
- 查询条件大量使用 `LambdaQueryWrapper`、`QueryWrapper`、`Wrappers`

证据：
- `framework/src/main/java/cn/lili/modules/**/serviceimpl/*.java`
- `framework/src/main/java/cn/lili/modules/**/mapper/*.java`

推断：
- 新增业务应优先沿用 MyBatis-Plus 现有风格，而不是引入 JPA、裸 JDBC 或新的 Repository 风格

## 8. 当前仓库与理想强规范的差异

这部分非常重要。

仓库现状并不完全符合“强类型分层”：
- 存在 `ResultMessage<Map<String, Object>>`
- 存在返回 `Entity`、`IPage<Entity>` 的 Controller
- 存在 Service / Mapper 层返回 `List<Map<String, Object>>`
- 入参模型既有 `DTO`、`VO`、`SearchParams`，也有直接使用实体的情况

证据：
- `buyer-api/src/main/java/cn/lili/controller/goods/GoodsBuyerController.java`
- `seller-api/src/main/java/cn/lili/controller/goods/GoodsStoreController.java`
- `manager-api/src/main/java/cn/lili/controller/goods/GoodsManagerController.java`
- `framework/src/main/java/cn/lili/modules/member/mapper/MemberEvaluationMapper.java`
- `framework/src/main/java/cn/lili/modules/goods/service/GoodsSkuService.java`

结论：
- 不要误判为“这个仓库已经全面采用 Entity / BO / VO / Mapper / Service / Controller 严格分层”
- 正确做法是：承认历史包袱，同时让后续新增代码默认按更严格规范前进

## 9. 后续新增代码必须遵守的规则

### 9.1 分层规则

- 新增功能默认采用：
  - Entity
  - DTO/BO
  - VO
  - Mapper
  - Service
  - Controller
- Controller 不写核心业务流程
- 不在 Controller 中拼装数据库结构

### 9.2 数据访问规则

- 禁止新增裸 JDBC：
  - `JdbcTemplate`
  - `DriverManager`
  - `Connection`
  - `PreparedStatement`
  - `ResultSet`
- 禁止新增字符串拼接 SQL
- 优先使用 `LambdaQueryWrapper`、`LambdaUpdateWrapper`
- 复杂 SQL 如必须进 XML，返回类型也应是强类型对象

### 9.3 接口模型规则

- 保持统一返回包装 `ResultMessage<T>`
- 新接口优先返回明确 VO/DTO
- 不再新增业务型 `Map<String, Object>` 响应，除非只是外围工具、三方透传或极小兼容改动
- 新增新增/修改请求优先使用 DTO/BO，不直接暴露 Entity

### 9.4 分页规则

- 为兼容仓库现状，新分页接口优先使用：
  - `PageVO`
  - 或 `SearchParams extends PageVO`
  - 返回 `ResultMessage<IPage<VO/DTO>>`

### 9.5 校验与事务

- Controller 使用 `@Validated`
- 请求对象与关键参数补充 `jakarta.validation`
- 跨表修改、状态流转、消息一致性相关逻辑必须在 Service 层评估 `@Transactional(rollbackFor = Exception.class)`

### 9.6 复用优先

- 优先复用已有：
  - Service
  - 枚举
  - 工具类
  - 缓存 key
  - 事件模型
  - RocketMQ topic/tag

## 10. 禁止事项

- 不要在 API 模块新建大量业务实现，业务应进入 `framework`
- 不要新增新的持久化框架或平行分层体系
- 不要因为历史代码中已有 `Map` 返回，就继续把 `Map` 当作默认业务模型
- 不要在 shell 中用内联中文 SQL/JSON 直接写数据库或调接口
- 不要把乱码修复建立在“显示猜测”上，必须验证 UTF-8 实际链路

## 11. 默认决策原则

遇到“现状代码”和“更严格规范”冲突时：
- 小修小补：优先兼容原实现
- 新增功能：优先按更严格规范实现
- 大范围改造：先明确迁移边界，再动手

## 12. 证据摘要

核心证据文件：
- `pom.xml`
- `framework/pom.xml`
- `config/application.yml`
- `framework/src/main/java/cn/lili/common/vo/ResultMessage.java`
- `framework/src/main/java/cn/lili/common/vo/PageVO.java`
- `manager-api/src/main/java/cn/lili/controller/goods/GoodsManagerController.java`
- `framework/src/main/java/cn/lili/modules/goods/serviceimpl/GoodsServiceImpl.java`
- `buyer-api/src/main/java/cn/lili/BuyerApiApplication.java`
- `seller-api/src/main/java/cn/lili/StoreApiApplication.java`
- `manager-api/src/main/java/cn/lili/ManagerApiApplication.java`

## 13. 与 AGENTS.md 的关系

- `AGENTS.md` 用于后续会话自动读取和执行
- 本文档用于保留更完整的背景、证据和判断过程
