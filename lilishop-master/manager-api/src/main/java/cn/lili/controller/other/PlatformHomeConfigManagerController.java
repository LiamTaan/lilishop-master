package cn.lili.controller.other;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.page.entity.dto.PlatformHomeConfigSaveDTO;
import cn.lili.modules.page.entity.vos.PlatformHomeConfigVO;
import cn.lili.modules.page.service.PlatformHomeConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端,首页配置聚合接口
 *
 * @author codex
 * @since 2026/6/25
 */
@RestController
@Tag(name = "管理端,首页配置聚合接口")
@RequestMapping("/manager/other/platformHomeConfig")
public class PlatformHomeConfigManagerController {

    @Autowired
    private PlatformHomeConfigService platformHomeConfigService;

    @Operation(summary = "获取结构化首页配置")
    @Parameter(name = "clientType", description = "客户端类型", required = true)
    @GetMapping
    public ResultMessage<PlatformHomeConfigVO> get(@RequestParam String clientType) {
        return ResultUtil.data(platformHomeConfigService.getConfig(clientType));
    }

    @DemoSite
    @Operation(summary = "保存结构化首页配置")
    @PutMapping
    public ResultMessage<PlatformHomeConfigVO> save(@Valid @RequestBody PlatformHomeConfigSaveDTO saveDTO) {
        return ResultUtil.data(platformHomeConfigService.saveConfig(saveDTO));
    }
}
