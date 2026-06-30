package cn.lili.controller.message;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.common.vo.SearchVO;
import cn.lili.modules.member.entity.dos.MemberNoticeSenter;
import cn.lili.modules.member.service.MemberNoticeSenterService;

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
@RequestMapping("/manager/message/memberNoticeSenter")
public class MemberNoticeSenterManagerController {
    @Autowired
    private MemberNoticeSenterService memberNoticeSenterService;

    @GetMapping("/get/{id}")
    public ResultMessage<MemberNoticeSenter> get(@PathVariable String id) {
        MemberNoticeSenter memberNoticeSenter = memberNoticeSenterService.getById(id);
        return ResultUtil.data(memberNoticeSenter);
    }

    @GetMapping("/getAll")
    public ResultMessage<List<MemberNoticeSenter>> getAll() {

        List<MemberNoticeSenter> list = memberNoticeSenterService.list();
        return ResultUtil.data(list);
    }

    @GetMapping("/getByPage")
    public ResultMessage<IPage<MemberNoticeSenter>> getByPage(MemberNoticeSenter entity,
                                                              SearchVO searchVo,
                                                              PageVO page) {
        return ResultUtil.data(memberNoticeSenterService.getByPage(entity, searchVo, page));
    }

    @PostMapping("/insertOrUpdate")
    public ResultMessage<MemberNoticeSenter> saveOrUpdate(@RequestBody MemberNoticeSenter memberNoticeSenter) {

        memberNoticeSenterService.customSave(memberNoticeSenter);
        return ResultUtil.data(memberNoticeSenter);
    }

    @DeleteMapping("/delByIds/{ids}")
    public ResultMessage<Object> delAllByIds(@PathVariable List<String> ids) {
        validateIds(ids);
        memberNoticeSenterService.removeByIds(ids);
        return ResultUtil.success();
    }

    private void validateIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
        for (String id : ids) {
            if (id == null || !id.matches("\\d+")) {
                throw new ServiceException(ResultCode.PARAMS_ERROR);
            }
        }
    }
}
