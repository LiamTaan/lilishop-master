package cn.lili.controller.setting;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.message.entity.dto.MessageTemplateAggregateDTO;
import cn.lili.modules.message.service.NoticeMessageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户消息模板聚合分页（站内信 + 微信分表，scene_code 关联）。
 */
@RestController
@RequestMapping("/manager/setting/messageTemplate")
public class MessageTemplateManagerController {

    @Autowired
    private NoticeMessageService noticeMessageService;

    @GetMapping("/page")
    public ResultMessage<IPage<MessageTemplateAggregateDTO>> page(PageVO pageVO, String type) {
        return ResultUtil.data(noticeMessageService.getMessageTemplateAggregatePage(pageVO, type));
    }
}
