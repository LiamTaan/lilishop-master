package cn.lili.controller.store;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.vos.StoreGoodsLabelVO;
import cn.lili.modules.goods.service.StoreGoodsLabelService;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.dto.StoreApplyTypeSelectDTO;
import cn.lili.modules.store.entity.dto.StoreCompanyAuthorizedApplyDTO;
import cn.lili.modules.store.entity.dto.StoreCompanyLegalApplyDTO;
import cn.lili.modules.store.entity.dto.StoreIndividualApplyDTO;
import cn.lili.modules.store.entity.dto.StorePersonalApplyDTO;
import cn.lili.modules.store.entity.vos.*;
import cn.lili.modules.store.service.StoreDetailService;
import cn.lili.modules.store.service.StoreService;
import cn.lili.modules.store.support.BuyerStoreScopeSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;


/**
 * 买家端,店铺接口
 *
 * @author Bulbasaur
 * @since 2020/11/17 2:32 下午
 */
@RestController
@RequestMapping("/buyer/store/store")
@Tag(name = "买家端,店铺接口")
public class StoreBuyerController {

    /**
     * 店铺
     */
    @Autowired
    private StoreService storeService;
    /**
     * 店铺商品分类
     */
    @Autowired
    private StoreGoodsLabelService storeGoodsLabelService;
    /**
     * 店铺详情
     */
    @Autowired
    private StoreDetailService storeDetailService;

    @Autowired
    private BuyerStoreScopeSupport buyerStoreScopeSupport;

    @Operation(summary = "获取店铺列表分页")
    @GetMapping
    public ResultMessage<IPage<StoreVO>> getByPage(StoreSearchParams entity, PageVO page) {
        buyerStoreScopeSupport.applyStoreScope(entity);
        return ResultUtil.data(storeService.findByConditionPage(entity, page));
    }

    @GetMapping("/store")
    @Operation(summary = "im-获取店铺信息")
    public ResultMessage<Store> getStoreUser() {
        AuthUser authUser = UserContext.getCurrentUser();
        return ResultUtil.data(storeService.getById(authUser.getStoreId()));
    }

    @GetMapping("/store/{storeId}")
    @Parameter(name = "storeId", description = "店铺Id", required = true)
    @Operation(summary = "im-店铺ID获取店铺信息")
    public ResultMessage<Store> getStoreUserDetail(@PathVariable String storeId) {
        return ResultUtil.data(storeService.getById(storeId));
    }

    @Operation(summary = "通过id获取店铺信息")
    @Parameter(name = "id", description = "店铺ID", required = true)
    @GetMapping("/get/detail/{id}")
    public ResultMessage<StoreBasicInfoVO> detail(@NotNull @PathVariable String id) {
        return ResultUtil.data(storeDetailService.getStoreBasicInfoDTO(id));
    }

    @Operation(summary = "通过id获取店铺详细信息-营业执照")
    @Parameter(name = "id", description = "店铺ID", required = true)
    @GetMapping("/get/licencePhoto/{id}")
    public ResultMessage<StoreOtherVO> licencePhoto(@NotNull @PathVariable String id) {
        return ResultUtil.data(storeDetailService.getStoreOtherVO(id));
    }

    @Operation(summary = "通过id获取店铺商品分类")
    @Parameters({
            @Parameter(name = "id", description = "店铺ID", required = true)
    })
    @GetMapping("/label/get/{id}")
    public ResultMessage<List<StoreGoodsLabelVO>> storeGoodsLabel(@NotNull @PathVariable String id) {
        return ResultUtil.data(storeGoodsLabelService.listByStoreId(id));
    }

    @Operation(summary = "选择入驻主体类型")
    @PutMapping("/apply/type")
    public ResultMessage<Object> selectApplyType(@Valid @RequestBody StoreApplyTypeSelectDTO applyTypeSelectDTO,
                                                 @RequestHeader(value = "loginSessionToken", required = false) String loginSessionToken) {
        storeService.selectApplyType(applyTypeSelectDTO, loginSessionToken);
        return ResultUtil.success();
    }

    @Operation(summary = "选择供货商/批发商入驻主体类型")
    @PutMapping("/apply/supplier/type")
    public ResultMessage<Object> selectSupplierApplyType(@Valid @RequestBody StoreApplyTypeSelectDTO applyTypeSelectDTO,
                                                         @RequestHeader(value = "loginSessionToken", required = false) String loginSessionToken) {
        storeService.selectSupplierApplyType(applyTypeSelectDTO, loginSessionToken);
        return ResultUtil.success();
    }

    @Operation(summary = "个人主体提交入驻资料")
    @PutMapping("/apply/personal")
    public ResultMessage<Object> applyPersonal(@Valid @RequestBody StorePersonalApplyDTO personalApplyDTO,
                                               @RequestHeader String uuid,
                                               @RequestHeader(value = "loginSessionToken", required = false) String loginSessionToken) {
        storeService.applyPersonal(personalApplyDTO, uuid, loginSessionToken);
        return ResultUtil.success();
    }

    @Operation(summary = "个人主体提交供货商/批发商入驻资料")
    @PutMapping("/apply/supplier/personal")
    public ResultMessage<Object> applySupplierPersonal(@Valid @RequestBody StorePersonalApplyDTO personalApplyDTO,
                                                       @RequestHeader String uuid,
                                                       @RequestHeader(value = "loginSessionToken", required = false) String loginSessionToken) {
        storeService.applySupplierPersonal(personalApplyDTO, uuid, loginSessionToken);
        return ResultUtil.success();
    }

    @Operation(summary = "个体户提交入驻资料")
    @PutMapping("/apply/individual")
    public ResultMessage<Object> applyIndividual(@Valid @RequestBody StoreIndividualApplyDTO individualApplyDTO,
                                                 @RequestHeader String uuid,
                                                 @RequestHeader(value = "loginSessionToken", required = false) String loginSessionToken) {
        storeService.applyIndividual(individualApplyDTO, uuid, loginSessionToken);
        return ResultUtil.success();
    }

    @Operation(summary = "个体户提交供货商/批发商入驻资料")
    @PutMapping("/apply/supplier/individual")
    public ResultMessage<Object> applySupplierIndividual(@Valid @RequestBody StoreIndividualApplyDTO individualApplyDTO,
                                                         @RequestHeader String uuid,
                                                         @RequestHeader(value = "loginSessionToken", required = false) String loginSessionToken) {
        storeService.applySupplierIndividual(individualApplyDTO, uuid, loginSessionToken);
        return ResultUtil.success();
    }

    @Operation(summary = "企业法人提交入驻资料")
    @PutMapping("/apply/company/legal")
    public ResultMessage<Object> applyCompanyLegal(@Valid @RequestBody StoreCompanyLegalApplyDTO companyLegalApplyDTO,
                                                   @RequestHeader String uuid,
                                                   @RequestHeader(value = "loginSessionToken", required = false) String loginSessionToken) {
        storeService.applyCompanyLegal(companyLegalApplyDTO, uuid, loginSessionToken);
        return ResultUtil.success();
    }

    @Operation(summary = "企业法人提交供货商/批发商入驻资料")
    @PutMapping("/apply/supplier/company/legal")
    public ResultMessage<Object> applySupplierCompanyLegal(@Valid @RequestBody StoreCompanyLegalApplyDTO companyLegalApplyDTO,
                                                           @RequestHeader String uuid,
                                                           @RequestHeader(value = "loginSessionToken", required = false) String loginSessionToken) {
        storeService.applySupplierCompanyLegal(companyLegalApplyDTO, uuid, loginSessionToken);
        return ResultUtil.success();
    }

    @Operation(summary = "企业被授权人提交入驻资料")
    @PutMapping("/apply/company/authorized")
    public ResultMessage<Object> applyCompanyAuthorized(@Valid @RequestBody StoreCompanyAuthorizedApplyDTO companyAuthorizedApplyDTO,
                                                        @RequestHeader String uuid,
                                                        @RequestHeader(value = "loginSessionToken", required = false) String loginSessionToken) {
        storeService.applyCompanyAuthorized(companyAuthorizedApplyDTO, uuid, loginSessionToken);
        return ResultUtil.success();
    }

    @Operation(summary = "企业被授权人提交供货商/批发商入驻资料")
    @PutMapping("/apply/supplier/company/authorized")
    public ResultMessage<Object> applySupplierCompanyAuthorized(@Valid @RequestBody StoreCompanyAuthorizedApplyDTO companyAuthorizedApplyDTO,
                                                                @RequestHeader String uuid,
                                                                @RequestHeader(value = "loginSessionToken", required = false) String loginSessionToken) {
        storeService.applySupplierCompanyAuthorized(companyAuthorizedApplyDTO, uuid, loginSessionToken);
        return ResultUtil.success();
    }

    @Operation(summary = "获取当前登录客户的店铺信息-入驻店铺")
    @GetMapping("/apply")
    public ResultMessage<StoreApplyVO> apply() {
        return ResultUtil.data(storeDetailService.getStoreApplyVOByMemberId(UserContext.getCurrentUser().getId()));
    }
}
