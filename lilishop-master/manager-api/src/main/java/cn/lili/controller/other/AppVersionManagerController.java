package cn.lili.controller.other;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.system.entity.dos.AppVersion;
import cn.lili.modules.system.service.AppVersionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理端,app版本控制器
 *
 * @author Chopper
 * @since 2018-07-04 21:50:52
 */
@RestController
@RequestMapping("/manager/other/appVersion")
public class AppVersionManagerController {
    @Autowired
    private AppVersionService appVersionService;


    @GetMapping
    public ResultMessage<IPage<AppVersion>> getByPage(PageVO page, String type) {
        return ResultUtil.data(this.appVersionService.pageByType(type, page));
    }


    @PostMapping
    public ResultMessage<Object> add(@Valid @RequestBody AppVersion appVersion) {

        if(this.appVersionService.checkAppVersion(appVersion)){
            if(this.appVersionService.save(appVersion)){
                return ResultUtil.success();
            }
        }
        throw new ServiceException(ResultCode.ERROR);
    }

    @PutMapping("/{id}")
    public ResultMessage<Object> edit(@Valid @RequestBody AppVersion appVersion,
            @PathVariable String id) {
        appVersion.setId(id);
        if(this.appVersionService.checkAppVersion(appVersion)){
            if(this.appVersionService.updateById(appVersion)){
                return ResultUtil.success();
            }
        }
        throw new ServiceException(ResultCode.ERROR);
    }

    @DeleteMapping("/{id}")
    public ResultMessage<Boolean> delete(
            @PathVariable String id) {
        if(this.appVersionService.removeById(id)){
            return ResultUtil.success();
        }
        throw new ServiceException(ResultCode.ERROR);
    }

}
