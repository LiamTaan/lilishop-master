package cn.lili.controller.sms;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.sms.entity.dos.SmsSign;
import cn.lili.modules.sms.service.SmsSignService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理端,短信签名接口
 *
 * @author Chopper
 * @since 2021/1/30 4:09 下午
 */
@RestController
@RequestMapping("/manager/sms/sign")
public class SmsSignManagerController {
    @Autowired
    private SmsSignService smsSignService;


    @PostMapping
    public ResultMessage<SmsSign> save(@Valid SmsSign smsSign) {
        smsSignService.addSmsSign(smsSign);
        return ResultUtil.success();
    }

    @DeleteMapping("/{id}")
    public ResultMessage<SmsSign> delete(@PathVariable String id) {
        smsSignService.deleteSmsSign(id);
        return ResultUtil.success();
    }


    @GetMapping("/{id}")
    public ResultMessage<SmsSign> getDetail(@PathVariable String id) {
        return ResultUtil.data(smsSignService.getById(id));
    }

    @PutMapping("/querySmsSign")
    public ResultMessage<SmsSign> querySmsSign() {
        smsSignService.querySmsSign();
        return ResultUtil.success();
    }

    @PutMapping("/modifySmsSign")
    public ResultMessage<SmsSign> modifySmsSign(@Valid SmsSign smsSign) {
        smsSignService.modifySmsSign(smsSign);
        return ResultUtil.success();
    }

    @GetMapping("/querySmsSignPage")
    public ResultMessage<IPage<SmsSign>> querySmsSignPage(PageVO page, Integer signStatus) {
        return ResultUtil.data(smsSignService.page(page, signStatus));
    }

}
