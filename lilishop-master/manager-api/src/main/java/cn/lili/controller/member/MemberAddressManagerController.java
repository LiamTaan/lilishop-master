package cn.lili.controller.member;

import cn.hutool.core.bean.BeanUtil;
import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.MemberAddress;
import cn.lili.modules.member.entity.dto.MemberAddressManagerSaveDTO;
import cn.lili.modules.member.entity.dto.MemberAddressQueryDTO;
import cn.lili.modules.member.entity.vo.MemberAddressManagerVO;
import cn.lili.modules.member.service.MemberAddressService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理端,客户地址API
 *
 * @author Bulbasaur
 * @since 2020-02-25 14:10:16
 */
@RestController
@RequestMapping("/manager/member/address")
public class MemberAddressManagerController {
    @Autowired
    private MemberAddressService memberAddressService;

    @GetMapping
    public ResultMessage<IPage<MemberAddressManagerVO>> getByPage(PageVO page, MemberAddressQueryDTO queryDTO) {
        return ResultUtil.data(memberAddressService.queryAddressPage(page, queryDTO));
    }

    @GetMapping("/{memberId}")
    public ResultMessage<IPage<MemberAddress>> getByPage(PageVO page, @PathVariable("memberId") String memberId) {
        return ResultUtil.data(memberAddressService.getAddressByMember(page, memberId));
    }

    @PreventDuplicateSubmissions
    @DeleteMapping("/delById/{id}")
    public ResultMessage<Object> delShippingAddressById(@PathVariable String id) {
        memberAddressService.removeMemberAddress(id);
        return ResultUtil.success();
    }

    @PutMapping
    public ResultMessage<MemberAddress> editShippingAddress(@Valid @RequestBody MemberAddressManagerSaveDTO shippingAddress) {
        //修改客户地址
        return ResultUtil.data(memberAddressService.updateMemberAddress(toMemberAddress(shippingAddress)));
    }

    @PreventDuplicateSubmissions
    @PostMapping
    public ResultMessage<MemberAddress> addShippingAddress(@Valid @RequestBody MemberAddressManagerSaveDTO shippingAddress) {
        //添加客户地址
        return ResultUtil.data(memberAddressService.saveMemberAddress(toMemberAddress(shippingAddress)));
    }

    private MemberAddress toMemberAddress(MemberAddressManagerSaveDTO dto) {
        return BeanUtil.copyProperties(dto, MemberAddress.class);
    }

}
