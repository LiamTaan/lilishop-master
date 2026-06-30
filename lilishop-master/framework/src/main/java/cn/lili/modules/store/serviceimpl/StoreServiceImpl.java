package cn.lili.modules.store.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.properties.RocketmqCustomProperties;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
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
import cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum;
import cn.lili.modules.member.entity.dto.ClerkAddDTO;
import cn.lili.modules.member.entity.dto.CollectionDTO;
import cn.lili.modules.member.service.ClerkService;
import cn.lili.modules.member.service.FootprintService;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.sms.SmsUtil;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.dos.StoreAuditLog;
import cn.lili.modules.store.entity.dos.StoreDetail;
import cn.lili.modules.store.entity.dto.StoreAdminSaveDTO;
import cn.lili.modules.store.entity.dto.StoreApplyCommonDTO;
import cn.lili.modules.store.entity.dto.StoreApplySubmitDTO;
import cn.lili.modules.store.entity.dto.StoreApplyTypeSelectDTO;
import cn.lili.modules.store.entity.dto.StoreAuditDTO;
import cn.lili.modules.store.entity.dto.StoreCompanyAuthorizedApplyDTO;
import cn.lili.modules.store.entity.dto.StoreCompanyLegalApplyDTO;
import cn.lili.modules.store.entity.dto.StoreEditDTO;
import cn.lili.modules.store.entity.dto.StoreIndividualApplyDTO;
import cn.lili.modules.store.entity.dto.StorePersonalApplyDTO;
import cn.lili.modules.store.entity.enums.CompanyIdentityTypeEnum;
import cn.lili.modules.store.entity.enums.StoreAuditStatusEnum;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.entity.enums.StoreStatusEnum;
import cn.lili.modules.store.entity.enums.StoreSubjectTypeEnum;
import cn.lili.modules.store.entity.vos.StoreAuditSummaryVO;
import cn.lili.modules.store.entity.vos.StoreSearchParams;
import cn.lili.modules.store.entity.vos.StoreVO;
import cn.lili.modules.system.entity.dos.Region;
import cn.lili.modules.system.service.RegionService;
import cn.lili.modules.store.mapper.StoreMapper;
import cn.lili.modules.store.service.StoreAuditLogService;
import cn.lili.modules.store.service.StoreDetailService;
import cn.lili.modules.store.service.StoreService;
import cn.lili.mybatis.util.PageUtil;
import cn.lili.modules.verification.entity.enums.VerificationEnums;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    @Autowired
    @Lazy
    private MemberService memberService;

    @Autowired
    private ClerkService clerkService;

    @Autowired
    @Lazy
    private GoodsService goodsService;

    @Autowired
    @Lazy
    private GoodsSkuService goodsSkuService;

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

    @Autowired
    private SmsUtil smsUtil;

    @Autowired
    @Lazy
    private RegionService regionService;

    @Override
    public IPage<StoreVO> findByConditionPage(StoreSearchParams storeSearchParams, PageVO page) {
        return this.baseMapper.getStoreList(PageUtil.initPage(page), storeSearchParams.queryWrapper("s"));
    }

    @Override
    public List<Store> listByIds(Collection<? extends Serializable> idList) {
        if (idList == null || idList.isEmpty()) {
            return Collections.emptyList();
        }
        return super.listByIds(idList);
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
    public Store add(StoreAdminSaveDTO storeAdminSaveDTO) {
        validateStoreNameUnique(storeAdminSaveDTO.getStoreName(), null);
        validateApplicationPayload(storeAdminSaveDTO.getSubjectType(), storeAdminSaveDTO.getCompanyIdentityType(), storeAdminSaveDTO);

        Member member = memberService.getById(storeAdminSaveDTO.getMemberId());
        if (member == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(member.getHaveStore())) {
            throw new ServiceException(ResultCode.STORE_APPLY_DOUBLE_ERROR);
        }

        Store store = new Store(member);
        applyStoreCommonFields(store, storeAdminSaveDTO);
        applyIdentity(store, null, storeAdminSaveDTO.getSubjectType(), storeAdminSaveDTO.getCompanyIdentityType(),
                storeAdminSaveDTO.getStoreType(), storeAdminSaveDTO.getAgentLevel(),
                storeAdminSaveDTO.getAgentRegionId(), storeAdminSaveDTO.getAgentRegionName());
        store.setStoreDisable(StoreStatusEnum.APPLYING.name());
        store.setAuditStatus(StoreAuditStatusEnum.SUBMITTED.name());
        this.save(store);

        StoreDetail storeDetail = getOrCreateStoreDetail(store.getId());
        applyStoreDetailCommonFields(storeDetail, storeAdminSaveDTO);
        applySubjectSpecificFields(storeDetail, storeAdminSaveDTO.getSubjectType(), storeAdminSaveDTO.getCompanyIdentityType(), storeAdminSaveDTO);
        applyIdentity(null, storeDetail, storeAdminSaveDTO.getSubjectType(), storeAdminSaveDTO.getCompanyIdentityType(),
                storeAdminSaveDTO.getStoreType(), storeAdminSaveDTO.getAgentLevel(),
                storeAdminSaveDTO.getAgentRegionId(), storeAdminSaveDTO.getAgentRegionName());
        if (storeDetail.getStockWarning() == null) {
            storeDetail.setStockWarning(10);
        }
        storeDetailService.saveOrUpdate(storeDetail);

        memberService.update(new LambdaUpdateWrapper<Member>()
                .eq(Member::getId, member.getId())
                .set(Member::getHaveStore, true)
                .set(Member::getStoreId, store.getId()));
        return store;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Store edit(StoreEditDTO storeEditDTO) {
        Store store = this.getById(storeEditDTO.getStoreId());
        if (store == null) {
            throw new ServiceException(ResultCode.STORE_NOT_EXIST);
        }
        validateStoreNameUnique(storeEditDTO.getStoreName(), storeEditDTO.getStoreId());
        String subjectType = resolveSubjectType(storeEditDTO.getSubjectType(), store.getSubjectType());
        String companyIdentityType = resolveCompanyIdentityType(storeEditDTO.getCompanyIdentityType(), store.getCompanyIdentityType(), subjectType);
        storeEditDTO.setSubjectType(subjectType);
        storeEditDTO.setCompanyIdentityType(companyIdentityType);
        validateApplicationPayload(subjectType, companyIdentityType, storeEditDTO);

        StoreDetail storeDetail = getOrCreateStoreDetail(store.getId());
        applyStoreCommonFields(store, storeEditDTO);
        applyIdentity(store, null, subjectType, companyIdentityType, storeEditDTO.getStoreType(),
                storeEditDTO.getAgentLevel(), storeEditDTO.getAgentRegionId(), storeEditDTO.getAgentRegionName());
        if (StoreAuditStatusEnum.REJECTED.name().equals(store.getAuditStatus())
                || StoreAuditStatusEnum.DRAFT.name().equals(store.getAuditStatus())) {
            store.setAuditStatus(StoreAuditStatusEnum.SUBMITTED.name());
            store.setStoreDisable(StoreStatusEnum.APPLYING.name());
        }
        this.updateById(store);

        applyStoreDetailCommonFields(storeDetail, storeEditDTO);
        applySubjectSpecificFields(storeDetail, subjectType, companyIdentityType, storeEditDTO);
        applyIdentity(null, storeDetail, subjectType, companyIdentityType, storeEditDTO.getStoreType(),
                storeEditDTO.getAgentLevel(), storeEditDTO.getAgentRegionId(), storeEditDTO.getAgentRegionName());
        storeDetail.setStoreId(store.getId());
        storeDetailService.saveOrUpdate(storeDetail);

        storeDetailService.updateStoreGoodsInfo(store);
        removeStoreCache(store.getId());
        String destination = rocketmqCustomProperties.getStoreTopic() + ":" + StoreTagsEnum.EDIT_STORE_SETTING.name();
        rocketMQTemplate.asyncSend(destination, store, RocketmqSendCallbackBuilder.commonCallback());
        return store;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(String id, Integer passed) {
        Store store = this.getById(id);
        if (store == null) {
            throw new ServiceException(ResultCode.STORE_NOT_EXIST);
        }
        String originalAuditStatus = store.getAuditStatus();
        if (passed == 0) {
            store.setAuditStatus(StoreAuditStatusEnum.APPROVED.name());
            store.setAuditRemark(null);
            approveStore(store);
        } else {
            store.setAuditStatus(StoreAuditStatusEnum.REJECTED.name());
            store.setStoreDisable(StoreStatusEnum.REFUSED.value());
        }
        removeStoreCache(store.getId());
        boolean updated = this.updateById(store);
        if (updated) {
            saveAuditLog(store.getId(), originalAuditStatus, store.getAuditStatus(), store.getAuditRemark());
        }
        return updated;
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
            applyManagerAuditAssignment(store, auditDTO);
            approveStore(store);
        } else if (StoreAuditStatusEnum.REJECTED.name().equals(auditStatus)) {
            store.setStoreDisable(StoreStatusEnum.REFUSED.value());
        } else {
            store.setStoreDisable(StoreStatusEnum.CLOSED.value());
        }
        removeStoreCache(store.getId());
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
            if (update) {
                goodsService.underStoreGoods(id);
                removeStoreCache(id);
            }
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
                removeStoreCache(id);
            }
            return updated;
        }
        throw new ServiceException(ResultCode.STORE_NOT_EXIST);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean selectApplyType(StoreApplyTypeSelectDTO applyTypeSelectDTO) {
        String subjectType = normalizeSubjectType(applyTypeSelectDTO.getSubjectType());
        ensureBuyerApplyAgentContext();
        Store store = prepareMutableStore();
        StoreDetail storeDetail = getOrCreateStoreDetail(store.getId());

        applyIdentity(store, storeDetail, subjectType, null, StoreBizTypeEnum.AGENT.name(), null, null, null);
        store.setStoreDisable(StoreStatusEnum.APPLY.value());
        store.setAuditStatus(StoreAuditStatusEnum.DRAFT.name());
        store.setAuditRemark(null);
        this.updateById(store);
        storeDetailService.saveOrUpdate(storeDetail);
        removeStoreCache(store.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyPersonal(StorePersonalApplyDTO personalApplyDTO, String uuid) {
        verifyStoreApplySms(personalApplyDTO.getMobile(), uuid, personalApplyDTO.getSmsCode());
        ensureBuyerApplyAgentContext();
        validateApplicationPayload(StoreSubjectTypeEnum.PERSONAL.name(), null, personalApplyDTO);
        Store store = prepareMutableStore();
        StoreDetail storeDetail = getOrCreateStoreDetail(store.getId());
        applyStoreCommonFields(store, personalApplyDTO);
        applyStoreDetailCommonFields(storeDetail, personalApplyDTO);
        applySubjectSpecificFields(storeDetail, StoreSubjectTypeEnum.PERSONAL.name(), null, personalApplyDTO);
        applyIdentity(store, storeDetail, StoreSubjectTypeEnum.PERSONAL.name(), null, StoreBizTypeEnum.AGENT.name(), null, null, null);
        return submitApplication(store, storeDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyIndividual(StoreIndividualApplyDTO individualApplyDTO, String uuid) {
        verifyStoreApplySms(individualApplyDTO.getMobile(), uuid, individualApplyDTO.getSmsCode());
        ensureBuyerApplyAgentContext();
        validateApplicationPayload(StoreSubjectTypeEnum.INDIVIDUAL.name(), null, individualApplyDTO);
        Store store = prepareMutableStore();
        StoreDetail storeDetail = getOrCreateStoreDetail(store.getId());
        applyStoreCommonFields(store, individualApplyDTO);
        applyStoreDetailCommonFields(storeDetail, individualApplyDTO);
        applySubjectSpecificFields(storeDetail, StoreSubjectTypeEnum.INDIVIDUAL.name(), null, individualApplyDTO);
        applyIdentity(store, storeDetail, StoreSubjectTypeEnum.INDIVIDUAL.name(), null, StoreBizTypeEnum.AGENT.name(), null, null, null);
        return submitApplication(store, storeDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyCompanyLegal(StoreCompanyLegalApplyDTO companyLegalApplyDTO, String uuid) {
        verifyStoreApplySms(companyLegalApplyDTO.getMobile(), uuid, companyLegalApplyDTO.getSmsCode());
        ensureBuyerApplyAgentContext();
        validateApplicationPayload(StoreSubjectTypeEnum.COMPANY.name(), CompanyIdentityTypeEnum.LEGAL.name(), companyLegalApplyDTO);
        Store store = prepareMutableStore();
        StoreDetail storeDetail = getOrCreateStoreDetail(store.getId());
        applyStoreCommonFields(store, companyLegalApplyDTO);
        applyStoreDetailCommonFields(storeDetail, companyLegalApplyDTO);
        applySubjectSpecificFields(storeDetail, StoreSubjectTypeEnum.COMPANY.name(), CompanyIdentityTypeEnum.LEGAL.name(), companyLegalApplyDTO);
        applyIdentity(store, storeDetail, StoreSubjectTypeEnum.COMPANY.name(), CompanyIdentityTypeEnum.LEGAL.name(),
                StoreBizTypeEnum.AGENT.name(), null, null, null);
        return submitApplication(store, storeDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyCompanyAuthorized(StoreCompanyAuthorizedApplyDTO companyAuthorizedApplyDTO, String uuid) {
        verifyStoreApplySms(companyAuthorizedApplyDTO.getMobile(), uuid, companyAuthorizedApplyDTO.getSmsCode());
        ensureBuyerApplyAgentContext();
        validateApplicationPayload(StoreSubjectTypeEnum.COMPANY.name(), CompanyIdentityTypeEnum.AUTHORIZED.name(), companyAuthorizedApplyDTO);
        Store store = prepareMutableStore();
        StoreDetail storeDetail = getOrCreateStoreDetail(store.getId());
        applyStoreCommonFields(store, companyAuthorizedApplyDTO);
        applyStoreDetailCommonFields(storeDetail, companyAuthorizedApplyDTO);
        applySubjectSpecificFields(storeDetail, StoreSubjectTypeEnum.COMPANY.name(), CompanyIdentityTypeEnum.AUTHORIZED.name(), companyAuthorizedApplyDTO);
        applyIdentity(store, storeDetail, StoreSubjectTypeEnum.COMPANY.name(), CompanyIdentityTypeEnum.AUTHORIZED.name(),
                StoreBizTypeEnum.AGENT.name(), null, null, null);
        return submitApplication(store, storeDetail);
    }

    @Override
    public void updateStoreGoodsNum(String storeId, Long num) {
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
        clerkService.remove(new LambdaQueryWrapper<Clerk>().eq(Clerk::getShopkeeper, true));
        List<Clerk> clerkList = new ArrayList<>();
        for (Store store : this.list(new LambdaQueryWrapper<Store>()
                .eq(Store::getDeleteFlag, false)
                .eq(Store::getStoreDisable, StoreStatusEnum.OPEN.name()))) {
            clerkList.add(new Clerk(store));
        }
        clerkService.saveBatch(clerkList);
    }

    @Override
    public List<GoodsSku> getToMemberHistory(String memberId) {
        AuthUser currentUser = UserContext.getCurrentUser();
        List<String> skuIdList = new ArrayList<>();
        for (FootPrint footPrint :
                footprintService.list(new LambdaUpdateWrapper<FootPrint>()
                        .eq(FootPrint::getStoreId, currentUser.getStoreId())
                        .eq(FootPrint::getMemberId, memberId))) {
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

    private boolean submitApplication(Store store, StoreDetail storeDetail) {
        validateStoreNameUnique(store.getStoreName(), store.getId());
        store.setStoreDisable(StoreStatusEnum.APPLYING.name());
        store.setAuditStatus(StoreAuditStatusEnum.SUBMITTED.name());
        store.setAuditRemark(null);
        storeDetail.setStoreId(store.getId());
        if (storeDetail.getStockWarning() == null) {
            storeDetail.setStockWarning(10);
        }
        boolean detailUpdated = storeDetailService.saveOrUpdate(storeDetail);
        boolean storeUpdated = this.updateById(store);
        removeStoreCache(store.getId());
        return detailUpdated && storeUpdated;
    }

    private Store prepareMutableStore() {
        Store store = getStoreByMember();
        if (store == null) {
            AuthUser authUser = Objects.requireNonNull(UserContext.getCurrentUser());
            Member member = memberService.getById(authUser.getId());
            if (member == null) {
                throw new ServiceException(ResultCode.USER_NOT_EXIST);
            }
            store = new Store(member);
            this.save(store);
            return store;
        }
        checkStoreStatus(store);
        return store;
    }

    private StoreDetail getOrCreateStoreDetail(String storeId) {
        StoreDetail storeDetail = storeDetailService.getStoreDetail(storeId);
        if (storeDetail == null) {
            storeDetail = new StoreDetail();
            storeDetail.setStoreId(storeId);
        }
        return storeDetail;
    }

    private void verifyStoreApplySms(String mobile, String uuid, String code) {
        if (!smsUtil.verifyCode(mobile, VerificationEnums.STORE_APPLY, uuid, code)) {
            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
        }
    }

    private void ensureBuyerApplyAgentContext() {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        if (currentUser.getRole() != UserEnums.MEMBER
                || currentUser.getIdentityCode() != LoginIdentityCodeEnum.CONSUMER) {
            throw new ServiceException(ResultCode.IDENTITY_NOT_SUPPORTED);
        }
    }

    private void applyStoreCommonFields(Store store, StoreApplyCommonDTO dto) {
        store.setStoreName(dto.getStoreName());
        store.setStoreLogo(dto.getStoreLogo());
        store.setStoreDesc(dto.getStoreDesc());
        store.setStoreCenter(dto.getStoreCenter());
        store.setStoreAddressPath(dto.getStoreAddressPath());
        store.setStoreAddressIdPath(dto.getStoreAddressIdPath());
        store.setStoreAddressDetail(dto.getStoreAddressDetail());
    }

    private void applyStoreDetailCommonFields(StoreDetail storeDetail, StoreApplyCommonDTO dto) {
        storeDetail.setStoreName(dto.getStoreName());
        storeDetail.setStoreLogo(dto.getStoreLogo());
        storeDetail.setStoreDesc(dto.getStoreDesc());
        storeDetail.setStoreCenter(dto.getStoreCenter());
        storeDetail.setStoreAddressPath(dto.getStoreAddressPath());
        storeDetail.setStoreAddressIdPath(dto.getStoreAddressIdPath());
        storeDetail.setStoreAddressDetail(dto.getStoreAddressDetail());
        storeDetail.setBusinessLicenseUrl(dto.getBusinessLicenseUrl());
        storeDetail.setCreditCode(dto.getCreditCode());
        storeDetail.setBusinessLicenseRegionId(dto.getBusinessLicenseRegionId());
        storeDetail.setBusinessLicenseExpireType(dto.getBusinessLicenseExpireType());
        storeDetail.setBusinessLicenseExpireDate(dto.getBusinessLicenseExpireDate());
        storeDetail.setFacadeImageUrl(dto.getFacadeImageUrl());
        storeDetail.setIndoorImageUrls(dto.getIndoorImageUrls());
        storeDetail.setGoodsManagementCategory(dto.getGoodsManagementCategory());
    }

    private void applySubjectSpecificFields(StoreDetail storeDetail, String subjectType, String companyIdentityType, Object payload) {
        clearSubjectSpecificFields(storeDetail, subjectType, companyIdentityType);
        if (payload instanceof StorePersonalApplyDTO dto) {
            storeDetail.setRealName(dto.getRealName());
            storeDetail.setIdCardNo(dto.getIdCardNo());
            storeDetail.setLegalMobile(dto.getMobile());
            return;
        }
        if (payload instanceof StoreIndividualApplyDTO dto) {
            storeDetail.setRealName(dto.getRealName());
            storeDetail.setIdCardNo(dto.getIdCardNo());
            storeDetail.setLegalMobile(dto.getMobile());
            return;
        }
        if (payload instanceof StoreCompanyLegalApplyDTO dto) {
            storeDetail.setLegalName(dto.getLegalName());
            storeDetail.setLegalId(dto.getLegalId());
            storeDetail.setLegalMobile(dto.getMobile());
            return;
        }
        if (payload instanceof StoreCompanyAuthorizedApplyDTO dto) {
            storeDetail.setRealName(dto.getAuthorizedName());
            storeDetail.setIdCardNo(dto.getAuthorizedIdNo());
            storeDetail.setLegalMobile(dto.getMobile());
            storeDetail.setAuthorizationUrl(dto.getAuthorizationUrl());
            return;
        }
        if (payload instanceof StoreEditDTO dto) {
            storeDetail.setRealName(dto.getRealName());
            storeDetail.setIdCardNo(dto.getIdCardNo());
            storeDetail.setLegalName(dto.getLegalName());
            storeDetail.setLegalId(dto.getLegalId());
            storeDetail.setLegalMobile(dto.getLegalMobile());
            storeDetail.setAuthorizationUrl(dto.getAuthorizationUrl());
            storeDetail.setStockWarning(dto.getStockWarning());
            storeDetail.setDdCode(dto.getDdCode());
            storeDetail.setSalesConsigneeName(dto.getSalesConsigneeName());
            storeDetail.setSalesConsigneeMobile(dto.getSalesConsigneeMobile());
            storeDetail.setSalesConsigneeAddressId(dto.getSalesConsigneeAddressId());
            storeDetail.setSalesConsigneeAddressPath(dto.getSalesConsigneeAddressPath());
            storeDetail.setSalesConsigneeDetail(dto.getSalesConsigneeDetail());
            storeDetail.setSalesConsignorName(dto.getSalesConsignorName());
            storeDetail.setSalesConsignorMobile(dto.getSalesConsignorMobile());
            storeDetail.setSalesConsignorAddressId(dto.getSalesConsignorAddressId());
            storeDetail.setSalesConsignorAddressPath(dto.getSalesConsignorAddressPath());
            storeDetail.setSalesConsignorDetail(dto.getSalesConsignorDetail());
        }
    }

    private void clearSubjectSpecificFields(StoreDetail storeDetail, String subjectType, String companyIdentityType) {
        if (StoreSubjectTypeEnum.PERSONAL.name().equals(subjectType)) {
            storeDetail.setBusinessLicenseUrl(null);
            storeDetail.setCreditCode(null);
            storeDetail.setBusinessLicenseRegionId(null);
            storeDetail.setBusinessLicenseExpireType(null);
            storeDetail.setBusinessLicenseExpireDate(null);
            storeDetail.setFacadeImageUrl(null);
            storeDetail.setIndoorImageUrls(null);
            storeDetail.setLegalName(null);
            storeDetail.setLegalId(null);
            storeDetail.setAuthorizationUrl(null);
            return;
        }
        if (StoreSubjectTypeEnum.INDIVIDUAL.name().equals(subjectType)) {
            storeDetail.setLegalName(null);
            storeDetail.setLegalId(null);
            storeDetail.setAuthorizationUrl(null);
            return;
        }
        if (StoreSubjectTypeEnum.COMPANY.name().equals(subjectType)) {
            if (CompanyIdentityTypeEnum.LEGAL.name().equals(companyIdentityType)) {
                storeDetail.setRealName(null);
                storeDetail.setIdCardNo(null);
                storeDetail.setAuthorizationUrl(null);
            } else {
                storeDetail.setLegalName(null);
                storeDetail.setLegalId(null);
            }
        }
    }

    private void validateApplicationPayload(String subjectType, String companyIdentityType, StoreApplyCommonDTO payload) {
        String normalizedSubjectType = normalizeSubjectType(subjectType);
        String normalizedCompanyIdentityType = resolveCompanyIdentityType(companyIdentityType, companyIdentityType, normalizedSubjectType);
        if (payload instanceof StoreAdminSaveDTO || payload instanceof StoreEditDTO) {
            validateBusinessIdentity(payload.getStoreType(), payload.getAgentLevel(), payload.getAgentRegionId(), payload.getAgentRegionName());
        }
        if (CharSequenceUtil.isBlank(payload.getStoreName())) {
            throw new ServiceException(ResultCode.PARAMS_ERROR, "店铺名称不能为空");
        }
        if (StoreSubjectTypeEnum.PERSONAL.name().equals(normalizedSubjectType)) {
            if (!(payload instanceof StorePersonalApplyDTO || payload instanceof StoreEditDTO)) {
                return;
            }
            if (payload instanceof StorePersonalApplyDTO dto && CharSequenceUtil.hasBlank(dto.getRealName(), dto.getIdCardNo(), dto.getMobile())) {
                throw new ServiceException(ResultCode.PARAMS_ERROR, "个人主体请完整填写姓名、身份证号和本人手机号");
            }
            if (payload instanceof StoreEditDTO dto && CharSequenceUtil.hasBlank(dto.getRealName(), dto.getIdCardNo(), dto.getLegalMobile())) {
                throw new ServiceException(ResultCode.PARAMS_ERROR, "个人主体请完整填写姓名、身份证号和本人手机号");
            }
            return;
        }
        if (StoreSubjectTypeEnum.INDIVIDUAL.name().equals(normalizedSubjectType)) {
            if (payload instanceof StoreIndividualApplyDTO dto
                    && CharSequenceUtil.hasBlank(dto.getBusinessLicenseUrl(), dto.getCreditCode(), dto.getRealName(), dto.getIdCardNo(), dto.getMobile())) {
                throw new ServiceException(ResultCode.PARAMS_ERROR, "个体户请完整填写营业执照、信用代码、经营者姓名、身份证号和手机号");
            }
            if (payload instanceof StoreEditDTO dto
                    && CharSequenceUtil.hasBlank(dto.getBusinessLicenseUrl(), dto.getCreditCode(), dto.getRealName(), dto.getIdCardNo(), dto.getLegalMobile())) {
                throw new ServiceException(ResultCode.PARAMS_ERROR, "个体户请完整填写营业执照、信用代码、经营者姓名、身份证号和手机号");
            }
            return;
        }
        if (!StoreSubjectTypeEnum.COMPANY.name().equals(normalizedSubjectType)) {
            throw new ServiceException(ResultCode.PARAMS_ERROR, "申请主体类型不正确");
        }
        if (payload instanceof StoreCompanyLegalApplyDTO dto
                && CharSequenceUtil.hasBlank(dto.getBusinessLicenseUrl(), dto.getCreditCode(), dto.getLegalName(), dto.getLegalId(), dto.getMobile())) {
            throw new ServiceException(ResultCode.PARAMS_ERROR, "企业法人请完整填写营业执照、信用代码和法人信息");
        }
        if (payload instanceof StoreCompanyAuthorizedApplyDTO dto
                && CharSequenceUtil.hasBlank(dto.getBusinessLicenseUrl(), dto.getCreditCode(), dto.getAuthorizedName(), dto.getAuthorizedIdNo(), dto.getAuthorizationUrl(), dto.getMobile())) {
            throw new ServiceException(ResultCode.PARAMS_ERROR, "企业非法人请完整填写营业执照、信用代码、被授权人信息、授权书和手机号");
        }
        if (payload instanceof StoreEditDTO dto) {
            if (CompanyIdentityTypeEnum.LEGAL.name().equals(normalizedCompanyIdentityType)
                    && CharSequenceUtil.hasBlank(dto.getBusinessLicenseUrl(), dto.getCreditCode(), dto.getLegalName(), dto.getLegalId(), dto.getLegalMobile())) {
                throw new ServiceException(ResultCode.PARAMS_ERROR, "企业法人请完整填写营业执照、信用代码和法人信息");
            }
            if (CompanyIdentityTypeEnum.AUTHORIZED.name().equals(normalizedCompanyIdentityType)
                    && CharSequenceUtil.hasBlank(dto.getBusinessLicenseUrl(), dto.getCreditCode(), dto.getRealName(), dto.getIdCardNo(), dto.getAuthorizationUrl(), dto.getLegalMobile())) {
                throw new ServiceException(ResultCode.PARAMS_ERROR, "企业非法人请完整填写营业执照、信用代码、被授权人信息、授权书和手机号");
            }
        }
    }

    private void validateStoreNameUnique(String storeName, String excludeStoreId) {
        if (CharSequenceUtil.isBlank(storeName)) {
            return;
        }
        Store store = getOne(new QueryWrapper<Store>().eq("store_name", storeName));
        if (store != null && !CharSequenceUtil.equals(store.getId(), excludeStoreId)) {
            throw new ServiceException(ResultCode.STORE_NAME_EXIST_ERROR);
        }
    }

    private void applyIdentity(Store store, StoreDetail storeDetail, String subjectType, String companyIdentityType, String bizType,
                               String agentLevel, String agentRegionId, String agentRegionName) {
        String normalizedSubjectType = normalizeSubjectType(subjectType);
        String normalizedCompanyIdentityType = resolveCompanyIdentityType(companyIdentityType, companyIdentityType, normalizedSubjectType);
        String normalizedBizType = normalizeBizType(bizType);
        String normalizedAgentLevel = normalizeOptionalAgentLevel(normalizedBizType, agentLevel);
        String normalizedAgentRegionId = normalizeOptionalAgentValue(normalizedBizType, agentRegionId);
        String normalizedAgentRegionName = normalizeOptionalAgentValue(normalizedBizType, agentRegionName);

        if (store != null) {
            store.setSubjectType(normalizedSubjectType);
            store.setCompanyIdentityType(normalizedCompanyIdentityType);
            store.setStoreType(normalizedBizType);
            store.setAgentLevel(normalizedAgentLevel);
            store.setAgentRegionId(normalizedAgentRegionId);
            store.setAgentRegionName(normalizedAgentRegionName);
        }
        if (storeDetail != null) {
            storeDetail.setSubjectType(normalizedSubjectType);
            storeDetail.setCompanyIdentityType(normalizedCompanyIdentityType);
            storeDetail.setStoreType(normalizedBizType);
            storeDetail.setAgentLevel(normalizedAgentLevel);
            storeDetail.setAgentRegionId(normalizedAgentRegionId);
            storeDetail.setAgentRegionName(normalizedAgentRegionName);
        }
    }

    private void checkStoreStatus(Store store) {
        if (store == null) {
            throw new ServiceException(ResultCode.STORE_NOT_EXIST);
        }
        if (StoreStatusEnum.OPEN.name().equals(store.getStoreDisable())
                || StoreStatusEnum.CLOSED.name().equals(store.getStoreDisable())
                || StoreStatusEnum.APPLYING.name().equals(store.getStoreDisable())) {
            throw new ServiceException(ResultCode.STORE_STATUS_ERROR);
        }
    }

    private Store getStoreByMember() {
        LambdaQueryWrapper<Store> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (UserContext.getCurrentUser() != null) {
            lambdaQueryWrapper.eq(Store::getMemberId, UserContext.getCurrentUser().getId());
        }
        return this.getOne(lambdaQueryWrapper, false);
    }

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
        createAgentRoleIfNecessary(store);
    }

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

    private void validateBusinessIdentity(String bizType, String agentLevel, String agentRegionId, String agentRegionName) {
        String normalizedBizType = normalizeBizType(bizType);
        if (StoreBizTypeEnum.AGENT.name().equals(normalizedBizType)) {
            String normalizedAgentLevel = normalizeAgentLevel(agentLevel);
            if (CharSequenceUtil.hasBlank(agentRegionId, agentRegionName)) {
                throw new ServiceException(ResultCode.AGENT_REGION_REQUIRED);
            }
            validateAgentRegionLevel(normalizedAgentLevel, agentRegionId);
        }
    }

    private void applyManagerAuditAssignment(Store store, StoreAuditDTO auditDTO) {
        if (!StoreBizTypeEnum.AGENT.name().equalsIgnoreCase(store.getStoreType())) {
            return;
        }
        validateBusinessIdentity(StoreBizTypeEnum.AGENT.name(), auditDTO.getAgentLevel(),
                auditDTO.getAgentRegionId(), auditDTO.getAgentRegionName());
        store.setAgentLevel(normalizeAgentLevel(auditDTO.getAgentLevel()));
        store.setAgentRegionId(auditDTO.getAgentRegionId());
        store.setAgentRegionName(auditDTO.getAgentRegionName());
        StoreDetail storeDetail = getOrCreateStoreDetail(store.getId());
        storeDetail.setAgentLevel(store.getAgentLevel());
        storeDetail.setAgentRegionId(store.getAgentRegionId());
        storeDetail.setAgentRegionName(store.getAgentRegionName());
        storeDetail.setStoreType(StoreBizTypeEnum.AGENT.name());
        storeDetailService.saveOrUpdate(storeDetail);
    }

    private String normalizeSubjectType(String subjectType) {
        if (CharSequenceUtil.isBlank(subjectType)) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
        try {
            return StoreSubjectTypeEnum.valueOf(subjectType.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException exception) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
    }

    private String resolveSubjectType(String subjectType, String fallback) {
        return normalizeSubjectType(CharSequenceUtil.isNotBlank(subjectType) ? subjectType : fallback);
    }

    private String resolveCompanyIdentityType(String companyIdentityType, String fallback, String subjectType) {
        if (!StoreSubjectTypeEnum.COMPANY.name().equals(subjectType)) {
            return null;
        }
        String effectiveValue = CharSequenceUtil.isNotBlank(companyIdentityType) ? companyIdentityType : fallback;
        if (CharSequenceUtil.isBlank(effectiveValue)) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
        try {
            return CompanyIdentityTypeEnum.valueOf(effectiveValue.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException exception) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
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

    private String normalizeOptionalAgentLevel(String bizType, String agentLevel) {
        if (!StoreBizTypeEnum.AGENT.name().equals(bizType) || CharSequenceUtil.isBlank(agentLevel)) {
            return null;
        }
        return normalizeAgentLevel(agentLevel);
    }

    private String normalizeOptionalAgentValue(String bizType, String value) {
        if (!StoreBizTypeEnum.AGENT.name().equals(bizType) || CharSequenceUtil.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private void validateAgentRegionLevel(String agentLevel, String agentRegionId) {
        Region region = regionService.getById(agentRegionId);
        if (region == null || CharSequenceUtil.isBlank(region.getLevel())) {
            throw new ServiceException(ResultCode.AGENT_REGION_REQUIRED, "代理区域不存在或层级无效");
        }
        String expectedLevel = resolveAgentRegionLevel(agentLevel);
        if (!expectedLevel.equalsIgnoreCase(region.getLevel())) {
            throw new ServiceException(ResultCode.AGENT_REGION_REQUIRED,
                    buildAgentRegionLevelMessage(agentLevel, region.getLevel()));
        }
    }

    private String resolveAgentRegionLevel(String agentLevel) {
        return switch (AgentLevelEnum.valueOf(agentLevel)) {
            case CITY -> "city";
            case COUNTY -> "district";
            case TOWNSHIP -> "street";
            case WHOLESALER -> "district";
        };
    }

    private String buildAgentRegionLevelMessage(String agentLevel, String actualRegionLevel) {
        return switch (AgentLevelEnum.valueOf(agentLevel)) {
            case CITY -> "市级代理只能选择市级区域，当前选择了" + toRegionLevelLabel(actualRegionLevel);
            case COUNTY -> "区县代理只能选择区县级区域，当前选择了" + toRegionLevelLabel(actualRegionLevel);
            case TOWNSHIP -> "乡镇代理只能选择街道/乡镇级区域，当前选择了" + toRegionLevelLabel(actualRegionLevel);
            case WHOLESALER -> "批发商代理只能选择区县级区域，当前选择了" + toRegionLevelLabel(actualRegionLevel);
        };
    }

    private String toRegionLevelLabel(String regionLevel) {
        if (CharSequenceUtil.isBlank(regionLevel)) {
            return "未知层级";
        }
        return switch (regionLevel.trim().toLowerCase(Locale.ROOT)) {
            case "province" -> "省级";
            case "city" -> "市级";
            case "district" -> "区县级";
            case "street" -> "街道/乡镇级";
            case "country" -> "国家级";
            default -> regionLevel;
        };
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

    private void removeStoreCache(String storeId) {
        cache.remove(CachePrefix.STORE.getPrefix() + storeId);
    }
}
