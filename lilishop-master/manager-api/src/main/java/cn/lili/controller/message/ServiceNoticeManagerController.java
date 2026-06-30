package cn.lili.controller.message;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.common.vo.SearchVO;
import cn.lili.modules.system.entity.dos.ServiceNotice;
import cn.lili.modules.system.service.ServiceNoticeService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,服务订阅消息接口
 *
 * @author Chopper
 * @since 2020/11/17 4:33 下午
 */
@RestController
@RequestMapping("/manager/message/serviceNotice")
public class ServiceNoticeManagerController {
    @Autowired
    private ServiceNoticeService serviceNoticeService;

    @GetMapping("/{id}")
    public ResultMessage<ServiceNotice> get(@PathVariable String id) {
        ServiceNotice serviceNotice = serviceNoticeService.getById(id);
        return ResultUtil.data(serviceNotice);
    }

    @GetMapping("/page")
    public ResultMessage<IPage<ServiceNotice>> getByPage(ServiceNotice entity,
                                                         SearchVO searchVo,
                                                         PageVO page) {
        return ResultUtil.data(serviceNoticeService.getByPage(entity, searchVo, page));
    }

    @PostMapping
    public ResultMessage<ServiceNotice> save(@RequestBody ServiceNotice serviceNotice) {
        //标记平台消息
        serviceNotice.setStoreId("-1");
        serviceNoticeService.saveOrUpdate(serviceNotice);
        return ResultUtil.data(serviceNotice);
    }

    @PostMapping("/{id}")
    public ResultMessage<ServiceNotice> update(@PathVariable String id, @RequestBody ServiceNotice serviceNotice) {
        serviceNotice.setId(id);
        serviceNoticeService.saveOrUpdate(serviceNotice);
        return ResultUtil.data(serviceNotice);
    }

    @DeleteMapping("/{ids}")
    public ResultMessage<Object> delAllByIds(@PathVariable List<String> ids) {
        validateIds(ids);
        serviceNoticeService.removeByIds(ids);
        return ResultUtil.success();
    }

    private void validateIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
        for (String id : ids) {
            if (id == null || !id.matches("\\d+")) {
                throw new ServiceException(ResultCode.PARAMS_ERROR);
            }
        }
    }
}
