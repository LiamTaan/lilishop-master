package cn.lili.controller.order;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.order.order.entity.dos.OrderComplaint;
import cn.lili.modules.order.order.entity.dto.OrderComplaintCommunicationDTO;
import cn.lili.modules.order.order.entity.enums.CommunicationOwnerEnum;
import cn.lili.modules.order.order.entity.enums.OrderComplaintStatusEnum;
import cn.lili.modules.order.order.entity.vo.OrderComplaintCommunicationVO;
import cn.lili.modules.order.order.entity.vo.OrderComplaintOperationParams;
import cn.lili.modules.order.order.entity.vo.OrderComplaintSearchParams;
import cn.lili.modules.order.order.entity.vo.OrderComplaintVO;
import cn.lili.modules.order.order.service.OrderComplaintCommunicationService;
import cn.lili.modules.order.order.service.OrderComplaintService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 管理端,交易投诉接口
 *
 * @author paulG
 * @since 2020/12/5
 */
@RestController
@RequestMapping("/manager/order/complain")
public class OrderComplaintManagerController {

    /**
     * 交易投诉
     */
    @Autowired
    private OrderComplaintService orderComplaintService;

    /**
     * 交易投诉沟通
     */
    @Autowired
    private OrderComplaintCommunicationService orderComplaintCommunicationService;

    @GetMapping("/{id}")
    public ResultMessage<OrderComplaintVO> get(@PathVariable String id) {
        return ResultUtil.data(orderComplaintService.getOrderComplainById(id));
    }

    @GetMapping
    public ResultMessage<IPage<OrderComplaint>> get(OrderComplaintSearchParams searchParams, PageVO pageVO) {
        return ResultUtil.data(orderComplaintService.getOrderComplainByPage(searchParams, pageVO));
    }

    @PutMapping
    public ResultMessage<OrderComplaintVO> update(@RequestBody OrderComplaintVO orderComplainVO) {
        orderComplaintService.updateOrderComplain(orderComplainVO);
        return ResultUtil.data(orderComplainVO);

    }

    @PostMapping("/communication")
    public ResultMessage<OrderComplaintCommunicationVO> addCommunication(@RequestBody OrderComplaintCommunicationDTO communicationDTO) {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        OrderComplaintCommunicationVO communicationVO = new OrderComplaintCommunicationVO(communicationDTO.getComplainId(), communicationDTO.getContent(), CommunicationOwnerEnum.PLATFORM.name(), currentUser.getUsername(), currentUser.getId());
        orderComplaintCommunicationService.addCommunication(communicationVO);
        return ResultUtil.data(communicationVO);
    }

    @PreventDuplicateSubmissions
    @PutMapping("/status")
    public ResultMessage<Object> updateStatus(@RequestBody OrderComplaintOperationParams orderComplainVO) {
        orderComplaintService.updateOrderComplainByStatus(orderComplainVO);
        return ResultUtil.success();
    }


    @PreventDuplicateSubmissions
    @PutMapping("/complete/{id}")
    public ResultMessage<Object> complete(@PathVariable String id, String arbitrationResult) {
        //新建对象
        OrderComplaintOperationParams orderComplaintOperationParams = new OrderComplaintOperationParams();
        orderComplaintOperationParams.setComplainId(id);
        orderComplaintOperationParams.setArbitrationResult(arbitrationResult);
        orderComplaintOperationParams.setComplainStatus(OrderComplaintStatusEnum.COMPLETE.name());

        //修改状态
        orderComplaintService.updateOrderComplainByStatus(orderComplaintOperationParams);
        return ResultUtil.success();
    }
}
