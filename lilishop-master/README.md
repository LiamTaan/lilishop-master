# Lilishop Wholesale Backend

`Lilishop + 批发商城补丁` 的本地开发仓库。

当前仓库基于 `Java 21 + Spring Boot 3.5.6 + Maven`，包含商城基础能力与本次批发商城后端治理能力。

## 模块说明

- `framework`：核心业务与公共基础设施
- `manager-api`：管理端接口
- `seller-api`：供货商/店铺端接口
- `buyer-api`：买家/代理商/消费者端接口
- `common-api`：公共接口
- `consumer`：消息消费、定时任务、异步任务
- `im-api`：IM / WebSocket 相关接口

## 运行环境

- JDK：`21`
- Maven：`3.9+`
- MySQL：`8.x`
- Redis：`6.x` 或以上
- Elasticsearch：`7.17.19`
- RocketMQ：`4.9.6`
- XXL-Job Admin：`2.3.0`

## 数据库初始化

空库初始化时，建议按顺序执行下面四个文件：

1. [DB_INIT/01_full_base_schema.sql](D:/user_wuyou/lilishop-master/lilishop-master/DB_INIT/01_full_base_schema.sql)
2. [DB_INIT/02_startup_and_wholesale_patch.sql](D:/user_wuyou/lilishop-master/lilishop-master/DB_INIT/02_startup_and_wholesale_patch.sql)
3. [DB_INIT/03_procurement_schema_patch.sql](D:/user_wuyou/lilishop-master/lilishop-master/DB_INIT/03_procurement_schema_patch.sql)
4. [DB_INIT/04_wholesale_manager_menu_alignment.sql](D:/user_wuyou/lilishop-master/lilishop-master/DB_INIT/04_wholesale_manager_menu_alignment.sql)

说明：

- `01_full_base_schema.sql`：基础库建表与初始化数据
- `02_startup_and_wholesale_patch.sql`：官方启动补丁 + 本项目批发商城补丁 + 推荐索引
- `03_procurement_schema_patch.sql`：采购单、采购入库等采购域补表
- `04_wholesale_manager_menu_alignment.sql`：批发管理端菜单基线、权限回填、旧平台菜单清理、平台角色基线
- 执行 `04_wholesale_manager_menu_alignment.sql` 时，请从仓库根目录发起，以便其中的 `SOURCE DB/...` 路径正常解析

已初始化过的旧库如出现缺表，按缺失模块补执行：

- [DB/patch_missing_im_seat_tables.sql](D:/user_wuyou/lilishop-master/lilishop-master/DB/patch_missing_im_seat_tables.sql)
- [DB/patch_missing_procurement_inventory_damage_tables.sql](D:/user_wuyou/lilishop-master/lilishop-master/DB/patch_missing_procurement_inventory_damage_tables.sql)

## Dev 环境说明

当前 `application-dev.yml` 已统一按本机环境配置，默认值如下：

- MySQL：`127.0.0.1:3306`
- 数据库：`lilishop`
- 用户名：`root`
- 密码：`XXX`
- Redis：`127.0.0.1:6379`
- Redis 密码：空
- Elasticsearch：`127.0.0.1:9200`
- RocketMQ NameServer：`127.0.0.1:19876`
- XXL-Job Admin：`http://127.0.0.1:9001/xxl-job-admin`

如需覆盖，可通过环境变量覆盖：

- `MYSQL_HOST`
- `MYSQL_PORT`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `ES_NODES`
- `ROCKETMQ_NAME_SERVER`
- `XXL_JOB_ADMIN`

## Docker 中间件

项目本身不通过 Docker 启动，Java 服务建议本地 IDEA / Maven 启动。

本地开发所需的补充中间件编排放在外部目录：

- [D:/utils/DockerDesktopWSL/rural-helper/lilishop/docker-compose.yml](D:/utils/DockerDesktopWSL/rural-helper/lilishop/docker-compose.yml)

这套编排用于启动：

- `lilishop-elasticsearch`
- `lilishop-kibana`
- `lilishop-rocketmq-namesrv`
- `lilishop-rocketmq-broker`
- `lilishop-rocketmq-dashboard`
- `lilishop-xxl-job`

启动命令：

```powershell
cd D:\utils\DockerDesktopWSL\rural-helper\lilishop
docker compose up -d
```

如果你本机已有旧的 `rural-helper` RocketMQ 容器，建议先删除：

```powershell
docker rm -f rural-helper-rocketmq-broker
docker rm -f rural-helper-rocketmq-namesrv
docker rm -f rural-helper-rocketmq-dashboard
```

## 编译

```powershell
$env:JAVA_HOME='D:\utils\jdk21'
$env:Path='D:\utils\jdk21\bin;' + $env:Path
mvn -pl framework,manager-api -am -DskipTests compile
```

如需按“无 app 界面、接口模拟 app”的方式做全量测试，建议至少编译：

```powershell
$env:JAVA_HOME='D:\utils\jdk21'
$env:Path='D:\utils\jdk21\bin;' + $env:Path
mvn -pl framework,manager-api,buyer-api,seller-api,common-api,im-api -am -DskipTests compile
```

## 启动方式

### 启动管理端

```powershell
mvn -pl manager-api -am spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 启动买家端

```powershell
mvn -pl buyer-api -am spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 启动商家端

```powershell
mvn -pl seller-api -am spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 启动公共服务

```powershell
mvn -pl common-api -am spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 启动消费者

```powershell
mvn -pl consumer -am spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 启动 IM

```powershell
mvn -pl im-api -am spring-boot:run "-Dspring-boot.run.profiles=dev"
```

## 默认端口

- `buyer-api`：`8888`
- `manager-api`：`8887`
- `seller-api`：`8889`
- `common-api`：`8890`
- `im-api`：`8885`
- `consumer`：`8886`

## Swagger

启动后可访问：

- 管理端：`http://127.0.0.1:8887/swagger-ui/index.html`
- 买家端：`http://127.0.0.1:8888/swagger-ui/index.html`
- 商家端：`http://127.0.0.1:8889/swagger-ui.html`
- 公共服务：`http://127.0.0.1:8890/swagger-ui/index.html`
- IM 服务：`http://127.0.0.1:8885/swagger-ui/index.html`

说明：

- 管理后台测试主要使用 `manager-api`
- 无 app 界面时，买家/代理商/消费者相关流程通过 `buyer-api + common-api` 接口模拟
- 商家履约、发货、拼团、营销等流程通过 `seller-api` 接口模拟
- IM 会话场景再补测 `im-api`

## 交付文档

交付、联调、验收相关文档见：

- [docs/批发商城后端客户交付总说明.md](D:/user_wuyou/lilishop-master/lilishop-master/docs/批发商城后端客户交付总说明.md)
- [docs/批发商城交付目录索引.md](D:/user_wuyou/lilishop-master/lilishop-master/docs/批发商城交付目录索引.md)
