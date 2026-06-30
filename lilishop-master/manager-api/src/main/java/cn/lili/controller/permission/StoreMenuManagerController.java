package cn.lili.controller.permission;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.StoreMenu;
import cn.lili.modules.member.entity.vo.StoreMenuVO;
import cn.lili.modules.member.service.StoreMenuService;
import cn.lili.modules.permission.entity.dos.Menu;
import cn.lili.modules.permission.entity.dto.MenuSearchParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,菜单管理接口
 *
 * @author Chopper
 * @since 2020/11/20 12:07
 */
@Slf4j
@RestController
@RequestMapping("/manager/permission/storeMenu")
public class StoreMenuManagerController {

    @Autowired
    private StoreMenuService storeMenuService;

    @GetMapping
    public ResultMessage<List<StoreMenu>> searchPermissionList(MenuSearchParams searchParams) {
        return ResultUtil.data(storeMenuService.searchList(searchParams));
    }

    @PostMapping
    @DemoSite
    public ResultMessage<StoreMenu> add(@RequestBody StoreMenu menu) {
        try {
            storeMenuService.saveOrUpdateMenu(menu);
        } catch (Exception e) {
            log.error("添加菜单错误", e);
        }
        return ResultUtil.data(menu);
    }

    @PutMapping("/{id}")
    @DemoSite
    public ResultMessage<StoreMenu> edit(@PathVariable String id, @RequestBody StoreMenu menu) {
        menu.setId(id);
        storeMenuService.saveOrUpdateMenu(menu);
        return ResultUtil.data(menu);
    }

    @DeleteMapping("/{ids}")
    @DemoSite
    public ResultMessage<Menu> delByIds(@PathVariable List<String> ids) {
        storeMenuService.deleteIds(ids);
        return ResultUtil.success();
    }

    @GetMapping("/tree")
    public ResultMessage<List<StoreMenuVO>> getAllMenuList() {
        return ResultUtil.data(storeMenuService.tree());
    }

}
