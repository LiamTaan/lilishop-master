package cn.lili.controller.store;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.dto.StoreAdminSaveDTO;
import cn.lili.modules.store.entity.dto.StoreAuditDTO;
import cn.lili.modules.store.entity.dto.StoreEditDTO;
import cn.lili.modules.store.entity.dto.StoreSettlementProfileDTO;
import cn.lili.modules.store.entity.vos.StoreAuditLogVO;
import cn.lili.modules.store.entity.vos.StoreAuditSummaryVO;
import cn.lili.modules.store.entity.vos.StoreDetailVO;
import cn.lili.modules.store.entity.vos.StoreManagementCategoryVO;
import cn.lili.modules.store.entity.vos.StoreSearchParams;
import cn.lili.modules.store.entity.vos.StoreVO;
import cn.lili.modules.store.service.StoreDetailService;
import cn.lili.modules.store.service.StoreAuditLogService;
import cn.lili.modules.store.service.StoreSettlementProfileService;
import cn.lili.modules.store.service.StoreService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 管理端,店铺管理接口
 *
 * @author Bulbasaur
 * @since 2020/12/6 16:09
 */
@RestController
@RequestMapping("/manager/store/store")
public class StoreManagerController {

    /**
     * 店铺
     */
    @Autowired
    private StoreService storeService;
    /**
     * 店铺详情
     */
    @Autowired
    private StoreDetailService storeDetailService;

    @Autowired
    private StoreAuditLogService storeAuditLogService;

    @Autowired
    private StoreSettlementProfileService storeSettlementProfileService;

    @GetMapping("/all")
    public ResultMessage<List<Store>> getAll() {
        return ResultUtil.data(storeService.listOpenStores());
    }

    @GetMapping
    public ResultMessage<IPage<StoreVO>> getByPage(StoreSearchParams entity, PageVO page) {
        return ResultUtil.data(storeService.findByConditionPage(entity, page));
    }

    @GetMapping("/summary")
    public ResultMessage<StoreAuditSummaryVO> summary() {
        return ResultUtil.data(storeService.managementSummary());
    }

    @GetMapping("/get/detail/{storeId}")
    public ResultMessage<StoreDetailVO> detail(@PathVariable String storeId) {
        return ResultUtil.data(storeDetailService.getStoreDetailVOFresh(storeId));
    }

    @PostMapping("/add")
    public ResultMessage<Store> add(@Valid @RequestBody StoreAdminSaveDTO storeAdminSaveDTO) {
        return ResultUtil.data(storeService.add(storeAdminSaveDTO));
    }

    @PutMapping("/edit/{id}")
    public ResultMessage<Store> edit(@PathVariable String id, @Valid @RequestBody StoreEditDTO storeEditDTO) {
        storeEditDTO.setStoreId(id);
        return ResultUtil.data(storeService.edit(storeEditDTO));
    }

    @GetMapping("/settlementProfile/{storeId}")
    public ResultMessage<StoreSettlementProfileDTO> settlementProfile(@PathVariable String storeId) {
        return ResultUtil.data(storeSettlementProfileService.getProfile(storeId));
    }

    @PutMapping("/settlementProfile/{storeId}")
    public ResultMessage<Object> settlementProfile(@PathVariable String storeId,
                                                   @Valid @RequestBody StoreSettlementProfileDTO storeSettlementProfileDTO) {
        return ResultUtil.data(storeSettlementProfileService.saveOrUpdateProfile(storeId, storeSettlementProfileDTO));
    }

    @PutMapping("/audit/{id}/{passed}")
    public ResultMessage<Object> audit(@PathVariable String id, @PathVariable Integer passed) {
        storeService.audit(id, passed);
        return ResultUtil.success();
    }

    @PutMapping("/audit/{id}")
    public ResultMessage<Object> audit(@PathVariable String id, @RequestBody StoreAuditDTO auditDTO) {
        storeService.audit(id, auditDTO);
        return ResultUtil.success();
    }

    @GetMapping("/audit/log/{storeId}")
    public ResultMessage<List<StoreAuditLogVO>> auditLog(@PathVariable String storeId) {
        return ResultUtil.data(storeAuditLogService.listByStoreId(storeId));
    }

    @DemoSite
    @PutMapping("/disable/{id}")
    public ResultMessage<Store> disable(@PathVariable String id) {
        storeService.disable(id);
        return ResultUtil.success();
    }

    @PutMapping("/enable/{id}")
    public ResultMessage<Store> enable(@PathVariable String id) {
        storeService.enable(id);
        return ResultUtil.success();
    }

    @GetMapping("/managementCategory/{storeId}")
    public ResultMessage<List<StoreManagementCategoryVO>> firstCategory(@PathVariable String storeId) {
        return ResultUtil.data(this.storeDetailService.goodsManagementCategory(storeId));
    }


    @GetMapping("/{memberId}/member")
    public ResultMessage<Store> getByMemberId(@Valid @PathVariable String memberId) {
        return ResultUtil.data(storeService.getStoreByMemberId(memberId));
    }

    @PostMapping("store/to/clerk")
    public ResultMessage<Object> storeToClerk(){
        this.storeService.storeToClerk();
        return ResultUtil.success();
    }
}
