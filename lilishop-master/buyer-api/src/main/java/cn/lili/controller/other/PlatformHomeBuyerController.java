package cn.lili.controller.other;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.page.entity.vos.PlatformHomeVO;
import cn.lili.modules.page.service.PlatformHomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 买家端,平台首页聚合接口
 *
 * @author codex
 * @since 2026/6/25
 */
@RestController
@Tag(name = "买家端,平台首页聚合接口")
@RequestMapping("/buyer/other/home")
public class PlatformHomeBuyerController {

    @Autowired
    private PlatformHomeService platformHomeService;

    @Operation(summary = "获取结构化平台首页")
    @Parameter(name = "clientType", description = "客户端类型", required = true)
    @GetMapping("/platform")
    public ResultMessage<PlatformHomeVO> platform(@RequestParam String clientType) {
        return ResultUtil.data(platformHomeService.getPlatformHome(clientType));
    }
}
