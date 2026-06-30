package cn.lili.controller.store;

import cn.lili.common.context.ThreadContextHolder;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.OperationalJudgment;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.order.order.entity.dos.StoreFlow;
import cn.lili.modules.order.order.service.StoreFlowService;
import cn.lili.modules.store.entity.dos.Bill;
import cn.lili.modules.store.entity.dto.BillSearchParams;
import cn.lili.modules.store.entity.vos.BillListVO;
import cn.lili.modules.store.service.BillService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

/**
 * 管理端,商家结算单接口
 *
 * @author Chopper
 * @since 2020/11/17 7:23 下午
 */
@RestController
@RequestMapping("/manager/order/bill")
public class BillManagerController {
    @Autowired
    private BillService billService;

    @Autowired
    private StoreFlowService storeFlowService;

    @GetMapping("/get/{id}")
    public ResultMessage<Bill> get(@PathVariable @NotNull String id) {
        return ResultUtil.data(billService.getById(id));
    }

    @GetMapping("/getByPage")
    public ResultMessage<IPage<BillListVO>> getByPage(BillSearchParams billSearchParams) {
        return ResultUtil.data(billService.billPage(billSearchParams));
    }

    @GetMapping("/{id}/getStoreFlow")
    public ResultMessage<IPage<StoreFlow>> getStoreFlow(@PathVariable String id, String flowType, PageVO pageVO) {
        return ResultUtil.data(storeFlowService.getStoreFlow(id, flowType, pageVO));
    }

    @PutMapping("/pay/{id}")
    public ResultMessage<Object> pay(@PathVariable String id) {
        billService.complete(id);
        return ResultUtil.success();
    }

    @GetMapping(value = "/downLoad/{id}", produces = "application/octet-stream")
    public void downLoadDeliverExcel(@PathVariable String id) {
            OperationalJudgment.judgment(billService.getById(id));
            HttpServletResponse response = ThreadContextHolder.getHttpResponse();
            billService.download(response, id);
    }
}
