package cn.lili.controller.passport;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.entity.dto.ManagerMemberEditDTO;
import cn.lili.modules.member.entity.dto.MemberAddDTO;
import cn.lili.modules.member.entity.dto.MemberPointUpdateDTO;
import cn.lili.modules.member.entity.dto.MemberStatusUpdateDTO;
import cn.lili.modules.member.entity.enums.PointTypeEnum;
import cn.lili.modules.member.entity.vo.MemberSearchVO;
import cn.lili.modules.member.entity.vo.MemberVO;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.system.aspect.annotation.SystemLogPoint;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
/**
 * 管理端,客户接口
 *
 * @author Bulbasaur
 * @since 2020-02-25 14:10:16
 */
@RestController
@RequestMapping("/manager/passport/member")
public class MemberManagerController {
    @Autowired
    private MemberService memberService;

    @GetMapping
    public ResultMessage<IPage<MemberVO>> getByPage(MemberSearchVO memberSearchVO, PageVO page) {
        return ResultUtil.data(memberService.getMemberPage(memberSearchVO, page));
    }


    @GetMapping("/{id}")
    public ResultMessage<MemberVO> get(@PathVariable String id) {

        return ResultUtil.data(memberService.getMember(id));
    }

    @SystemLogPoint(description = "添加客户", customerLog = "'新增用户名称: ['+#member.username+']'")
    @PostMapping
    public ResultMessage<Member> save(@Valid @RequestBody MemberAddDTO member) {

        return ResultUtil.data(memberService.addMember(member));
    }

    @DemoSite
    @PreventDuplicateSubmissions
    @SystemLogPoint(description = "修改客户信息", customerLog = "'修改的用户名称: ['+#managerMemberEditDTO.username+']'")
    @PutMapping
    public ResultMessage<Member> update(@Valid @RequestBody ManagerMemberEditDTO managerMemberEditDTO) {
        return ResultUtil.data(memberService.updateMember(managerMemberEditDTO));
    }

    @DemoSite
    @PreventDuplicateSubmissions
    @SystemLogPoint(description = "修改客户状态", customerLog = "'修改的客户名称: ['+#memberIds+']，是否开启: ['+#disabled+']'")
    @PutMapping("/updateMemberStatus")
    public ResultMessage<Object> updateMemberStatus(@Valid @RequestBody MemberStatusUpdateDTO updateDTO) {
        memberService.updateMemberStatus(updateDTO.getMemberIds(), updateDTO.getDisabled());
        return ResultUtil.success();
    }


    @GetMapping("/num")
    public ResultMessage<Long> getByPage(MemberSearchVO memberSearchVO) {
        return ResultUtil.data(memberService.getMemberNum(memberSearchVO));
    }




    @PutMapping("/updateMemberPoint")
    public ResultMessage<Object> updateMemberPoint(@Valid @RequestBody MemberPointUpdateDTO updateDTO) {
        String content="";
        if (updateDTO.getType().equals(PointTypeEnum.INCREASE.name())) {
            content="运营后台手动增加积分:"+updateDTO.getPoint();
        }else{
            content="运营后台手动减少积分:"+updateDTO.getPoint();
        }
        if(memberService.updateMemberPoint(updateDTO.getPoint(), updateDTO.getType(), updateDTO.getMemberId(), content)){
            return ResultUtil.success();
        }
        return ResultUtil.error();
    }

}
