package cn.lili.controller.wechat;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.common.vo.SearchVO;
import cn.lili.modules.wechat.entity.dos.WechatMPMessage;
import cn.lili.modules.wechat.service.WechatMPMessageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


/**
 * @author Chopper
 */
@RestController
@RequestMapping("/manager/wechat/wechatMPMessage")
public class WechatMPMessageManagerController {
    @Autowired
    private WechatMPMessageService wechatMPMessageService;

    @DemoSite
    @GetMapping("/init")
    public ResultMessage init() {
        wechatMPMessageService.init();
        return ResultUtil.success();
    }

    @GetMapping("/{id}")
    public ResultMessage<WechatMPMessage> get(@PathVariable String id) {

        WechatMPMessage wechatMPMessage = wechatMPMessageService.getById(id);
        return ResultUtil.data(wechatMPMessage);
    }

    @GetMapping
    public ResultMessage<IPage<WechatMPMessage>> getByPage(WechatMPMessage entity,
                                                           SearchVO searchVo,
                                                           PageVO page) {
        IPage<WechatMPMessage> data = wechatMPMessageService.getByPage(entity, searchVo, page);
        return new ResultUtil<IPage<WechatMPMessage>>().setData(data);
    }

    @DemoSite
    @PostMapping
    public ResultMessage<WechatMPMessage> save(@RequestBody WechatMPMessage wechatMPMessage) {

        wechatMPMessageService.save(wechatMPMessage);
        return ResultUtil.data(wechatMPMessage);
    }

    @DemoSite
    @PutMapping("/{id}")
    public ResultMessage<WechatMPMessage> update(@PathVariable String id, @RequestBody WechatMPMessage wechatMPMessage) {
        wechatMPMessage.setId(id);
        // #region agent log: wechat mp message update entry
        try {
            String logPath = "d:\\lilishop_source\\lilishop\\debug-ade6ce.log";
            String msg = "{"
                    + "\"sessionId\":\"ade6ce\","
                    + "\"runId\":\"toggleWechatMpClose\","
                    + "\"hypothesisId\":\"H_UPDATE_WECHAT_MP_ENTRY\","
                    + "\"location\":\"WechatMPMessageManagerController:update\","
                    + "\"message\":\"wechatMPMessage update payload\","
                    + "\"data\":{\"pathId\":\"" + (id == null ? "" : id) + "\","
                    + "\"bodyId\":\"" + (wechatMPMessage == null ? "" : wechatMPMessage.getId()) + "\","
                    + "\"enable\":\"" + (wechatMPMessage == null || wechatMPMessage.getEnable() == null ? "" : wechatMPMessage.getEnable()) + "\","
                    + "\"updateTimeIsNull\":\"" + (wechatMPMessage == null || wechatMPMessage.getUpdateTime() == null) + "\""
                    + "},"
                    + "\"timestamp\":" + System.currentTimeMillis()
                    + "}\n";
            Files.write(Path.of(logPath), msg.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignore) {
        }
        // #endregion
        wechatMPMessageService.updateById(wechatMPMessage);
        return ResultUtil.data(wechatMPMessage);
    }

    @DemoSite
    @DeleteMapping("/{ids}")
    public ResultMessage<Object> delAllByIds(@PathVariable List<String> ids) {

        wechatMPMessageService.removeByIds(ids);
        return ResultUtil.success();
    }
}
