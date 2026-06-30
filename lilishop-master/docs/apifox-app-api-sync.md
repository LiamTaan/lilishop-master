# Apifox App 与商家端 API 同步说明

## 目标

- 管理端 `manager-api` 不再维护 Swagger 注解和 SpringDoc 配置。
- App 与商家端接口统一通过 Apifox 管理，不再开放 Swagger UI 页面。
- 当前已落地的联调模块覆盖：
  - `buyer-api`
  - `common-api`
  - `im-api`
  - `seller-api`

## OpenAPI 导入地址

以下模块已调整为“开启 `api-docs`，关闭 `swagger-ui`”：

| 模块 | 本地端口 | OpenAPI 地址 | 说明 |
| --- | --- | --- | --- |
| `buyer-api` | `8888` | `http://127.0.0.1:8888/v3/api-docs` | 买家端主业务接口 |
| `common-api` | `8890` | `http://127.0.0.1:8890/v3/api-docs` | 登录、短信、上传、站点等公共接口 |
| `im-api` | `8885` | `http://127.0.0.1:8885/v3/api-docs` | IM 与部分店铺聊天相关接口 |
| `seller-api` | `8889` | `http://127.0.0.1:8889/v3/api-docs` | 商家端 / 供货商端业务接口 |

说明：

- `swagger-ui` 页面保持关闭，不再作为联调入口。
- `/v3/api-docs` 已加入各模块白名单，允许 Apifox 直接拉取定义。
- Apifox 中请使用“导入 OpenAPI/Swagger”或“URL 导入”方式同步以上地址。
- 如果联调环境不是本机，请把 `127.0.0.1` 替换为对应服务域名或网关地址。

## 推荐同步方式

1. 在 Apifox 中创建 `Lilishop App & Store` 项目。
2. 分别导入 `buyer-api`、`common-api`、`im-api`、`seller-api` 的 OpenAPI URL。
3. 按模块建立目录：
   - `01-common`
   - `02-buyer`
   - `03-im`
   - `04-seller`
4. 每次后端接口有新增或字段变化时，优先更新注解/模型后重新导入 Apifox，不再补 Swagger 页面说明。

## 联调补充资料

仓库内已有一份面向前端联调的 App 文档脚本：

- 脚本：[build_app_api_doc.py](D:\user_wuyou\lilishop-master\lilishop-master\docs_generated\build_app_api_doc.py)
- 输出文档：[批发商城APP联调接口文档.docx](D:\user_wuyou\lilishop-master\lilishop-master\docs\批发商城APP联调接口文档.docx)

该文档适合补充页面级说明、字段语义和联调注意事项；Apifox 作为接口定义和调试入口，两者配合使用。
