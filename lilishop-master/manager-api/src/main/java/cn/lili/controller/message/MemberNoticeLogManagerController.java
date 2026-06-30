package cn.lili.controller.message;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.MemberNoticeLog;
import cn.lili.modules.member.service.MemberNoticeLogService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端,客户消息接口
 *
 * @author Chopper
 * @since 2020-02-25 14:10:16
 */
@RestController
@RequestMapping("/manager/message/memberNoticeLog")
public class MemberNoticeLogManagerController {
    @Autowired
    private MemberNoticeLogService memberNoticeLogService;

    @GetMapping("/get/{id}")
    public ResultMessage<MemberNoticeLog> get(@PathVariable String id) {
        MemberNoticeLog memberNoticeLog = memberNoticeLogService.getById(id);
        return ResultUtil.data(memberNoticeLog);
    }

    @GetMapping("/getAll")
    public ResultMessage<List<MemberNoticeLog>> getAll() {
        List<MemberNoticeLog> list = memberNoticeLogService.list();
        return ResultUtil.data(list);
    }

    @GetMapping("/getByPage")
    public ResultMessage<IPage<MemberNoticeLog>> getByPage(PageVO page) {
        IPage<MemberNoticeLog> data = memberNoticeLogService.page(PageUtil.initPage(page));
        return ResultUtil.data(data);
    }

    @PostMapping("/insertOrUpdate")
    public ResultMessage<MemberNoticeLog> saveOrUpdate(@RequestBody MemberNoticeLog memberNoticeLog) {
        memberNoticeLogService.saveOrUpdate(memberNoticeLog);
        return ResultUtil.data(memberNoticeLog);
    }

    @DeleteMapping("/delByIds/{ids}")
    public ResultMessage<Object> delAllByIds(@PathVariable List<String> ids) {
        memberNoticeLogService.removeByIds(ids);
        return ResultUtil.success();
    }
}
