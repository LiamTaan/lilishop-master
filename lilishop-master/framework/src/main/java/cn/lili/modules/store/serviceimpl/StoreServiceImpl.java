package cn.lili.modules.store.serviceimpl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.properties.RocketmqCustomProperties;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.utils.BeanUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.agent.entity.dto.AgentRoleCreateDTO;
import cn.lili.modules.agent.entity.enums.AgentLevelEnum;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.goods.service.GoodsSkuService;
import cn.lili.modules.member.entity.dos.Clerk;
import cn.lili.modules.member.entity.dos.FootPrint;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.entity.dto.ClerkAddDTO;
import cn.lili.modules.member.entity.dto.CollectionDTO;
import cn.lili.modules.member.service.ClerkService;
import cn.lili.modules.member.service.FootprintService;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.dos.StoreAuditLog;
import cn.lili.modules.store.entity.dos.StoreDetail;
import cn.lili.modules.store.entity.dto.*;
import cn.lili.modules.store.entity.enums.StoreAuditStatusEnum;
import cn.lili.modules.store.entity.enums.StoreApplyTypeEnum;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.entity.enums.StoreStatusEnum;
import cn.lili.modules.store.entity.vos.StoreAuditSummaryVO;
import cn.lili.modules.store.entity.vos.StoreSearchParams;
import cn.lili.modules.store.entity.vos.StoreVO;
import cn.lili.modules.store.mapper.StoreMapper;
import cn.lili.modules.store.service.StoreDetailService;
import cn.lili.modules.store.service.StoreAuditLogService;
import cn.lili.modules.store.service.StoreService;
import cn.lili.mybatis.util.PageUtil;
import cn.lili.rocketmq.RocketmqSendCallbackBuilder;
import cn.lili.rocketmq.tags.StoreTagsEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 店铺业务层实现
 *
 * @author pikachu
 * @since 2020-03-07 16:18:56
 */
@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {

    /**
     * 客户
     */
    @Autowired
    @Lazy
    private MemberService memberService;

    /**
     * 店员
     */
    @Autowired
    private ClerkService clerkService;
    /**
     * 商品
     */
    @Autowired
    @Lazy
    private GoodsService goodsService;

    @Autowired
    @Lazy
    private GoodsSkuService goodsSkuService;
    /**
     * 店铺详情
     */
    @Autowired
    @Lazy
    private StoreDetailService storeDetailService;

    @Autowired
    private StoreAuditLogService storeAuditLogService;

    @Autowired
    private RocketmqCustomProperties rocketmqCustomProperties;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    @Lazy
    private FootprintService footprintService;

    @Autowired
    private Cache cache;

    @Autowired
    @Lazy
    private AgentRoleRelationService agentRoleRelationService;

    @Override
    public IPage<StoreVO> findByConditionPage(StoreSearchParams storeSearchParams, PageVO page) {
        return this.baseMapper.getStoreList(PageUtil.initPage(page), storeSearchParams.queryWrapper("s"));
    }

    @Override
    public StoreVO getStoreDetail() {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        StoreVO storeVO = this.baseMapper.getStoreDetail(currentUser.getStoreId());
        storeVO.setNickName(currentUser.getNickName());
        return storeVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Store add(AdminStoreApplyDTO adminStoreApplyDTO) {

        //判断店铺名称是否存在
        QueryWrapper<Store> queryWrapper = Wrappers.query();
        queryWrapper.eq("store_name", adminStoreApplyDTO.getStoreName());
        if (this.getOne(queryWrapper) != null) {
            throw new ServiceException(ResultCode.STORE_NAME_EXIST_ERROR);
        }

        Member member = memberService.getById(adminStoreApplyDTO.getMemberId());
        //判断用户是否存在
        if (member == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        //判断是否拥有店铺
        if (Boolean.TRUE.equals(member.getHaveStore())) {
            throw new ServiceException(ResultCode.STORE_APPLY_DOUBLE_ERROR);
        }

        //添加店铺
        validateApplyType(adminStoreApplyDTO);
        Store store = new Store(member, adminStoreApplyDTO);
        this.save(store);

        //判断是否存在店铺详情，如果没有则进行新建，如果存在则进行修改
        StoreDetail storeDetail = new StoreDetail(store, adminStoreApplyDTO);
        applyBusinessIdentity(
                store,
                storeDetail,
                adminStoreApplyDTO.getApplyType(),
                adminStoreApplyDTO.getStoreType(),
                adminStoreApplyDTO.getAgentLevel(),
                adminStoreApplyDTO.getAgentRegionId(),
                adminStoreApplyDTO.getAgentRegionName()
        );
        this.updateById(store);

        storeDetailService.save(storeDetail);

        //设置客户-店铺信息
        memberService.update(new LambdaUpdateWrapper<Member>()
                .eq(Member::getId, member.getId())
                .set(Member::getHaveStore, true)
                .set(Member::getStoreId, store.getId()));
        return store;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Store edit(StoreEditDTO storeEditDTO) {
        if (storeEditDTO != null) {
            //判断店铺名是否唯一
            if (CharSequenceUtil.isNotBlank(storeEditDTO.getStoreName())) {
                Store storeTmp = getOne(new QueryWrapper<Store>().eq("store_name", storeEditDTO.getStoreName()));
                if (storeTmp != null && !CharSequenceUtil.equals(storeTmp.getId(), storeEditDTO.getStoreId())) {
                    throw new ServiceException(ResultCode.STORE_NAME_EXIST_ERROR);
                }
            }
            validateApplyType(storeEditDTO);
            Store store = this.getById(storeEditDTO.getStoreId());
            if (store != null && StoreAuditStatusEnum.REJECTED.name().equals(store.getAuditStatus())) {
                storeEditDTO.setAuditStatus(StoreAuditStatusEnum.SUBMITTED.name());
            }
            if (store != null && StoreAuditStatusEnum.DRAFT.name().equals(store.getAuditStatus())) {
                storeEditDTO.setAuditStatus(StoreAuditStatusEnum.SUBMITTED.name());
            }
            if (CharSequenceUtil.isBlank(storeEditDTO.getStoreDisable())) {
                storeEditDTO.setStoreDisable(StoreStatusEnum.APPLYING.name());
            }
            String effectiveApplyType = CharSequenceUtil.isNotBlank(storeEditDTO.getApplyType()) ? storeEditDTO.getApplyType() : store.getApplyType();
            String effectiveBizType = CharSequenceUtil.isNotBlank(storeEditDTO.getStoreType()) ? storeEditDTO.getStoreType() : store.getStoreType();
            String effectiveAgentLevel = CharSequenceUtil.isNotBlank(storeEditDTO.getAgentLevel()) ? storeEditDTO.getAgentLevel() : store.getAgentLevel();
            String effectiveRegionId = CharSequenceUtil.isNotBlank(storeEditDTO.getAgentRegionId()) ? storeEditDTO.getAgentRegionId() : store.getAgentRegionId();
            String effectiveRegionName = CharSequenceUtil.isNotBlank(storeEditDTO.getAgentRegionName()) ? storeEditDTO.getAgentRegionName() : store.getAgentRegionName();
            storeEditDTO.setApplyType(effectiveApplyType);
            storeEditDTO.setStoreType(normalizeBizType(effectiveBizType));
            storeEditDTO.setAgentLevel(StoreBizTypeEnum.AGENT.name().equalsIgnoreCase(storeEditDTO.getStoreType()) ? normalizeAgentLevel(effectiveAgentLevel) : null);
            storeEditDTO.setAgentRegionId(StoreBizTypeEnum.AGENT.name().equalsIgnoreCase(storeEditDTO.getStoreType()) ? effectiveRegionId : null);
            storeEditDTO.setAgentRegionName(StoreBizTypeEnum.AGENT.name().equalsIgnoreCase(storeEditDTO.getStoreType()) ? effectiveRegionName : null);
            StoreDetail storeDetail = storeDetailService.getStoreDetail(storeEditDTO.getStoreId());
            applyBusinessIdentity(
                    store,
                    storeDetail,
                    effectiveApplyType,
                    effectiveBizType,
                    effectiveAgentLevel,
                    effectiveRegionId,
                    effectiveRegionName
            );
            //修改店铺详细信息
            updateStoreDetail(storeEditDTO);
            //修改店铺信息
            return updateStore(storeEditDTO);
        } else {
            throw new ServiceException(ResultCode.STORE_NOT_EXIST);
        }
    }

    /**
     * 修改店铺基本信息
     *
     * @param storeEditDTO 修改店铺信息
     */
    private Store updateStore(StoreEditDTO storeEditDTO) {
        Store store = this.getById(storeEditDTO.getStoreId());
        if (store != null) {
            BeanUtil.copyProperties(storeEditDTO, store);
            store.setId(storeEditDTO.getStoreId());
            boolean result = this.updateById(store);
            if (result) {
                storeDetailService.updateStoreGoodsInfo(store);
            }
            String destination = rocketmqCustomProperties.getStoreTopic() + ":" + StoreTagsEnum.EDIT_STORE_SETTING.name();
            //发送订单变更mq消息
            rocketMQTemplate.asyncSend(destination, store, RocketmqSendCallbackBuilder.commonCallback());
        }

        cache.remove(CachePrefix.STORE.getPrefix() + storeEditDTO.getStoreId());
        return store;
    }

    /**
     * 修改店铺详细信息
     *
     * @param storeEditDTO 修改店铺信息
     */
    private void updateStoreDetail(StoreEditDTO storeEditDTO) {
        StoreDetail storeDetail = new StoreDetail();
        BeanUtil.copyProperties(storeEditDTO, storeDetail);
        storeDetailService.update(storeDetail, new QueryWrapper<StoreDetail>().eq("store_id", storeEditDTO.getStoreId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(String id, Integer passed) {
        Store store = this.getById(id);
        if (store == null) {
            throw new ServiceException(ResultCode.STORE_NOT_EXIST);
        }
        if (passed == 0) {
            approveStore(store);
        } else {
            store.setStoreDisable(StoreStatusEnum.REFUSED.value());
        }
        cache.remove(CachePrefix.STORE.getPrefix() + store.getId());
        return this.updateById(store);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(String id, StoreAuditDTO auditDTO) {
        Store store = this.getById(id);
        if (store == null) {
            throw new ServiceException(ResultCode.STORE_NOT_EXIST);
        }
        String originalAuditStatus = store.getAuditStatus();
        String auditStatus = auditDTO.getAuditStatus();
        if (!StoreAuditStatusEnum.APPROVED.name().equals(auditStatus)
                && !StoreAuditStatusEnum.REJECTED.name().equals(auditStatus)
                && !StoreAuditStatusEnum.FROZEN.name().equals(auditStatus)) {
            throw new ServiceException(ResultCode.STORE_AUDIT_STATUS_ERROR);
        }
        store.setAuditStatus(auditStatus);
        store.setAuditRemark(auditDTO.getAuditRemark());
        if (StoreAuditStatusEnum.APPROVED.name().equals(auditStatus)) {
            approveStore(store);
        } else if (StoreAuditStatusEnum.REJECTED.name().equals(auditStatus)) {
            store.setStoreDisable(StoreStatusEnum.REFUSED.value());
        } else {
            store.setStoreDisable(StoreStatusEnum.CLOSED.value());
        }
        cache.remove(CachePrefix.STORE.getPrefix() + store.getId());
        boolean updated = this.updateById(store);
        if (updated) {
            saveAuditLog(store.getId(), originalAuditStatus, auditStatus, auditDTO.getAuditRemark());
        }
        return updated;
    }

    @Override
    public boolean disable(String id) {
        Store store = this.getById(id);
        if (store != null) {

            LambdaUpdateWrapper<Store> storeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            storeLambdaUpdateWrapper.eq(Store::getId, id);
            storeLambdaUpdateWrapper.set(Store::getStoreDisable, StoreStatusEnum.CLOSED.value());
            boolean update = this.update(storeLambdaUpdateWrapper);
            //下架所有此店铺商品
            if (update) {
                goodsService.underStoreGoods(id);
                cache.remove(CachePrefix.STORE.getPrefix() + id);
            }

            //删除店员token
            clerkService.list(new LambdaQueryWrapper<Clerk>().eq(Clerk::getStoreId, id)).forEach(clerk -> {
                cache.vagueDel(CachePrefix.ACCESS_TOKEN.getPrefix(UserEnums.STORE, clerk.getMemberId()));
                cache.vagueDel(CachePrefix.REFRESH_TOKEN.getPrefix(UserEnums.STORE, clerk.getMemberId()));
            });

            return update;
        }

        throw new ServiceException(ResultCode.STORE_NOT_EXIST);
    }

    @Override
    public boolean enable(String id) {
        Store store = this.getById(id);
        if (store != null) {
            store.setStoreDisable(StoreStatusEnum.OPEN.value());
            boolean updated = this.updateById(store);
            if (updated) {
                cache.remove(CachePrefix.STORE.getPrefix() + id);
            }
            return updated;
        }
        throw new ServiceException(ResultCode.STORE_NOT_EXIST);
    }

    @Override
    public boolean applyFirstStep(StoreCompanyDTO storeCompanyDTO) {
        validateBusinessIdentity(
                storeCompanyDTO.getStoreType(),
                storeCompanyDTO.getAgentLevel(),
                storeCompanyDTO.getAgentRegionId(),
                storeCompanyDTO.getAgentRegionName()
        );
        //获取当前操作的店铺
        Store store = getStoreByMember();

        //店铺为空，则新增店铺
        if (store == null) {
            AuthUser authUser = Objects.requireNonNull(UserContext.getCurrentUser());
            Member member = memberService.getById(authUser.getId());
            //根据客户创建店铺
            store = new Store(member);
            BeanUtil.copyProperties(storeCompanyDTO, store);
            this.save(store);
            StoreDetail storeDetail = new StoreDetail();
            storeDetail.setStoreId(store.getId());
            BeanUtil.copyProperties(storeCompanyDTO, storeDetail);
            applyBusinessIdentity(
                    store,
                    storeDetail,
                    storeCompanyDTO.getApplyType(),
                    storeCompanyDTO.getStoreType(),
                    storeCompanyDTO.getAgentLevel(),
                    storeCompanyDTO.getAgentRegionId(),
                    storeCompanyDTO.getAgentRegionName()
            );
            this.updateById(store);
            return storeDetailService.save(storeDetail);
        } else {

            //校验迪纳普状态
            checkStoreStatus(store);
            String effectiveApplyType = CharSequenceUtil.isNotBlank(storeCompanyDTO.getApplyType()) ? storeCompanyDTO.getApplyType() : store.getApplyType();
            String effectiveBizType = CharSequenceUtil.isNotBlank(storeCompanyDTO.getStoreType()) ? storeCompanyDTO.getStoreType() : store.getStoreType();
            String effectiveAgentLevel = CharSequenceUtil.isNotBlank(storeCompanyDTO.getAgentLevel()) ? storeCompanyDTO.getAgentLevel() : store.getAgentLevel();
            String effectiveRegionId = CharSequenceUtil.isNotBlank(storeCompanyDTO.getAgentRegionId()) ? storeCompanyDTO.getAgentRegionId() : store.getAgentRegionId();
            String effectiveRegionName = CharSequenceUtil.isNotBlank(storeCompanyDTO.getAgentRegionName()) ? storeCompanyDTO.getAgentRegionName() : store.getAgentRegionName();
            //复制参数 修改已存在店铺
            BeanUtil.copyProperties(storeCompanyDTO, store);
            applyBusinessIdentity(
                    store,
                    null,
                    effectiveApplyType,
                    effectiveBizType,
                    effectiveAgentLevel,
                    effectiveRegionId,
                    effectiveRegionName
            );
            this.updateById(store);
            //判断是否存在店铺详情，如果没有则进行新建，如果存在则进行修改
            StoreDetail storeDetail = storeDetailService.getStoreDetail(store.getId());
            //如果店铺详情为空，则new ，否则复制对象，然后保存即可。
            if (storeDetail == null) {
                storeDetail = new StoreDetail();
                storeDetail.setStoreId(store.getId());
                BeanUtil.copyProperties(storeCompanyDTO, storeDetail);
                applyBusinessIdentity(
                        null,
                        storeDetail,
                        effectiveApplyType,
                        effectiveBizType,
                        effectiveAgentLevel,
                        effectiveRegionId,
                        effectiveRegionName
                );
                return storeDetailService.save(storeDetail);
            } else {
                BeanUtil.copyProperties(storeCompanyDTO, storeDetail);
                applyBusinessIdentity(
                        null,
                        storeDetail,
                        effectiveApplyType,
                        effectiveBizType,
                        effectiveAgentLevel,
                        effectiveRegionId,
                        effectiveRegionName
                );
                return storeDetailService.updateById(storeDetail);
            }
        }
    }

    @Override
    public boolean applySecondStep(StoreBankDTO storeBankDTO) {

        //获取当前操作的店铺
        Store store = getStoreByMember();
        //校验店铺状态
        checkStoreStatus(store);
        StoreDetail storeDetail = storeDetailService.getStoreDetail(store.getId());
        //设置店铺的银行信息
        BeanUtil.copyProperties(storeBankDTO, storeDetail);
        return storeDetailService.updateById(storeDetail);
    }

    @Override
    public boolean applyThirdStep(StoreOtherInfoDTO storeOtherInfoDTO) {
        //获取当前操作的店铺
        Store store = getStoreByMember();

        //校验店铺状态
        checkStoreStatus(store);
        String currentApplyType = store.getApplyType();
        String currentBizType = store.getStoreType();
        String currentAgentLevel = store.getAgentLevel();
        String currentRegionId = store.getAgentRegionId();
        String currentRegionName = store.getAgentRegionName();
        BeanUtil.copyProperties(storeOtherInfoDTO, store);

        StoreDetail storeDetail = storeDetailService.getStoreDetail(store.getId());
        String effectiveApplyType = CharSequenceUtil.isNotBlank(storeOtherInfoDTO.getApplyType()) ? storeOtherInfoDTO.getApplyType() : currentApplyType;
        String effectiveBizType = CharSequenceUtil.isNotBlank(storeOtherInfoDTO.getStoreType()) ? storeOtherInfoDTO.getStoreType() : currentBizType;
        String effectiveAgentLevel = CharSequenceUtil.isNotBlank(storeOtherInfoDTO.getAgentLevel()) ? storeOtherInfoDTO.getAgentLevel() : currentAgentLevel;
        String effectiveRegionId = CharSequenceUtil.isNotBlank(storeOtherInfoDTO.getAgentRegionId()) ? storeOtherInfoDTO.getAgentRegionId() : currentRegionId;
        String effectiveRegionName = CharSequenceUtil.isNotBlank(storeOtherInfoDTO.getAgentRegionName()) ? storeOtherInfoDTO.getAgentRegionName() : currentRegionName;
        validateBusinessIdentity(effectiveBizType, effectiveAgentLevel, effectiveRegionId, effectiveRegionName);
        applyBusinessIdentity(store, storeDetail, effectiveApplyType, effectiveBizType, effectiveAgentLevel, effectiveRegionId, effectiveRegionName);
        //设置店铺的其他信息
        BeanUtil.copyProperties(storeOtherInfoDTO, storeDetail);
        //设置店铺经营范围
        storeDetail.setGoodsManagementCategory(storeOtherInfoDTO.getGoodsManagementCategory());
        //最后一步申请，给予店铺设置库存预警默认值
        storeDetail.setStockWarning(10);
        //修改店铺详细信息
        storeDetailService.updateById(storeDetail);
        //设置店铺名称,修改店铺信息
        store.setStoreDisable(StoreStatusEnum.APPLYING.name());
        store.setAuditStatus(StoreAuditStatusEnum.SUBMITTED.name());
        return this.updateById(store);
    }

    /**
     * 申请店铺时 对店铺状态进行校验判定
     *
     * @param store 店铺
     */
    private void checkStoreStatus(Store store) {
        if (store == null) {
            throw new ServiceException(ResultCode.STORE_NOT_EXIST);
        }

        //如果店铺状态为已开启、已关闭、申请中，则抛出异常
        if (store.getStoreDisable().equals(StoreStatusEnum.OPEN.name())
                || store.getStoreDisable().equals(StoreStatusEnum.CLOSED.name())
                || store.getStoreDisable().equals(StoreStatusEnum.APPLYING.name())
        ) {
            throw new ServiceException(ResultCode.STORE_STATUS_ERROR);
        }

    }

    @Override
    public void updateStoreGoodsNum(String storeId, Long num) {
        //修改店铺商品数量
        this.update(new LambdaUpdateWrapper<Store>()
                .set(Store::getGoodsNum, num)
                .eq(Store::getId, storeId));
    }

    @Override
    public void updateStoreCollectionNum(CollectionDTO collectionDTO) {
        baseMapper.updateCollection(collectionDTO.getId(), collectionDTO.getNum());
    }

    @Override
    public void storeToClerk() {
        //清空店铺信息方便重新导入不会有重复数据
        clerkService.remove(new LambdaQueryWrapper<Clerk>().eq(Clerk::getShopkeeper, true));
        List<Clerk> clerkList = new ArrayList<>();
        //遍历已开启的店铺
        for (Store store : this.list(new LambdaQueryWrapper<Store>().eq(Store::getDeleteFlag, false).eq(Store::getStoreDisable,
                StoreStatusEnum.OPEN.name()))) {
            clerkList.add(new Clerk(store));
        }
        clerkService.saveBatch(clerkList);
    }

    @Override
    public List<GoodsSku> getToMemberHistory(String memberId) {
        AuthUser currentUser = UserContext.getCurrentUser();
        List<String> skuIdList = new ArrayList<>();
        for (FootPrint footPrint :
                footprintService.list(new LambdaUpdateWrapper<FootPrint>().eq(FootPrint::getStoreId, currentUser.getStoreId()).eq(FootPrint::getMemberId, memberId))) {
            if (footPrint.getSkuId() != null) {
                skuIdList.add(footPrint.getSkuId());
            }
        }
        return goodsSkuService.getGoodsSkuByIdFromCache(skuIdList);
    }

    @Override
    public List<Store> listOpenStores() {
        return this.list(new LambdaQueryWrapper<Store>().eq(Store::getStoreDisable, StoreStatusEnum.OPEN.name()));
    }

    @Override
    public Store getStoreByMemberId(String memberId) {
        if (CharSequenceUtil.isBlank(memberId)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<Store>().eq(Store::getMemberId, memberId).last("limit 1"), false);
    }

    @Override
    public StoreAuditSummaryVO managementSummary() {
        StoreAuditSummaryVO summary = new StoreAuditSummaryVO();
        summary.setTotalCount(this.count(Wrappers.lambdaQuery()));
        summary.setSubmittedCount(this.count(Wrappers.<Store>lambdaQuery().eq(Store::getAuditStatus, StoreAuditStatusEnum.SUBMITTED.name())));
        summary.setApprovedCount(this.count(Wrappers.<Store>lambdaQuery().eq(Store::getAuditStatus, StoreAuditStatusEnum.APPROVED.name())));
        summary.setRejectedCount(this.count(Wrappers.<Store>lambdaQuery().eq(Store::getAuditStatus, StoreAuditStatusEnum.REJECTED.name())));
        summary.setFrozenCount(this.count(Wrappers.<Store>lambdaQuery().eq(Store::getAuditStatus, StoreAuditStatusEnum.FROZEN.name())));
        summary.setOpenCount(this.count(Wrappers.<Store>lambdaQuery().eq(Store::getStoreDisable, StoreStatusEnum.OPEN.name())));
        summary.setClosedCount(this.count(Wrappers.<Store>lambdaQuery().eq(Store::getStoreDisable, StoreStatusEnum.CLOSED.name())));
        return summary;
    }

    /**
     * 获取当前登录操作的店铺
     *
     * @return 店铺信息
     */
    private Store getStoreByMember() {
        LambdaQueryWrapper<Store> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (UserContext.getCurrentUser() != null) {
            lambdaQueryWrapper.eq(Store::getMemberId, UserContext.getCurrentUser().getId());
        }
        return this.getOne(lambdaQueryWrapper, false);
    }

    /**
     * 店铺审核通过后执行初始化
     *
     * @param store 店铺
     */
    private void approveStore(Store store) {
        store.setStoreDisable(StoreStatusEnum.OPEN.value());
        Member member = memberService.getById(store.getMemberId());
        if (member != null) {
            member.setHaveStore(true);
            member.setStoreId(store.getId());
            memberService.updateById(member);
            Clerk existingClerk = clerkService.getClerkByMemberId(member.getId());
            if (existingClerk == null) {
                ClerkAddDTO clerkAddDTO = new ClerkAddDTO();
                clerkAddDTO.setMemberId(member.getId());
                clerkAddDTO.setIsSuper(true);
                clerkAddDTO.setShopkeeper(true);
                clerkAddDTO.setStoreId(store.getId());
                clerkService.saveClerk(clerkAddDTO);
            } else if (store.getId().equals(existingClerk.getStoreId())) {
                existingClerk.setStatus(true);
                existingClerk.setIsSuper(true);
                existingClerk.setShopkeeper(true);
                clerkService.updateById(existingClerk);
            } else {
                throw new ServiceException(ResultCode.CLERK_USER_ERROR);
            }
        }
        storeDetailService.update(new LambdaUpdateWrapper<StoreDetail>()
                .eq(StoreDetail::getStoreId, store.getId())
                .set(StoreDetail::getSettlementDay, new DateTime()));
        createAgentRoleIfNecessary(store);
    }

    /**
     * 校验申请主体类型对应的必填字段
     *
     * @param applyDTO 申请信息
     */
    private void validateApplyType(AdminStoreApplyDTO applyDTO) {
        if (applyDTO == null || CharSequenceUtil.isBlank(applyDTO.getApplyType())) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
        validateBusinessIdentity(applyDTO.getStoreType(), applyDTO.getAgentLevel(), applyDTO.getAgentRegionId(), applyDTO.getAgentRegionName());
        String applyType = applyDTO.getApplyType();
        if (StoreApplyTypeEnum.PERSONAL.name().equals(applyType)) {
            validatePersonalApply(applyDTO.getRealName(), applyDTO.getIdCardNo(), applyDTO.getIdCardFrontUrl(), applyDTO.getIdCardBackUrl(), applyDTO.getLinkPhone());
            return;
        }
        validateBusinessApply(applyDTO.getStoreName(), applyDTO.getBusinessLicenseUrl(), applyDTO.getCreditCode(), applyDTO.getLegalName(), applyDTO.getLegalId(), applyDTO.getLegalMobile());
        if (StoreApplyTypeEnum.COMPANY_NON_LEGAL.name().equals(applyType) && CharSequenceUtil.isBlank(applyDTO.getAuthorizationUrl())) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
    }

    /**
     * 校验申请主体类型对应的必填字段
     *
     * @param applyDTO 申请信息
     */
    private void validateApplyType(StoreEditDTO applyDTO) {
        if (applyDTO == null || CharSequenceUtil.isBlank(applyDTO.getApplyType())) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
        validateBusinessIdentity(applyDTO.getStoreType(), applyDTO.getAgentLevel(), applyDTO.getAgentRegionId(), applyDTO.getAgentRegionName());
        String applyType = applyDTO.getApplyType();
        if (StoreApplyTypeEnum.PERSONAL.name().equals(applyType)) {
            validatePersonalApply(applyDTO.getRealName(), applyDTO.getIdCardNo(), applyDTO.getIdCardFrontUrl(), applyDTO.getIdCardBackUrl(), applyDTO.getLinkPhone());
            return;
        }
        validateBusinessApply(applyDTO.getStoreName(), applyDTO.getBusinessLicenseUrl(), applyDTO.getCreditCode(), applyDTO.getLegalName(), applyDTO.getLegalId(), applyDTO.getLegalMobile());
        if (StoreApplyTypeEnum.COMPANY_NON_LEGAL.name().equals(applyType) && CharSequenceUtil.isBlank(applyDTO.getAuthorizationUrl())) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
    }

    /**
     * 校验个人主体申请
     */
    private void validatePersonalApply(String realName, String idCardNo, String idCardFrontUrl, String idCardBackUrl, String mobile) {
        if (CharSequenceUtil.hasBlank(realName, idCardNo, idCardFrontUrl, idCardBackUrl, mobile)) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
    }

    /**
     * 校验企业主体申请
     */
    private void validateBusinessApply(String storeName, String businessLicenseUrl, String creditCode, String legalName, String legalIdCard, String legalMobile) {
        if (CharSequenceUtil.hasBlank(storeName, businessLicenseUrl, creditCode, legalName, legalIdCard, legalMobile)) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
    }

    /**
     * 记录店铺审核历史
     *
     * @param storeId 店铺ID
     * @param fromAuditStatus 审核前状态
     * @param toAuditStatus 审核后状态
     * @param auditRemark 审核备注
     */
    private void saveAuditLog(String storeId, String fromAuditStatus, String toAuditStatus, String auditRemark) {
        AuthUser currentUser = UserContext.getCurrentUser();
        StoreAuditLog log = new StoreAuditLog();
        log.setStoreId(storeId);
        log.setFromAuditStatus(fromAuditStatus);
        log.setToAuditStatus(toAuditStatus);
        log.setAuditRemark(auditRemark);
        if (currentUser != null) {
            log.setOperatorId(currentUser.getId());
            log.setOperatorName(currentUser.getUsername());
        }
        storeAuditLogService.save(log);
    }

    private void applyBusinessIdentity(Store store, StoreDetail storeDetail, String applyType, String bizType, String agentLevel,
                                       String agentRegionId, String agentRegionName) {
        String normalizedBizType = normalizeBizType(bizType);
        if (store != null) {
            if (CharSequenceUtil.isNotBlank(applyType)) {
                store.setApplyType(applyType);
            }
            store.setStoreType(normalizedBizType);
            if (StoreBizTypeEnum.AGENT.name().equals(normalizedBizType)) {
                store.setAgentLevel(normalizeAgentLevel(agentLevel));
                store.setAgentRegionId(agentRegionId);
                store.setAgentRegionName(agentRegionName);
            } else {
                store.setAgentLevel(null);
                store.setAgentRegionId(null);
                store.setAgentRegionName(null);
            }
        }
        if (storeDetail != null) {
            if (CharSequenceUtil.isNotBlank(applyType)) {
                storeDetail.setApplyType(applyType);
            }
            storeDetail.setStoreType(normalizedBizType);
            if (StoreBizTypeEnum.AGENT.name().equals(normalizedBizType)) {
                storeDetail.setAgentLevel(normalizeAgentLevel(agentLevel));
                storeDetail.setAgentRegionId(agentRegionId);
                storeDetail.setAgentRegionName(agentRegionName);
            } else {
                storeDetail.setAgentLevel(null);
                storeDetail.setAgentRegionId(null);
                storeDetail.setAgentRegionName(null);
            }
        }
    }

    private void validateBusinessIdentity(String bizType, String agentLevel, String agentRegionId, String agentRegionName) {
        String normalizedBizType = normalizeBizType(bizType);
        if (StoreBizTypeEnum.AGENT.name().equals(normalizedBizType)) {
            normalizeAgentLevel(agentLevel);
            if (CharSequenceUtil.hasBlank(agentRegionId, agentRegionName)) {
                throw new ServiceException(ResultCode.AGENT_REGION_REQUIRED);
            }
        }
    }

    private String normalizeBizType(String bizType) {
        if (CharSequenceUtil.isBlank(bizType)) {
            return StoreBizTypeEnum.SUPPLIER.name();
        }
        String normalized = bizType.trim().toUpperCase(Locale.ROOT);
        try {
            return StoreBizTypeEnum.valueOf(normalized).name();
        } catch (IllegalArgumentException exception) {
            throw new ServiceException(ResultCode.STORE_BIZ_TYPE_ERROR);
        }
    }

    private String normalizeAgentLevel(String agentLevel) {
        if (CharSequenceUtil.isBlank(agentLevel)) {
            throw new ServiceException(ResultCode.AGENT_LEVEL_REQUIRED);
        }
        String normalized = agentLevel.trim().toUpperCase(Locale.ROOT);
        try {
            return AgentLevelEnum.valueOf(normalized).name();
        } catch (IllegalArgumentException exception) {
            throw new ServiceException(ResultCode.AGENT_LEVEL_ERROR);
        }
    }

    private void createAgentRoleIfNecessary(Store store) {
        if (!StoreBizTypeEnum.AGENT.name().equalsIgnoreCase(store.getStoreType())) {
            return;
        }
        AgentRoleCreateDTO dto = new AgentRoleCreateDTO();
        dto.setMemberId(store.getMemberId());
        dto.setRegionId(store.getAgentRegionId());
        dto.setRegionName(store.getAgentRegionName());
        dto.setAgentLevel(store.getAgentLevel());
        dto.setRemark("店铺入驻审核通过自动创建代理身份");
        agentRoleRelationService.ensureAgentRole(dto);
    }

}
