package cn.lili.controller.wechat;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.wechat.entity.dos.WechatMessage;
import cn.lili.modules.wechat.service.WechatMessageService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


/**
 * 管理端,微信消息接口
 *
 * @author Chopper
 * @since 2020/12/2 10:40
 */
@RestController
@RequestMapping("/manager/wechat/wechatMessage")
public class WechatMessageManageController {
    @Autowired
    private WechatMessageService wechatMessageService;


    @GetMapping("/init")
    @DemoSite
    public ResultMessage init() {
        wechatMessageService.init();
        return ResultUtil.success();
    }

    @GetMapping("/{id}")
    public ResultMessage<WechatMessage> get(@PathVariable String id) {

        WechatMessage wechatMessage = wechatMessageService.getById(id);
        return ResultUtil.data(wechatMessage);
    }

    @GetMapping
    public ResultMessage<IPage<WechatMessage>> getByPage(PageVO page) {
        IPage<WechatMessage> data = wechatMessageService.page(PageUtil.initPage(page));
        return ResultUtil.data(data);
    }

    @DemoSite
    @PostMapping
    public ResultMessage<WechatMessage> save(@RequestBody WechatMessage wechatMessage) {

        wechatMessageService.save(wechatMessage);
        return ResultUtil.data(wechatMessage);
    }

    @DemoSite
    @PutMapping("/{id}")
    public ResultMessage<WechatMessage> update(@PathVariable String id, @RequestBody WechatMessage wechatMessage) {
        wechatMessage.setId(id);
        // #region agent log: wechat message update entry
        try {
            String logPath = "d:\\lilishop_source\\lilishop\\debug-ade6ce.log";
            String msg = "{"
                    + "\"sessionId\":\"ade6ce\","
                    + "\"runId\":\"toggleWechatOaClose\","
                    + "\"hypothesisId\":\"H_UPDATE_WECHAT_MESSAGE_ENTRY\","
                    + "\"location\":\"WechatMessageManageController:update\","
                    + "\"message\":\"wechatMessage update payload\","
                    + "\"data\":{\"pathId\":\"" + (id == null ? "" : id) + "\","
                    + "\"bodyId\":\"" + (wechatMessage == null ? "" : wechatMessage.getId()) + "\","
                    + "\"enable\":\"" + (wechatMessage == null || wechatMessage.getEnable() == null ? "" : wechatMessage.getEnable()) + "\","
                    + "\"updateTimeIsNull\":\"" + (wechatMessage == null || wechatMessage.getUpdateTime() == null) + "\""
                    + "},"
                    + "\"timestamp\":" + System.currentTimeMillis()
                    + "}\n";
            Files.write(Path.of(logPath), msg.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignore) {
        }
        // #endregion
        wechatMessageService.updateById(wechatMessage);
        return ResultUtil.data(wechatMessage);
    }

    @DemoSite
    @DeleteMapping("/{ids}")
    public ResultMessage<Object> delAllByIds(@PathVariable List ids) {
        wechatMessageService.removeByIds(ids);
        return ResultUtil.success();
    }
}
    
