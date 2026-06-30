package cn.lili.controller.other.broadcast;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.vos.StudioVO;
import cn.lili.modules.goods.service.StudioService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

/**
 * 管理端,直播间接口
 *
 * @author Bulbasaur
 * @since 2021/5/28 11:56 上午
 */
@RestController
@RequestMapping("/manager/broadcast/studio")
public class StudioManagerController {

    @Autowired
    private StudioService studioService;

    @GetMapping
    public ResultMessage<IPage<StudioVO>> page(PageVO pageVO, String status) {
        return ResultUtil.data(studioService.studioList(pageVO, null, status));
    }

    @GetMapping("/{studioId}")
    public ResultMessage<StudioVO> studioInfo(@PathVariable String studioId) {
        return ResultUtil.data(studioService.getStudioVO(studioId));
    }

    @PutMapping("/recommend/{id}")
    public ResultMessage<Object> recommend(@PathVariable String id, @NotNull boolean recommend) {
        if (studioService.updateRecommend(id, recommend)) {
            return ResultUtil.success();
        }
        throw new ServiceException(ResultCode.ERROR);
    }
}
