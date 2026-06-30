package cn.lili.controller.permission;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.permission.entity.dos.DepartmentRole;
import cn.lili.modules.permission.service.DepartmentRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,部门角色接口
 *
 * @author Chopper
 * @since 2020/11/22 14:05
 */
@RestController
@RequestMapping("/manager/permission/departmentRole")
public class DepartmentRoleManagerController {
    @Autowired
    private DepartmentRoleService departmentRoleService;

    @GetMapping("/{departmentId}")
    public ResultMessage<List<DepartmentRole>> get(@PathVariable String departmentId) {
        return ResultUtil.data(departmentRoleService.listByDepartmentId(departmentId));
    }

    @PutMapping("/{departmentId}")
    public ResultMessage<DepartmentRole> update(@PathVariable String departmentId, @RequestBody List<DepartmentRole> departmentRole) {

        departmentRoleService.updateByDepartmentId(departmentId, departmentRole);
        return ResultUtil.success();
    }

}
