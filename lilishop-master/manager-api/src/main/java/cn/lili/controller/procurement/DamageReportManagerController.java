package cn.lili.controller.procurement;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.OperationalJudgment;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.procurement.entity.dos.DamageReport;
import cn.lili.modules.procurement.entity.dos.DamageReportItem;
import cn.lili.modules.procurement.service.DamageReportItemService;
import cn.lili.modules.procurement.service.DamageReportService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 管理端,报损单接口
 * 提供报损单分页、详情与明细查询
 *
 * @author Bulbasaur
 * @since 2025-12-18
 */
@RestController
@RequestMapping("/manager/procurement/damage-report")
public class DamageReportManagerController {

    @Autowired
    private DamageReportService damageReportService;
    @Autowired
    private DamageReportItemService damageReportItemService;

    @GetMapping("/{id}")
    public ResultMessage<DamageReport> get(@PathVariable String id) {
        return ResultUtil.data(OperationalJudgment.judgment(damageReportService.getById(id)));
    }

    @GetMapping("/{id}/items")
    public ResultMessage<List<DamageReportItem>> items(@PathVariable String id) {
        DamageReport report = OperationalJudgment.judgment(damageReportService.getById(id));
        return ResultUtil.data(damageReportItemService.listByReportId(report.getId()));
    }

    @GetMapping("/page")
    public ResultMessage<IPage<DamageReport>> page(PageVO page,
                                                   @RequestParam(required = false) String storeId,
                                                   @RequestParam(required = false) String status,
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) Date startDate,
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) Date endDate) {
        return ResultUtil.data(damageReportService.pageByCondition(page, storeId, status, startDate, endDate));
    }
}
