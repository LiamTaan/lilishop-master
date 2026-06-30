package cn.lili.controller.permission;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.permission.entity.dos.RoleMenu;
import cn.lili.modules.permission.service.RoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,角色菜单接口
 *
 * @author Chopper
 * @since 2020/11/22 11:40
 */
@RestController
@RequestMapping("/manager/permission/roleMenu")
public class RoleMenuManagerController {
    @Autowired
    private RoleMenuService roleMenuService;

    @GetMapping("/{roleId}")
    public ResultMessage<List<RoleMenu>> get(@PathVariable String roleId) {
        return ResultUtil.data(roleMenuService.findByRoleId(roleId));
    }

    @PostMapping("/{roleId}")
    public ResultMessage save(@PathVariable String roleId, @RequestBody List<RoleMenu> roleMenus) {
        roleMenuService.updateRoleMenu(roleId, roleMenus);
        return ResultUtil.success();
    }

}
