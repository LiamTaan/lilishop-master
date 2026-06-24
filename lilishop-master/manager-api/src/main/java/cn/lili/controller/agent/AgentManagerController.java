package cn.lili.controller.agent;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.agent.entity.dos.AgentRoleRelation;
import cn.lili.modules.agent.entity.dos.AgentStoreBind;
import cn.lili.modules.agent.entity.dto.AgentRoleCreateDTO;
import cn.lili.modules.agent.entity.dto.AgentStoreBindAuditDTO;
import cn.lili.modules.agent.entity.dto.AgentStoreBindDTO;
import cn.lili.modules.agent.entity.params.AgentStoreBindSearchParams;
import cn.lili.modules.agent.entity.vos.AgentRoleRelationVO;
import cn.lili.modules.agent.entity.vos.AgentStoreBindVO;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.agent.service.AgentStoreBindService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端代理商治理接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@Tag(name = "管理端,代理商治理接口")
@RequestMapping("/manager/agent")
public class AgentManagerController {

    @Autowired
    private AgentRoleRelationService agentRoleRelationService;

    @Autowired
    private AgentStoreBindService agentStoreBindService;

    @Operation(summary = "创建代理商身份")
    @PostMapping("/role")
    public ResultMessage<AgentRoleRelation> createRole(@Valid @RequestBody AgentRoleCreateDTO dto) {
        return ResultUtil.data(agentRoleRelationService.createAgentRole(dto));
    }

    @Operation(summary = "查询代理商身份列表")
    @GetMapping("/role")
    public ResultMessage<List<AgentRoleRelationVO>> listRoles() {
        return ResultUtil.data(agentRoleRelationService.listAgentRoles());
    }

    @Operation(summary = "创建代理商店铺绑定")
    @PostMapping("/store")
    public ResultMessage<AgentStoreBind> createBind(@Valid @RequestBody AgentStoreBindDTO dto) {
        return ResultUtil.data(agentStoreBindService.createBind(dto));
    }

    @Operation(summary = "查询代理商店铺绑定列表")
    @GetMapping("/store")
    public ResultMessage<IPage<AgentStoreBindVO>> listBinds(AgentStoreBindSearchParams searchParams) {
        return ResultUtil.data(agentStoreBindService.pageAgentStoreBinds(searchParams));
    }

    @Operation(summary = "审核代理商店铺绑定")
    @Parameter(name = "bindId", description = "绑定ID", required = true)
    @PutMapping("/store/{bindId}/audit")
    public ResultMessage<AgentStoreBind> auditBind(@PathVariable String bindId, @Valid @RequestBody AgentStoreBindAuditDTO dto) {
        return ResultUtil.data(agentStoreBindService.auditBind(bindId, dto));
    }

    @Operation(summary = "解绑代理商店铺")
    @Parameter(name = "bindId", description = "绑定ID", required = true)
    @PutMapping("/store/{bindId}/unbind")
    public ResultMessage<Object> unbind(@PathVariable String bindId) {
        agentStoreBindService.unbind(bindId);
        return ResultUtil.success();
    }
}
