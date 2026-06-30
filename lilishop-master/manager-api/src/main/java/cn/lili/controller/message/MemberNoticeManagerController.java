package cn.lili.controller.message;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.MemberNotice;
import cn.lili.modules.member.service.MemberNoticeService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端,客户站内信管理接口
 *
 * @author Chopper
 * @since 2020/11/17 4:31 下午
 */
@RestController
@RequestMapping("/manager/message/memberNotice")
public class MemberNoticeManagerController {
    @Autowired
    private MemberNoticeService memberNoticeService;

    @GetMapping("/{id}")
    public ResultMessage<MemberNotice> get(@PathVariable String id) {
        MemberNotice memberNotice = memberNoticeService.getById(id);
        return ResultUtil.data(memberNotice);
    }

    @GetMapping("/page")
    public ResultMessage<IPage<MemberNotice>> getByPage(
            PageVO page) {
        IPage<MemberNotice> data = memberNoticeService.page(PageUtil.initPage(page));
        return ResultUtil.data(data);
    }

    @PostMapping("/read/{ids}")
    public ResultMessage<Object> read(@PathVariable List<String> ids) {
        memberNoticeService.read(ids);
        return ResultUtil.success();
    }

    @PostMapping("/read/all")
    public ResultMessage<Object> readAll() {
        memberNoticeService.readAll(UserContext.getCurrentUser().getId());
        return ResultUtil.success();
    }

    @DeleteMapping("/{ids}")
    public ResultMessage<Object> delAllByIds(@PathVariable List<String> ids) {
        memberNoticeService.removeByIds(ids);
        return ResultUtil.success();
    }

    @DeleteMapping
    public ResultMessage<Object> deleteAll() {
        memberNoticeService.removeAll(UserContext.getCurrentUser().getId());
        return ResultUtil.success();
    }

}
