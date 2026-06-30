package cn.lili.controller.member;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dto.EvaluationQueryParams;
import cn.lili.modules.member.entity.vo.MemberEvaluationListVO;
import cn.lili.modules.member.entity.vo.MemberEvaluationVO;
import cn.lili.modules.member.service.MemberEvaluationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

/**
 * 管理端,客户商品评价接口
 *
 * @author Bulbasaur
 * @since 2020-02-25 14:10:16
 */
@RestController
@RequestMapping("/manager/member/evaluation")
public class MemberEvaluationManagerController {
    @Autowired
    private MemberEvaluationService memberEvaluationService;

    @PreventDuplicateSubmissions
    @GetMapping("/get/{id}")
    public ResultMessage<MemberEvaluationVO> get(@PathVariable String id) {

        return ResultUtil.data(memberEvaluationService.queryById(id));
    }

    @GetMapping("/getByPage")
    public ResultMessage<IPage<MemberEvaluationListVO>> getByPage(EvaluationQueryParams evaluationQueryParams, PageVO page) {

        return ResultUtil.data(memberEvaluationService.queryPage(evaluationQueryParams));
    }

    @PreventDuplicateSubmissions
    @GetMapping("/updateStatus/{id}")
    public ResultMessage<Object> updateStatus(@PathVariable String id, @NotNull String status) {
        memberEvaluationService.updateStatus(id, status);
        return ResultUtil.success();
    }

    @PreventDuplicateSubmissions
    @PutMapping("/updateTop/{id}")
    public ResultMessage<Object> updateTop(@PathVariable String id, @NotNull Boolean top) {
        memberEvaluationService.updateTop(id, top);
        return ResultUtil.success();
    }

    @PutMapping("/delete/{id}")
    public ResultMessage<IPage<Object>> delete(@PathVariable String id) {
        memberEvaluationService.delete(id);
        return ResultUtil.success();
    }

}
