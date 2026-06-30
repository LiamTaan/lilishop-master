package cn.lili.controller.member;
 
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.MemberPointsHistory;
import cn.lili.modules.member.entity.vo.MemberPointsHistoryVO;
import cn.lili.modules.member.entity.vo.MemberPointsStatisticsVO;
import cn.lili.modules.member.service.MemberPointsHistoryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端,客户积分历史接口
 *
 * @author Bulbasaur
 * @since 2020-02-25 14:10:16
 */
@RestController
@RequestMapping("/manager/member/memberPointsHistory")
public class MemberPointsHistoryManagerController {
    @Autowired
    private MemberPointsHistoryService memberPointsHistoryService;

    @GetMapping("/getByPage")
    public ResultMessage<IPage<MemberPointsHistory>> getByPage(PageVO page, String memberId, String memberName) {
        return ResultUtil.data(memberPointsHistoryService.MemberPointsHistoryList(page, memberId, memberName));
    }

    @GetMapping("/getMemberPointsHistoryVO")
    public ResultMessage<MemberPointsHistoryVO> getMemberPointsHistoryVO(String memberId) {
        return ResultUtil.data(memberPointsHistoryService.getMemberPointsHistoryVO(memberId));
    }

    @GetMapping("/queryMemberPointsStatistics")
    public ResultMessage<MemberPointsStatisticsVO> queryMemberPointsStatistics() {
        return ResultUtil.data(memberPointsHistoryService.queryMemberPointsStatistics());
    }




}
