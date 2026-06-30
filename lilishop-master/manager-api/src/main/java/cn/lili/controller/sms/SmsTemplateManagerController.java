package cn.lili.controller.sms;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.sms.entity.dos.SmsTemplate;
import cn.lili.modules.sms.service.SmsTemplateService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理端,短信模板接口
 *
 * @author Bulbasaur
 * @since 2021/1/30 4:09 下午
 */
@RestController
@RequestMapping("/manager/sms/template")
public class SmsTemplateManagerController {
    @Autowired
    private SmsTemplateService smsTemplateService;

    @PostMapping
    @DemoSite
    public ResultMessage<SmsTemplate> save(@Valid SmsTemplate smsTemplate) {
        smsTemplateService.addSmsTemplate(smsTemplate);
        return ResultUtil.success();
    }

    @DeleteMapping
    @DemoSite
    public ResultMessage<SmsTemplate> delete(String templateCode) {
        smsTemplateService.deleteSmsTemplate(templateCode);
        return ResultUtil.success();
    }

    @PutMapping("/querySmsSign")
    @DemoSite
    public ResultMessage<SmsTemplate> querySmsSign() {
        smsTemplateService.querySmsTemplate();
        return ResultUtil.success();
    }

    @PutMapping("/modifySmsTemplate")
    @DemoSite
    public ResultMessage<SmsTemplate> modifySmsTemplate(@Valid SmsTemplate smsTemplate) {
        smsTemplateService.modifySmsTemplate(smsTemplate);
        return ResultUtil.success();
    }

    @GetMapping("/querySmsTemplatePage")
    public ResultMessage<IPage<SmsTemplate>> querySmsTemplatePage(PageVO page, Integer templateStatus) {
        return ResultUtil.data(smsTemplateService.page(page,templateStatus));
    }
}
