package cn.lili.controller.member;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.MemberBenefit;
import cn.lili.modules.member.entity.dto.MemberBenefitStateUpdateDTO;
import cn.lili.modules.member.entity.vo.EnumValueVO;
import cn.lili.modules.member.entity.vo.MemberBenefitDetailVO;
import cn.lili.modules.member.entity.vo.MemberBenefitGrantRecordVO;
import cn.lili.modules.member.service.MemberBenefitService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manager/member/benefit")
public class MemberBenefitManagerController {

    @Autowired
    private MemberBenefitService memberBenefitService;

    @GetMapping("/get/{id}")
    public ResultMessage<MemberBenefitDetailVO> get(@PathVariable String id) {
        return ResultUtil.data(memberBenefitService.getManagerBenefitDetail(id));
    }

    @GetMapping("/getByPage")
    public ResultMessage<IPage<MemberBenefit>> getByPage(@RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) String state,
                                                         PageVO page) {
        return ResultUtil.data(memberBenefitService.getBenefitPage(keyword, state, page));
    }

    @GetMapping("/grantRecords/getByPage")
    public ResultMessage<IPage<MemberBenefitGrantRecordVO>> grantRecordsGetByPage(
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String gradeId,
            PageVO page) {
        return ResultUtil.data(memberBenefitService.pageBenefitGrantRecords(mobile, gradeId, page));
    }

    @GetMapping("/list")
    public ResultMessage<List<MemberBenefit>> list(@RequestParam(required = false) Boolean onlyOpen) {
        return ResultUtil.data(memberBenefitService.listBenefits(onlyOpen));
    }

    @GetMapping("/types")
    public ResultMessage<List<EnumValueVO>> types() {
        return ResultUtil.data(memberBenefitService.listBenefitTypes());
    }

    @PostMapping
    public ResultMessage<Object> add(@Validated @RequestBody MemberBenefit memberBenefit) {
        memberBenefitService.saveBenefit(memberBenefit);
        return ResultUtil.success(ResultCode.SUCCESS);
    }

    @PutMapping("/update/{id}")
    public ResultMessage<Object> update(@PathVariable String id, @Validated @RequestBody MemberBenefit memberBenefit) {
        memberBenefitService.updateBenefit(id, memberBenefit);
        return ResultUtil.success(ResultCode.SUCCESS);
    }

    @DeleteMapping("/delete/{id}")
    public ResultMessage<Object> delete(@PathVariable String id) {
        memberBenefitService.deleteBenefit(id);
        return ResultUtil.success(ResultCode.SUCCESS);
    }

    @PutMapping("/state/{id}")
    public ResultMessage<Object> updateState(@PathVariable String id, @RequestBody @jakarta.validation.Valid MemberBenefitStateUpdateDTO updateDTO) {
        memberBenefitService.updateBenefitState(id, updateDTO.getState());
        return ResultUtil.success(ResultCode.SUCCESS);
    }
}
