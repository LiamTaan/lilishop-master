package cn.lili.controller.sms;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.sms.entity.dos.SmsReach;
import cn.lili.modules.sms.service.SmsReachService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端,短信接口
 *
 * @author Bulbasaur
 * @since 2021/1/30 4:09 下午
 */
@RestController
@RequestMapping("/manager/sms/sms")
public class SmsManagerController {
    @Autowired
    private SmsReachService smsReachService;

    @PostMapping
    public ResultMessage<Object> sendBatchSms(SmsReach smsReach, List<String> mobile) {
        smsReachService.addSmsReach(smsReach,mobile);
        return ResultUtil.success();
    }

    @GetMapping()
    public ResultMessage<IPage<SmsReach>> querySmsReachPage(PageVO page) {
        return ResultUtil.data(smsReachService.page(PageUtil.initPage(page)));
    }

    @GetMapping("/{id}")
    public ResultMessage<SmsReach> querySmsReach(@PathVariable String id) {
        return ResultUtil.data(smsReachService.getById(id));
    }

}
