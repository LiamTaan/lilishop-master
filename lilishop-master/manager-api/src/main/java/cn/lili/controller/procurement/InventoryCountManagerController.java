package cn.lili.controller.procurement;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.OperationalJudgment;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.procurement.entity.dos.InventoryCount;
import cn.lili.modules.procurement.entity.dos.InventoryCountItem;
import cn.lili.modules.procurement.service.InventoryCountItemService;
import cn.lili.modules.procurement.service.InventoryCountService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端,盘点单接口
 * 提供盘点单分页、详情、明细分页与下载
 *
 * @author Bulbasaur
 * @since 2025-12-18
 */
@RestController
@RequestMapping("/manager/procurement/inventory-count")
public class InventoryCountManagerController {

    @Autowired
    private InventoryCountService inventoryCountService;
    @Autowired
    private InventoryCountItemService inventoryCountItemService;

    @GetMapping("/page")
    public ResultMessage<IPage<InventoryCount>> page(PageVO pageVO) {
        return ResultUtil.data(inventoryCountService.page(pageVO));
    }

    @GetMapping("/{id}")
    public ResultMessage<InventoryCount> get(@PathVariable String id) {
        return ResultUtil.data(OperationalJudgment.judgment(inventoryCountService.getById(id)));
    }

    @GetMapping("/{id}/items/page")
    public ResultMessage<IPage<InventoryCountItem>> itemsPage(@PathVariable String id, PageVO pageVO) {
        InventoryCount count = OperationalJudgment.judgment(inventoryCountService.getById(id));
        Page<InventoryCountItem> page = new Page<>(pageVO.getPageNumber(), pageVO.getPageSize());
        return ResultUtil.data(inventoryCountItemService.pageByCountId(count.getId(), page));
    }

    @GetMapping("/{id}/download")
    public ResultMessage<List<InventoryCountItem>> download(@PathVariable String id) {
        InventoryCount count = OperationalJudgment.judgment(inventoryCountService.getById(id));
        return ResultUtil.data(inventoryCountItemService.listByCountId(count.getId()));
    }
}
