package cn.lili.controller.other;

import cn.lili.mybatis.util.PageUtil;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.page.entity.dos.Feedback;
import cn.lili.modules.page.service.FeedbackService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端,意见反馈接口
 *
 * @author Bulbasaur
 * @since 2020-05-5 15:10:16
 */
@RestController
@RequestMapping("/manager/other/feedback")
public class FeedbackManagerController {

    /**
     * 意见反馈
     */
    @Autowired
    private FeedbackService feedbackService;

    @GetMapping()
    public ResultMessage<IPage<Feedback>> page(PageVO pageVO) {
        return ResultUtil.data(feedbackService.page(PageUtil.initPage(pageVO)));
    }

    @GetMapping("/{id}")
    public ResultMessage<Feedback> getFeedback(
 @PathVariable String id) {
        return ResultUtil.data(this.feedbackService.getById(id));
    }

}
