package cn.lili.modules.store.service;

import cn.lili.common.vo.PageVO;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.member.entity.dto.CollectionDTO;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.dto.*;
import cn.lili.modules.store.entity.vos.StoreAuditSummaryVO;
import cn.lili.modules.store.entity.vos.StoreSearchParams;
import cn.lili.modules.store.entity.vos.StoreVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 店铺业务层
 *
 * @author pikachu
 * @since 2020/11/18 11:45 上午
 */
public interface StoreService extends IService<Store> {

    /**
     * 分页条件查询
     * 用于展示店铺列表
     *
     * @param entity
     * @return
     */
    IPage<StoreVO> findByConditionPage(StoreSearchParams entity, PageVO page);

    /**
     * 获取当前登录店铺信息
     *
     * @return 店铺信息DO
     */
    StoreVO getStoreDetail();

    /**
     * 增加店铺
     * 用于后台添加店铺
     *
     * @param adminStoreApplyDTO 后台添加店铺信息
     * @return 店铺
     */
    Store add(StoreAdminSaveDTO storeAdminSaveDTO);

    /**
     * 编辑店铺
     *
     * @param storeEditDTO 店铺修改信息
     * @return 店铺
     */
    Store edit(StoreEditDTO storeEditDTO);

    /**
     * 审核店铺申请
     *
     * @param id 店铺ID
     * @param auditDTO 审核信息
     * @return 操作结果
     */
    boolean audit(String id, StoreAuditDTO auditDTO);

    /**
     * 审核店铺
     *
     * @param id     店铺ID
     * @param passed 审核结果
     * @return 操作结果
     */
    boolean audit(String id, Integer passed);

    /**
     * 关闭店铺
     *
     * @param id 店铺ID
     * @return 店铺
     */
    boolean disable(String id);

    /**
     * 开启店铺
     *
     * @param id 店铺ID
     * @return 操作状态
     */
    boolean enable(String id);

    /**
     * 申请店铺第一步
     * 设置店铺公司信息，如果没有店铺新建店铺
     *
     * @param storeCompanyDTO 店铺公司信息
     * @return 店铺
     */
    boolean selectApplyType(StoreApplyTypeSelectDTO applyTypeSelectDTO, String loginSessionToken);

    /**
     * 选择供货商/批发商入驻主体类型
     *
     * @param applyTypeSelectDTO 入驻主体类型
     * @return 操作结果
     */
    boolean selectSupplierApplyType(StoreApplyTypeSelectDTO applyTypeSelectDTO, String loginSessionToken);

    /**
     * 申请店铺第二步
     *
     * @param storeBankDTO 店铺银行信息
     * @return 店铺
     */
    boolean applyPersonal(StorePersonalApplyDTO personalApplyDTO, String uuid, String loginSessionToken);

    /**
     * 个人主体提交供货商/批发商入驻资料
     *
     * @param personalApplyDTO 个人入驻资料
     * @param uuid             验证码会话标识
     * @return 操作结果
     */
    boolean applySupplierPersonal(StorePersonalApplyDTO personalApplyDTO, String uuid, String loginSessionToken);

    /**
     * 申请店铺第三步
     * 设置店铺信息，经营范围
     *
     * @param storeOtherInfoDTO 店铺其他信息
     * @return 店铺
     */
    boolean applyIndividual(StoreIndividualApplyDTO individualApplyDTO, String uuid, String loginSessionToken);

    /**
     * 个体户提交供货商/批发商入驻资料
     *
     * @param individualApplyDTO 个体户入驻资料
     * @param uuid               验证码会话标识
     * @return 操作结果
     */
    boolean applySupplierIndividual(StoreIndividualApplyDTO individualApplyDTO, String uuid, String loginSessionToken);

    /**
     * 企业法人提交入驻资料
     *
     * @param companyLegalApplyDTO 企业法人入驻资料
     * @param uuid                 验证码会话标识
     * @return 操作结果
     */
    boolean applyCompanyLegal(StoreCompanyLegalApplyDTO companyLegalApplyDTO, String uuid, String loginSessionToken);

    /**
     * 企业法人提交供货商/批发商入驻资料
     *
     * @param companyLegalApplyDTO 企业法人入驻资料
     * @param uuid                 验证码会话标识
     * @return 操作结果
     */
    boolean applySupplierCompanyLegal(StoreCompanyLegalApplyDTO companyLegalApplyDTO, String uuid, String loginSessionToken);

    /**
     * 企业被授权人提交入驻资料
     *
     * @param companyAuthorizedApplyDTO 企业被授权人入驻资料
     * @param uuid                      验证码会话标识
     * @return 操作结果
     */
    boolean applyCompanyAuthorized(StoreCompanyAuthorizedApplyDTO companyAuthorizedApplyDTO, String uuid, String loginSessionToken);

    /**
     * 企业被授权人提交供货商/批发商入驻资料
     *
     * @param companyAuthorizedApplyDTO 企业被授权人入驻资料
     * @param uuid                      验证码会话标识
     * @return 操作结果
     */
    boolean applySupplierCompanyAuthorized(StoreCompanyAuthorizedApplyDTO companyAuthorizedApplyDTO, String uuid, String loginSessionToken);


    /**
     * 更新店铺商品数量
     *
     * @param storeId 店铺ID
     * @param num     商品数量
     */
    void updateStoreGoodsNum(String storeId, Long num);

    /**
     * 更新店铺收藏数量
     *
     * @param collectionDTO 收藏信息
     */
    void updateStoreCollectionNum(CollectionDTO collectionDTO);

    /**
     * 重新生成所有店铺
     */
    void storeToClerk();

    /**
     * 店铺获取该客户的访问记录
     * @param memberId 客户Id
     * @return
     */
    List<GoodsSku> getToMemberHistory(String memberId);

    /**
     * 获取启用中的店铺列表
     *
     * @return 店铺列表（store_disable=OPEN）
     */
    List<Store> listOpenStores();

    /**
     * 根据客户ID获取店铺信息
     *
     * @param memberId 客户ID
     * @return 店铺信息（若不存在返回 null）
     */
    Store getStoreByMemberId(String memberId);

    /**
     * 店铺治理汇总
     *
     * @return 汇总结果
     */
    StoreAuditSummaryVO managementSummary();

    /**
     * 发送店铺治理通知
     *
     * @param store       店铺
     * @param title       标题
     * @param content     内容
     * @param platformMsg 是否平台公告
     */
    void sendGovernanceMessage(Store store, String title, String content, boolean platformMsg);
}
