import homeRouter from "./modules/home";
import dashboardWholesaleRouter from "./modules/dashboard-wholesale";
import storeGovernanceRouter from "./modules/store-governance";
import goodsGovernanceRouter from "./modules/goods-governance";
import orderGovernanceRouter from "./modules/order-governance";
import marketingGovernanceRouter from "./modules/marketing-governance";
import fundGovernanceRouter from "./modules/fund-governance";
import procurementGovernanceRouter from "./modules/procurement-governance";
import platformConfigRouter from "./modules/platform-config";
import memberCenterRouter from "./modules/member-center";
import messageCenterRouter from "./modules/message-center";
import contentCenterRouter from "./modules/content-center";
import supportCenterRouter from "./modules/support-center";
import merchantGrantRouter from "./modules/merchant-grant";
import systemCenterRouter from "./modules/system-center";

/** 当前批发商城管理端的业务菜单骨架，仅保留正式交付后台主链路 */
export const businessRouteModules = [
  homeRouter,
  dashboardWholesaleRouter,
  storeGovernanceRouter,
  goodsGovernanceRouter,
  orderGovernanceRouter,
  marketingGovernanceRouter,
  fundGovernanceRouter,
  procurementGovernanceRouter,
  platformConfigRouter,
  memberCenterRouter,
  messageCenterRouter,
  contentCenterRouter,
  supportCenterRouter,
  merchantGrantRouter,
  systemCenterRouter
];
