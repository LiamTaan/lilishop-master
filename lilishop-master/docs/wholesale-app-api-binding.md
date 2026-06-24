# APP 端联调绑定约定

## 消费者 APP

- `时令上新` 不新增独立 buyer-api 接口。
- 前端优先从 `GET /buyer/other/pageData/getIndex` 的首页装修楼层里按楼层标识承接。
- 如需独立承接专题入口，复用：
  - `GET /buyer/other/pageData/getSpecial`
  - `GET /buyer/other/pageData/get/{id}`

建议约定：

- `时令上新` 作为首页楼层标识或专题标识，前端按装修返回的 `name` / `title` / `pageClientType` 做识别。
- 如果后续运营需要单独跳转页，优先走专题页，不新增平行 Controller。

## 精品好团

- `精品好团` 同样不新增独立 buyer-api 接口。
- 统一复用现有拼团与专题装修数据：
  - 首页楼层：`GET /buyer/other/pageData/getIndex`
  - 专题页：`GET /buyer/other/pageData/getSpecial`
  - 专题详情：`GET /buyer/other/pageData/get/{id}`

建议约定：

- `精品好团` 作为拼团专题楼层或专题页标识承接。
- 前端通过楼层标签或专题名称区分展示，不在 buyer-api 新建 “精品好团” 专属接口。

## 快捷入口

- 首页快捷入口继续复用 `GET /buyer/other/shortcutNav`。
- 若 `时令上新` / `精品好团` 需要首页快捷宫格入口，优先通过快捷导航配置补齐，不新增后端接口。

## 供货商卡券管理

- 不新增独立卡券域接口，继续复用 `GET /store/promotion/coupon` 与 `GET /store/promotion/coupon/{couponId}`。
- `CouponVO` 已补充适合移动端直出的字段，前端优先使用：
  - `couponTypeLabel`：卡券类型文案
  - `faceValueText`：统一面值文案
  - `stockNum`：剩余库存
  - `unlimitedStock`：是否不限量
  - `stockText`：库存展示文案
  - `promotionStatusLabel`：状态文案

建议约定：

- 卡券类型：优先显示 `couponTypeLabel`，保留 `couponType` 作为状态码。
- 面值：优先显示 `faceValueText`，不再由前端根据 `price / couponDiscount` 自行拼装。
- 库存：优先显示 `stockText`；如需数值判断，使用 `stockNum + unlimitedStock`。
- 状态：优先显示 `promotionStatusLabel`，保留 `promotionStatus` 作为状态码。
